package com.example.neondrift.ui.screens.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.neondrift.data.AppSettings
import com.example.neondrift.data.GameStats
import com.example.neondrift.data.PreferencesRepository
import com.example.neondrift.domain.audio.HapticsManager
import com.example.neondrift.domain.audio.SoundSynth
import com.example.neondrift.domain.engine.CollisionDetector
import com.example.neondrift.domain.engine.DifficultyManager
import com.example.neondrift.domain.engine.PathGenerator
import com.example.neondrift.domain.engine.ScoreCalculator
import com.example.neondrift.domain.model.GameState
import com.example.neondrift.domain.model.PathSegment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = PreferencesRepository(application)
    private val hapticsManager = HapticsManager(application)

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _stats = MutableStateFlow(GameStats())
    val stats: StateFlow<GameStats> = _stats.asStateFlow()

    private var gameLoopJob: Job? = null
    private var pathGenerator: PathGenerator? = null

    // Steering input control
    private var activeSteerInput: Float = 0f // -1f (left) .. +1f (right)
    private var centerBonusAccumulator: Float = 0f
    private var bonusScoreFromMilestonesAndCenter: Int = 0

    init {
        viewModelScope.launch {
            preferencesRepository.settingsFlow.collect { _settings.value = it }
        }
        viewModelScope.launch {
            preferencesRepository.statsFlow.collect { _stats.value = it }
        }
    }

    /**
     * Initializes game dimensions and prepares fresh game session.
     */
    fun setupGame(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return

        pathGenerator = PathGenerator(screenWidth = width)
        val initialSegments = pathGenerator?.createInitialPath(16) ?: emptyList()

        val playerY = height * 0.72f
        val startX = width / 2f

        activeSteerInput = 0f
        centerBonusAccumulator = 0f
        bonusScoreFromMilestonesAndCenter = 0

        _gameState.value = GameState(
            isReady = true,
            isPlaying = true,
            isPaused = false,
            isGameOver = false,
            distance = 0f,
            score = 0,
            currentStreak = 0,
            highestStreak = 0,
            playerX = startX,
            playerVelocityX = 0f,
            playerScreenY = playerY,
            cameraWorldY = 0f,
            difficulty = DifficultyManager.getDifficulty(0f),
            pathSegments = initialSegments,
            trailPoints = listOf(Pair(startX, playerY)),
            screenWidth = width,
            screenHeight = height
        )

        startGameLoop()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTimeNanos = System.nanoTime()

            while (isActive) {
                val now = System.nanoTime()
                val dt = ((now - lastTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTimeNanos = now

                val state = _gameState.value
                if (state.isPlaying && !state.isPaused && !state.isGameOver) {
                    tick(dt)
                } else if (state.isGameOver) {
                    // Update game-over visual effects decay
                    updateGameOverEffects(dt)
                }

                delay(16) // ~60fps loop
            }
        }
    }

    private fun tick(dt: Float) {
        val state = _gameState.value
        val generator = pathGenerator ?: return
        val currentSettings = _settings.value

        // 1. Difficulty & Speed
        val difficulty = DifficultyManager.getDifficulty(state.distance)
        val scrollSpeed = DifficultyManager.getScrollSpeed(state.distance)

        // 2. Camera & Distance advancement
        val newCameraWorldY = state.cameraWorldY + (scrollSpeed * dt)
        val prevDistance = state.distance
        val newDistance = newCameraWorldY / 100f // 100 pixels = 1 meter

        // 3. Smooth Steering Physics
        // Sensitivity scales with difficulty
        val steerAccel = 3600f * difficulty.steeringSensitivity
        val maxSpeed = 820f * difficulty.steeringSensitivity
        val damping = 0.84f

        var velX = state.playerVelocityX + (activeSteerInput * steerAccel * dt)
        velX = velX.coerceIn(-maxSpeed, maxSpeed)
        // Apply friction when no active directional input
        if (activeSteerInput == 0f) {
            velX *= damping
        }

        var newPlayerX = state.playerX + (velX * dt)
        val playerMargin = 16f
        newPlayerX = newPlayerX.coerceIn(playerMargin, state.screenWidth - playerMargin)

        // 4. Path Segments Generation & Recycling
        val currentSegments = state.pathSegments.toMutableList()
        val lookaheadTargetY = newCameraWorldY + (state.screenHeight * 2.2f)

        while (currentSegments.isNotEmpty() && currentSegments.last().worldY < lookaheadTargetY) {
            val nextSeg = generator.generateNextSegment(currentSegments, newDistance)
            currentSegments.add(nextSeg)
        }

        // Prune off-screen segments behind camera
        val cleanupBehindY = newCameraWorldY - (state.screenHeight * 0.4f)
        while (currentSegments.size > 10 && currentSegments[1].worldY < cleanupBehindY) {
            currentSegments.removeAt(0)
        }

        // 5. Sample Path at Player Position & Collision Check
        // Path moves toward player: the player's Y relative to path origin is cameraWorldY + (screenHeight - playerScreenY)
        val playerPathWorldY = newCameraWorldY + (state.screenHeight - state.playerScreenY)
        val (pathCenterX, pathWidth) = PathGenerator.samplePath(playerPathWorldY, currentSegments)

        val collisionResult = CollisionDetector.checkCollision(
            playerX = newPlayerX,
            pathCenterX = pathCenterX,
            pathWidth = pathWidth,
            playerRadius = 14f,
            forgivenessRadius = 6f
        )

        // 6. Collision Trigger
        if (collisionResult.hasCollided) {
            triggerGameOver(newDistance, state)
            return
        }

        // 7. Scoring & Precision Center Bonus
        val isCenter = CollisionDetector.isNearCenter(newPlayerX, pathCenterX, pathWidth)
        if (isCenter) {
            centerBonusAccumulator += dt
            if (centerBonusAccumulator >= 0.25f) {
                val centerBonus = ScoreCalculator.calculateCenterBonus(
                    isNearCenter = true,
                    deltaTimeSec = centerBonusAccumulator,
                    streak = state.currentStreak
                )
                bonusScoreFromMilestonesAndCenter += centerBonus
                centerBonusAccumulator = 0f
            }
        } else {
            centerBonusAccumulator = 0f
        }

        // Base distance score
        val baseScore = ScoreCalculator.calculateBaseScore(newDistance)
        var totalScore = baseScore + bonusScoreFromMilestonesAndCenter

        // 8. Milestone Check
        var newStreak = state.currentStreak
        var newHighestStreak = state.highestStreak
        var milestoneMsg = state.milestoneMessage
        var milestoneTimer = max(0f, state.milestoneTimer - dt)

        val milestone = ScoreCalculator.checkMilestone(prevDistance, newDistance, state.currentStreak)
        if (milestone != null) {
            newStreak += milestone.streakIncrease
            if (newStreak > newHighestStreak) {
                newHighestStreak = newStreak
            }
            bonusScoreFromMilestonesAndCenter += milestone.bonusScore
            totalScore = baseScore + bonusScoreFromMilestonesAndCenter
            milestoneMsg = milestone.message
            milestoneTimer = 2.2f

            SoundSynth.playMilestone(currentSettings.soundEnabled)
            hapticsManager.playMilestone(currentSettings.hapticsEnabled)
        }

        if (milestoneTimer <= 0f) {
            milestoneMsg = null
        }

        // 9. Trail points
        val updatedTrail = (listOf(Pair(newPlayerX, state.playerScreenY)) + state.trailPoints)
            .take(14)

        // Update state
        _gameState.update {
            it.copy(
                distance = newDistance,
                score = totalScore,
                currentStreak = newStreak,
                highestStreak = newHighestStreak,
                playerX = newPlayerX,
                playerVelocityX = velX,
                cameraWorldY = newCameraWorldY,
                difficulty = difficulty,
                pathSegments = currentSegments,
                trailPoints = updatedTrail,
                milestoneMessage = milestoneMsg,
                milestoneTimer = milestoneTimer,
                screenShakeX = 0f,
                screenShakeY = 0f,
                collisionFlashAlpha = 0f
            )
        }
    }

    private fun triggerGameOver(finalDistance: Float, lastState: GameState) {
        val currentSettings = _settings.value
        SoundSynth.playCollision(currentSettings.soundEnabled)
        hapticsManager.playCollision(currentSettings.hapticsEnabled)

        viewModelScope.launch {
            val isNewBest = preferencesRepository.updateHighScore(lastState.score, finalDistance)
            _gameState.update { it.copy(isNewHighScore = isNewBest) }
        }

        _gameState.update {
            it.copy(
                isPlaying = false,
                isGameOver = true,
                screenShakeX = (Random.nextFloat() * 2f - 1f) * 20f,
                screenShakeY = (Random.nextFloat() * 2f - 1f) * 20f,
                collisionFlashAlpha = 0.95f,
                pathDistortionProgress = 1.0f
            )
        }
    }

    private fun updateGameOverEffects(dt: Float) {
        _gameState.update { current ->
            val shakeDecay = 0.88f
            val flashDecay = max(0f, current.collisionFlashAlpha - dt * 2.2f)
            val distortDecay = max(0f, current.pathDistortionProgress - dt * 1.5f)
            current.copy(
                screenShakeX = current.screenShakeX * shakeDecay,
                screenShakeY = current.screenShakeY * shakeDecay,
                collisionFlashAlpha = flashDecay,
                pathDistortionProgress = distortDecay
            )
        }
    }

    // Input handlers
    fun onSteerDrag(dragDeltaX: Float) {
        if (!_gameState.value.isPlaying || _gameState.value.isPaused) return
        _gameState.update { state ->
            val newX = (state.playerX + dragDeltaX).coerceIn(16f, state.screenWidth - 16f)
            state.copy(
                playerX = newX,
                playerVelocityX = dragDeltaX * 30f
            )
        }
    }

    fun onTapLeft() {
        if (!_gameState.value.isPlaying || _gameState.value.isPaused) return
        activeSteerInput = -1f
        hapticsManager.playTap(_settings.value.hapticsEnabled)
        _gameState.update { state ->
            state.copy(playerVelocityX = -520f)
        }
    }

    fun onTapRight() {
        if (!_gameState.value.isPlaying || _gameState.value.isPaused) return
        activeSteerInput = 1f
        hapticsManager.playTap(_settings.value.hapticsEnabled)
        _gameState.update { state ->
            state.copy(playerVelocityX = 520f)
        }
    }

    fun setContinuousSteer(direction: Float) {
        // -1f for left, +1f for right, 0f for neutral
        activeSteerInput = direction.coerceIn(-1f, 1f)
    }

    fun pause() {
        if (_gameState.value.isPlaying && !_gameState.value.isGameOver) {
            _gameState.update { it.copy(isPaused = true) }
        }
    }

    fun resume() {
        if (_gameState.value.isPlaying && !_gameState.value.isGameOver) {
            _gameState.update { it.copy(isPaused = false) }
        }
    }

    fun restart() {
        val state = _gameState.value
        setupGame(state.screenWidth, state.screenHeight)
    }
}
