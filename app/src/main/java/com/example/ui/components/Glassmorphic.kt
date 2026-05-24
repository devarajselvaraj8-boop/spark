package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.math.sin

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(CustomSurface)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

@Composable
fun NeonText(
    text: String,
    style: TextStyle,
    glowColor: Color = CyberCyan,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = style.copy(
            shadow = Shadow(
                color = glowColor.copy(alpha = 0.8f),
                offset = Offset(0f, 0f),
                blurRadius = 12f
            )
        ),
        modifier = modifier,
        textAlign = textAlign
    )
}

@Composable
fun NeonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glowColor: Color = CyberCyan,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = glowColor,
            contentColor = DeepDarkBackground
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White, glowColor)
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        content = content
    )
}

@Composable
fun AnimatedWaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 18,
    color: Color = CyberCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val animValues = (0 until barCount).map { i ->
        if (isPlaying) {
            infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 1.0f,
                animationSpec = infiniteTransitionSpec(i),
                label = "bar_$i"
            )
        } else {
            remember { mutableStateOf(0.15f) }
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = 4.dp.toPx()
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = (width - totalSpacing) / barCount

        for (i in 0 until barCount) {
            val progress = animValues[i].value
            val barHeight = height * progress
            val x = i * (barWidth + spacing)
            val y = height - barHeight

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.5f),
                        color
                    )
                ),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

private fun infiniteTransitionSpec(index: Int): InfiniteRepeatableSpec<Float> {
    val duration = (400..900).random()
    return infiniteRepeatable(
        animation = tween(
            durationMillis = duration,
            delayMillis = index * 20,
            easing = FastOutSlowInEasing
        ),
        repeatMode = RepeatMode.Reverse
    )
}
