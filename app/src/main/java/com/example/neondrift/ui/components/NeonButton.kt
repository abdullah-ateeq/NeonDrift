package com.example.neondrift.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neondrift.ui.theme.NeonCyan
import com.example.neondrift.ui.theme.NeonCyanGlow
import com.example.neondrift.ui.theme.NeonPurple

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    accentColor: Color = NeonCyan,
    height: Dp = 56.dp,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "btnScale"
    )

    if (isPrimary) {
        Button(
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = Color(0xFF090B10)
            ),
            contentPadding = PaddingValues(horizontal = 24.dp),
            modifier = modifier
                .height(height)
                .scale(scale)
        ) {
            Text(
                text = text.uppercase(),
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontSize = 15.sp
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.85f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = accentColor.copy(alpha = 0.08f),
                contentColor = accentColor
            ),
            contentPadding = PaddingValues(horizontal = 24.dp),
            modifier = modifier
                .height(height)
                .scale(scale)
        ) {
            Text(
                text = text.uppercase(),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp,
                fontSize = 14.sp
            )
        }
    }
}
