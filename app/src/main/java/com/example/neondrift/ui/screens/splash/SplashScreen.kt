package com.example.neondrift.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neondrift.ui.theme.NeonCyan
import com.example.neondrift.ui.theme.NeonCyanBright
import com.example.neondrift.ui.theme.NeonPurple
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Smooth entrance animation
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
        delay(700)
        onSplashFinished()
    }

    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Neon Drifting Emblem
            Canvas(modifier = Modifier.size(110.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val rad = size.width * 0.42f

                // Outer decorative ring
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.2f),
                    radius = rad,
                    center = center,
                    style = Stroke(width = 2.5f)
                )

                // Drifting satellite points
                val angleRad = Math.toRadians(rotation.value.toDouble())
                val satX = center.x + (cos(angleRad) * rad).toFloat()
                val satY = center.y + (sin(angleRad) * rad).toFloat()

                val satCenter = Offset(satX, satY)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.6f), Color.Transparent),
                        center = satCenter,
                        radius = 24f
                    ),
                    radius = 24f,
                    center = satCenter
                )
                drawCircle(color = NeonCyanBright, radius = 5.5f, center = satCenter)

                // Center Core
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonPurple.copy(alpha = 0.5f), Color.Transparent),
                        center = center,
                        radius = 30f
                    ),
                    radius = 30f,
                    center = center
                )
                drawCircle(color = NeonPurple, radius = 8f, center = center)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "NEONDRIFT",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "PRECISION DRIFT ARCADE",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 3.sp,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
