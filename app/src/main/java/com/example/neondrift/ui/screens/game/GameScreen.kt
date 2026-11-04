package com.example.neondrift.ui.screens.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neondrift.data.ThemeMode
import com.example.neondrift.ui.components.NeonButton
import com.example.neondrift.ui.components.NeonCard
import com.example.neondrift.ui.theme.NeonAmber
import com.example.neondrift.ui.theme.NeonCyan
import com.example.neondrift.ui.theme.NeonPurple
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateToResult: (score: Int, distance: Float, streak: Int, isNewHigh: Boolean) -> Unit,
    onNavigateHome: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val systemDark = isSystemInDarkTheme()
    val isDark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    // Handle game-over transition with a brief suspense delay to see the collision effect
    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) {
            delay(450)
            onNavigateToResult(
                state.score,
                state.distance,
                state.highestStreak,
                state.isNewHighScore
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        // Initialize game when dimensions are known
        LaunchedEffect(screenWidth, screenHeight) {
            if (screenWidth > 0f && screenHeight > 0f && !state.isReady) {
                viewModel.setupGame(screenWidth, screenHeight)
            }
        }

        // Gameplay Canvas
        GameCanvas(
            state = state,
            isDarkTheme = isDark,
            modifier = Modifier
                .fillMaxSize()
                // Combined gesture listener for Drag & Tap steering
                .pointerInput(state.isPlaying, state.isPaused) {
                    if (!state.isPlaying || state.isPaused) return@pointerInput
                    detectDragGestures(
                        onDragStart = { },
                        onDragEnd = { },
                        onDragCancel = { },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            viewModel.onSteerDrag(dragAmount.x)
                        }
                    )
                }
                .pointerInput(state.isPlaying, state.isPaused) {
                    if (!state.isPlaying || state.isPaused) return@pointerInput
                    detectTapGestures(
                        onTap = { offset ->
                            if (offset.x < size.width / 2f) {
                                viewModel.onTapLeft()
                            } else {
                                viewModel.onTapRight()
                            }
                        }
                    )
                }
        )

        // Top HUD Overlay
        HUDOverlay(
            score = state.score,
            distance = state.distance,
            streak = state.currentStreak,
            difficulty = state.difficulty.displayName,
            onPauseClick = { viewModel.pause() }
        )

        // Milestone Notification Banner
        AnimatedVisibility(
            visible = state.milestoneMessage != null,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { -it },
            exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp)
        ) {
            state.milestoneMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = NeonCyan.copy(alpha = 0.92f),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = msg,
                        color = Color(0xFF090B10),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }

        // Pause Menu Dialog Overlay
        if (state.isPaused) {
            PauseOverlay(
                onResume = { viewModel.resume() },
                onRestart = { viewModel.restart() },
                onHome = onNavigateHome
            )
        }
    }
}

@Composable
private fun HUDOverlay(
    score: Int,
    distance: Float,
    streak: Int,
    difficulty: String,
    onPauseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Score & Distance column
        Column {
            Text(
                text = "$score",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${distance.toInt()}m",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                if (streak > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonAmber.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "STREAK x$streak",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonAmber,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Difficulty Badge & Pause Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Text(
                    text = difficulty.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.0.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun PauseOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        NeonCard(
            modifier = Modifier
                .width(300.dp)
                .padding(16.dp),
            borderColor = NeonCyan.copy(alpha = 0.6f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "PAUSED",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = NeonCyan
                )

                Spacer(modifier = Modifier.height(4.dp))

                NeonButton(
                    text = "Resume",
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = true
                )

                NeonButton(
                    text = "Restart",
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = false
                )

                NeonButton(
                    text = "Main Menu",
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = false,
                    accentColor = NeonPurple
                )
            }
        }
    }
}
