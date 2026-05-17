package com.tickflow.app.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WorkProgressRing(
    progress: Float,
    tracking: Boolean,
    modifier: Modifier = Modifier,
) {
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val markerColor = MaterialTheme.colorScheme.primary
    val markerOutline = MaterialTheme.colorScheme.surface

    val pulse = if (tracking) {
        val infinite = rememberInfiniteTransition(label = "trackingPulse")
        infinite.animateFloat(
            initialValue = 0.85f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulseScale",
        ).value
    } else {
        1f
    }

    Canvas(
        modifier = modifier
            .size(232.dp)
            .semantics {
                contentDescription = "Workday progress"
                progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
            },
    ) {
        val strokeWidth = 22.dp.toPx()
        val radiusOffset = strokeWidth / 2
        val arcSize = size.copy(
            width = size.width - strokeWidth,
            height = size.height - strokeWidth,
        )
        val center = Offset(size.width / 2f, size.height / 2f)

        // Track
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(radiusOffset, radiusOffset),
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        if (progress > 0f) {
            val sweep = 360f * progress
            val gradient = Brush.sweepGradient(
                colorStops = arrayOf(
                    0.0f to primary,
                    0.55f to secondary,
                    1.0f to tertiary,
                ),
                center = center,
            )

            // Soft glow behind the active arc when tracking
            if (tracking) {
                drawArc(
                    brush = gradient,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(radiusOffset, radiusOffset),
                    size = arcSize,
                    alpha = 0.20f * pulse,
                    style = Stroke(width = strokeWidth * 1.7f, cap = StrokeCap.Round),
                )
            }

            drawArc(
                brush = gradient,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(radiusOffset, radiusOffset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Indicator dot at the end of the arc
            val angleRad = Math.toRadians((-90f + sweep).toDouble())
            val radius = (size.width - strokeWidth) / 2f
            val dotCenter = Offset(
                x = center.x + (radius * cos(angleRad)).toFloat(),
                y = center.y + (radius * sin(angleRad)).toFloat(),
            )
            drawCircle(
                color = markerOutline,
                radius = strokeWidth * 0.62f,
                center = dotCenter,
            )
            drawCircle(
                color = markerColor,
                radius = strokeWidth * 0.42f,
                center = dotCenter,
            )
        }
    }
}
