package com.example.neondrift.ui.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neondrift.data.GameStats
import com.example.neondrift.ui.components.NeonButton
import com.example.neondrift.ui.components.NeonCard
import com.example.neondrift.ui.theme.NeonCyan
import com.example.neondrift.ui.theme.NeonCyanBright
import com.example.neondrift.ui.theme.NeonCyanGlow
import com.example.neondrift.ui.theme.NeonPurple
import com.example.neondrift.ui.theme.NeonPurpleGlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sin

@Composable
fun HomeScreen(
    statsState: StateFlow<GameStats>,
    onPlayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val stats by statsState.collectAsState()

    // Smooth winding animation for preview path
    val infiniteTransition = rememberInfiniteTransition(label = "homeWinding")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pathOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Animated winding path preview background
        HomePathPreview(
            scrollOffset = animOffset,
            modifier = Modifier.fillMaxSize()
        )

        // Top Navigation Action Icons (Settings & About)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 44.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = onAboutClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Main Content (Title, Instruction, Stats, Play Button)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 110.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NEONDRIFT",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Stay inside the path.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp
                )
            }

            // Stats Card
            NeonCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "BEST SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${stats.bestScore}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(48.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "BEST DISTANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${stats.bestDistance.toInt()}m",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonPurple
                        )
                    }
                }
            }

            // Bottom Play Action
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NeonButton(
                    text = "Play",
                    onClick = onPlayClick,
                    modifier = Modifier.fillMaxWidth(),
                    height = 62.dp,
                    isPrimary = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Drag or tap left/right to steer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun HomePathPreview(
    scrollOffset: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val steps = 30
        val stepHeight = height / steps
        val pathWidth = width * 0.42f
        val halfW = pathWidth / 2f

        val leftPts = ArrayList<Offset>(steps + 1)
        val rightPts = ArrayList<Offset>(steps + 1)

        for (i in 0..steps) {
            val y = i * stepHeight
            val worldY = scrollOffset + (height - y)
            val wave = sin(worldY * 0.006f) * (width * 0.22f)
            val cx = (width / 2f) + wave

            leftPts.add(Offset(cx - halfW, y))
            rightPts.add(Offset(cx + halfW, y))
        }

        val leftPath = Path().apply {
            if (leftPts.isNotEmpty()) {
                moveTo(leftPts[0].x, leftPts[0].y)
                for (p in leftPts) lineTo(p.x, p.y)
            }
        }

        val rightPath = Path().apply {
            if (rightPts.isNotEmpty()) {
                moveTo(rightPts[0].x, rightPts[0].y)
                for (p in rightPts) lineTo(p.x, p.y)
            }
        }

        // Draw preview lines
        val halo = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawPath(leftPath, NeonCyanGlow.copy(alpha = 0.22f), style = halo)
        drawPath(rightPath, NeonPurpleGlow.copy(alpha = 0.22f), style = halo)

        val core = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawPath(leftPath, NeonCyan.copy(alpha = 0.45f), style = core)
        drawPath(rightPath, NeonPurple.copy(alpha = 0.45f), style = core)

        // Drifting point in preview
        val playerWorldY = scrollOffset + (height * 0.32f)
        val playerWave = sin(playerWorldY * 0.006f) * (width * 0.22f)
        val pCenter = Offset((width / 2f) + playerWave, height * 0.68f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonCyan.copy(alpha = 0.5f), Color.Transparent),
                center = pCenter,
                radius = 26f
            ),
            radius = 26f,
            center = pCenter
        )
        drawCircle(color = NeonCyanBright, radius = 5f, center = pCenter)
    }
}
