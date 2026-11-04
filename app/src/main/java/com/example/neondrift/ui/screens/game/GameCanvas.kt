package com.example.neondrift.ui.screens.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.neondrift.domain.engine.PathGenerator
import com.example.neondrift.domain.model.DifficultyLevel
import com.example.neondrift.domain.model.GameState
import com.example.neondrift.ui.theme.NeonCyan
import com.example.neondrift.ui.theme.NeonCyanBright
import com.example.neondrift.ui.theme.NeonCyanGlow
import com.example.neondrift.ui.theme.NeonPink
import com.example.neondrift.ui.theme.NeonPurple
import com.example.neondrift.ui.theme.NeonPurpleGlow
import kotlin.math.sin

@Composable
fun GameCanvas(
    state: GameState,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val boundaryColor = when (state.difficulty) {
        DifficultyLevel.EASY -> NeonCyan
        DifficultyLevel.MEDIUM -> NeonPurple
        DifficultyLevel.HARD -> NeonPink
    }
    val haloColor = when (state.difficulty) {
        DifficultyLevel.EASY -> NeonCyanGlow
        DifficultyLevel.MEDIUM -> NeonPurpleGlow
        DifficultyLevel.HARD -> NeonPink.copy(alpha = 0.45f)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Apply screen shake transform if active
        withTransform({
            translate(
                left = state.screenShakeX,
                top = state.screenShakeY
            )
        }) {
            // 1. Futuristic background grid / speed lines
            drawSpeedGrid(
                width = width,
                height = height,
                cameraWorldY = state.cameraWorldY,
                isDarkTheme = isDarkTheme
            )

            // 2. Draw path ribbons and boundaries
            if (state.pathSegments.isNotEmpty()) {
                drawNeonPath(
                    state = state,
                    width = width,
                    height = height,
                    boundaryColor = boundaryColor,
                    haloColor = haloColor,
                    isDarkTheme = isDarkTheme
                )
            }

            // 3. Player trail
            drawPlayerTrail(
                trailPoints = state.trailPoints,
                accentColor = boundaryColor
            )

            // 4. Glowing player point
            if (state.playerX > 0f) {
                drawPlayerPoint(
                    playerX = state.playerX,
                    playerY = state.playerScreenY,
                    accentColor = boundaryColor,
                    isDistorted = state.pathDistortionProgress > 0f
                )
            }

            // 5. Collision flash overlay
            if (state.collisionFlashAlpha > 0f) {
                drawRect(
                    color = Color.White.copy(alpha = state.collisionFlashAlpha.coerceIn(0f, 1f)),
                    topLeft = Offset.Zero,
                    size = size
                )
            }
        }
    }
}

private fun DrawScope.drawSpeedGrid(
    width: Float,
    height: Float,
    cameraWorldY: Float,
    isDarkTheme: Boolean
) {
    val baseLineColor = if (isDarkTheme) Color(0xFF161C2C) else Color(0xFFE2E8F0)
    val gridSpacingY = 120f
    val offsetModY = cameraWorldY % gridSpacingY

    // Moving horizontal grid lines to give visceral sense of velocity
    var y = offsetModY
    while (y < height) {
        val alphaFactor = (y / height) * 0.45f
        drawLine(
            color = baseLineColor.copy(alpha = alphaFactor),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.2f
        )
        y += gridSpacingY
    }
}

private fun DrawScope.drawNeonPath(
    state: GameState,
    width: Float,
    height: Float,
    boundaryColor: Color,
    haloColor: Color,
    isDarkTheme: Boolean
) {
    val steps = 44
    val stepHeight = height / steps

    val leftPoints = ArrayList<Offset>(steps + 1)
    val rightPoints = ArrayList<Offset>(steps + 1)
    val centerPoints = ArrayList<Offset>(steps + 1)

    val distortion = state.pathDistortionProgress

    for (i in 0..steps) {
        val screenY = i * stepHeight
        // Invert Y so top of screen (0) is ahead in world
        val worldY = state.cameraWorldY + (height - screenY)

        val (rawCenterX, w) = PathGenerator.samplePath(worldY, state.pathSegments)

        // Electrical glitch distortion on collision
        val glitchOffset = if (distortion > 0f) {
            sin(screenY * 0.05f + distortion * 20f) * 25f * distortion
        } else {
            0f
        }

        val cx = rawCenterX + glitchOffset
        val halfW = w / 2f

        centerPoints.add(Offset(cx, screenY))
        leftPoints.add(Offset(cx - halfW, screenY))
        rightPoints.add(Offset(cx + halfW, screenY))
    }

    // Interior Track Glow
    val trackPath = Path().apply {
        if (leftPoints.isNotEmpty()) {
            moveTo(leftPoints[0].x, leftPoints[0].y)
            for (p in leftPoints) lineTo(p.x, p.y)
            for (i in rightPoints.indices.reversed()) {
                lineTo(rightPoints[i].x, rightPoints[i].y)
            }
            close()
        }
    }

    val trackInteriorColor = if (isDarkTheme) {
        haloColor.copy(alpha = 0.06f)
    } else {
        boundaryColor.copy(alpha = 0.04f)
    }
    drawPath(path = trackPath, color = trackInteriorColor)

    // Center precision guide line (subtle dashed/faint)
    val centerPath = Path().apply {
        if (centerPoints.isNotEmpty()) {
            moveTo(centerPoints[0].x, centerPoints[0].y)
            for (p in centerPoints) lineTo(p.x, p.y)
        }
    }
    drawPath(
        path = centerPath,
        color = boundaryColor.copy(alpha = 0.22f),
        style = Stroke(
            width = 1.5f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Construct boundary curves
    val leftPath = Path().apply {
        if (leftPoints.isNotEmpty()) {
            moveTo(leftPoints[0].x, leftPoints[0].y)
            for (p in leftPoints) lineTo(p.x, p.y)
        }
    }

    val rightPath = Path().apply {
        if (rightPoints.isNotEmpty()) {
            moveTo(rightPoints[0].x, rightPoints[0].y)
            for (p in rightPoints) lineTo(p.x, p.y)
        }
    }

    // Draw Outer Soft Glow Halo
    val haloStroke = Stroke(width = 8.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawPath(path = leftPath, color = haloColor, style = haloStroke)
    drawPath(path = rightPath, color = haloColor, style = haloStroke)

    // Draw Sharp Core Boundary Lines
    val coreStroke = Stroke(width = 2.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawPath(path = leftPath, color = boundaryColor, style = coreStroke)
    drawPath(path = rightPath, color = boundaryColor, style = coreStroke)
}

private fun DrawScope.drawPlayerTrail(
    trailPoints: List<Pair<Float, Float>>,
    accentColor: Color
) {
    if (trailPoints.size < 2) return

    val total = trailPoints.size.toFloat()
    for (i in 0 until trailPoints.size - 1) {
        val p1 = trailPoints[i]
        val p2 = trailPoints[i + 1]
        val progress = 1f - (i / total) // 1.0 (recent) down to 0.0 (old)

        val alpha = progress * 0.45f
        val strokeWidth = progress * 6f + 1f

        drawLine(
            color = accentColor.copy(alpha = alpha),
            start = Offset(p1.first, p1.second + (i * 3.5f)),
            end = Offset(p2.first, p2.second + ((i + 1) * 3.5f)),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawPlayerPoint(
    playerX: Float,
    playerY: Float,
    accentColor: Color,
    isDistorted: Boolean
) {
    val center = Offset(playerX, playerY)

    // Outer corona glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.55f),
                accentColor.copy(alpha = 0.2f),
                Color.Transparent
            ),
            center = center,
            radius = 32f
        ),
        radius = 32f,
        center = center
    )

    // Vivid mid halo
    drawCircle(
        color = accentColor,
        radius = if (isDistorted) 16f else 11f,
        center = center,
        style = Stroke(width = 3f)
    )

    // Pure incandescent white core spark
    drawCircle(
        color = NeonCyanBright,
        radius = if (isDistorted) 8f else 6f,
        center = center
    )
}
