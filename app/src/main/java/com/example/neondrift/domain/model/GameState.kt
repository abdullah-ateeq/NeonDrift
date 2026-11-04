package com.example.neondrift.domain.model

data class GameState(
    val isReady: Boolean = false,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,

    val distance: Float = 0f,
    val score: Int = 0,
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,

    val playerX: Float = 0f,
    val playerVelocityX: Float = 0f,
    val playerScreenY: Float = 0f,
    val cameraWorldY: Float = 0f,

    val difficulty: DifficultyLevel = DifficultyLevel.EASY,
    val pathSegments: List<PathSegment> = emptyList(),

    // Visual feedback
    val trailPoints: List<Pair<Float, Float>> = emptyList(),
    val screenShakeX: Float = 0f,
    val screenShakeY: Float = 0f,
    val collisionFlashAlpha: Float = 0f,
    val pathDistortionProgress: Float = 0f,
    val milestoneMessage: String? = null,
    val milestoneTimer: Float = 0f,

    val isNewHighScore: Boolean = false,
    val screenWidth: Float = 0f,
    val screenHeight: Float = 0f
)
