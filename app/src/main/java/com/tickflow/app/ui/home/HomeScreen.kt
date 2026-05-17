package com.tickflow.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.WatchLater
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tickflow.app.core.model.CorrectionSuggestion
import com.tickflow.app.domain.usecase.LeavePrediction
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onSetup: () -> Unit,
    onSessions: () -> Unit,
    onSettings: () -> Unit,
    onAcceptCorrection: (CorrectionSuggestion) -> Unit,
    onRejectCorrection: (CorrectionSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Tickflow",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onSessions,
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Sessions",
                        )
                    }
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = onSettings,
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        if (!state.setupComplete) {
            SetupPrompt(
                onSetup = onSetup,
                contentPadding = padding,
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))

            HeroProgress(state = state)

            Spacer(Modifier.height(28.dp))

            PredictionCard(
                prediction = state.prediction,
                remainingMinutes = state.remainingMinutes,
                tracking = state.tracking,
            )

            Spacer(Modifier.height(20.dp))

            TrackingActionButton(
                tracking = state.tracking,
                onStart = onStart,
                onStop = onStop,
            )

            Spacer(Modifier.height(20.dp))

            AnimatedVisibility(
                visible = state.correctionSuggestion != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                state.correctionSuggestion?.let { suggestion ->
                    CorrectionReviewCard(
                        suggestion = suggestion,
                        onAccept = { onAcceptCorrection(suggestion) },
                        onReject = { onRejectCorrection(suggestion) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            DayStatsRow(
                workedMinutes = state.workedMinutes,
                targetMinutes = state.targetMinutes,
                carryInMinutes = state.carryInMinutes,
            )
        }
    }
}

@Composable
private fun HeroProgress(state: HomeUiState) {
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        label = "workProgress",
    )
    Box(contentAlignment = Alignment.Center) {
        WorkProgressRing(
            progress = animatedProgress,
            tracking = state.tracking,
            modifier = Modifier.size(232.dp),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatMinutes(state.workedMinutes),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "worked today",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            ProgressPill(
                tracking = state.tracking,
                percent = (state.progress * 100).toInt(),
            )
        }
    }
}

@Composable
private fun ProgressPill(tracking: Boolean, percent: Int) {
    val container = if (tracking) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val onContainer = if (tracking) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = container,
        contentColor = onContainer,
        shape = RoundedCornerShape(50),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (tracking) Icons.Outlined.AutoAwesome else Icons.Rounded.WatchLater,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (tracking) "Tracking · $percent%" else "$percent% of day",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun PredictionCard(
    prediction: LeavePrediction,
    remainingMinutes: Int,
    tracking: Boolean,
) {
    val icon: ImageVector = when (prediction) {
        is LeavePrediction.Complete -> Icons.Outlined.Check
        is LeavePrediction.LeaveAt -> Icons.Outlined.NightsStay
        is LeavePrediction.NotTracking -> Icons.Outlined.Coffee
    }
    val containerColor = when (prediction) {
        is LeavePrediction.Complete -> MaterialTheme.colorScheme.secondaryContainer
        is LeavePrediction.LeaveAt ->
            if (tracking) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh
        is LeavePrediction.NotTracking -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val onContainerColor = when (prediction) {
        is LeavePrediction.Complete -> MaterialTheme.colorScheme.onSecondaryContainer
        is LeavePrediction.LeaveAt ->
            if (tracking) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        is LeavePrediction.NotTracking -> MaterialTheme.colorScheme.onSurface
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = onContainerColor,
        ),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = onContainerColor.copy(alpha = 0.10f),
                contentColor = onContainerColor,
                modifier = Modifier.size(52.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = predictionText(prediction),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = when (prediction) {
                        is LeavePrediction.Complete -> "You hit your target today"
                        is LeavePrediction.LeaveAt -> "${formatMinutes(remainingMinutes)} to go"
                        is LeavePrediction.NotTracking -> "Tap start to begin your day"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainerColor.copy(alpha = 0.78f),
                )
            }
        }
    }
}

@Composable
private fun TrackingActionButton(
    tracking: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    if (tracking) {
        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.Reject)
                onStop()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.Stop,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Stop tracking",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    } else {
        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                onStart()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Start work",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CorrectionReviewCard(
    suggestion: CorrectionSuggestion,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Review a gap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${formatTime(suggestion.gapStart)} – ${formatTime(suggestion.gapEnd)} · ${
                    formatDuration(
                        suggestion.gapStart,
                        suggestion.gapEnd,
                    )
                }",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(16.dp))
            Row {
                FilledTonalButton(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Mark as work")
                }
                Spacer(Modifier.width(10.dp))
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Ignore")
                }
            }
        }
    }
}

@Composable
private fun DayStatsRow(
    workedMinutes: Int,
    targetMinutes: Int,
    carryInMinutes: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatTile(
            modifier = Modifier.weight(1f),
            label = "Target",
            value = formatMinutes(targetMinutes),
        )
        StatTile(
            modifier = Modifier.weight(1f),
            label = "Worked",
            value = formatMinutes(workedMinutes),
        )
        StatTile(
            modifier = Modifier.weight(1f),
            label = "Carry-over",
            value = signedMinutes(carryInMinutes),
        )
    }
}

@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SetupPrompt(
    onSetup: () -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(96.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.RocketLaunch,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Set up work tracking",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Add your work Wi-Fi and target schedule to start local automatic tracking.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onSetup,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.RocketLaunch,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Start setup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun predictionText(prediction: LeavePrediction): String =
    when (prediction) {
        is LeavePrediction.Complete -> "Done for today"
        is LeavePrediction.LeaveAt -> "Leave at ${
            DateTimeFormatter.ofPattern("HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(prediction.at)
        }"
        is LeavePrediction.NotTracking -> "Ready when you are"
    }

private fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val remainder = minutes % 60
    return if (hours > 0) "${hours}h ${remainder}m" else "${remainder}m"
}

private fun signedMinutes(minutes: Int): String =
    when {
        minutes > 0 -> "+${formatMinutes(minutes)}"
        minutes < 0 -> "-${formatMinutes(-minutes)}"
        else -> "0m"
    }

private val HomeTimeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

private fun formatTime(instant: java.time.Instant): String = HomeTimeFormatter.format(instant)

private fun formatDuration(start: java.time.Instant, end: java.time.Instant): String =
    formatMinutes(Duration.between(start, end).toMinutes().toInt())
