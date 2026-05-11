package com.tickflow.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun WorkProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val track = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    val active = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = modifier
            .size(196.dp)
            .semantics {
                contentDescription = "Workday progress"
                progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
            },
    ) {
        val strokeWidth = 18.dp.toPx()
        val radiusOffset = strokeWidth / 2
        drawArc(
            color = track,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(radiusOffset, radiusOffset),
            size = size.copy(width = size.width - strokeWidth, height = size.height - strokeWidth),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
        drawArc(
            color = active,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(radiusOffset, radiusOffset),
            size = size.copy(width = size.width - strokeWidth, height = size.height - strokeWidth),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}
