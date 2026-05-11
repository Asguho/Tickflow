package com.tickflow.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tickflow.app.core.model.CorrectionSuggestion
import com.tickflow.app.domain.usecase.LeavePrediction
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Tickflow", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Row {
                    TextButton(onClick = onSessions) { Text("Sessions") }
                    TextButton(onClick = onSettings) { Text("Settings") }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!state.setupComplete) {
                SetupPrompt(onSetup = onSetup)
                return@Column
            }

            Box(contentAlignment = Alignment.Center) {
                WorkProgressRing(progress = state.progress)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatMinutes(state.workedMinutes), style = MaterialTheme.typography.headlineMedium)
                    Text("worked today", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(28.dp))
            Text(
                text = predictionText(state.prediction),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${formatMinutes(state.remainingMinutes)} remaining",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
            Spacer(Modifier.height(28.dp))

            if (state.tracking) {
                Button(onClick = onStop, modifier = Modifier.fillMaxWidth()) {
                    Text("Stop tracking")
                }
            } else {
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                    Text("Start work")
                }
            }

            Spacer(Modifier.height(16.dp))
            state.correctionSuggestion?.let { suggestion ->
                CorrectionReview(
                    suggestion = suggestion,
                    onAccept = { onAcceptCorrection(suggestion) },
                    onReject = { onRejectCorrection(suggestion) },
                )
                Spacer(Modifier.height(16.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SummaryMetric(label = "Target", value = formatMinutes(state.targetMinutes))
                SummaryMetric(label = "Carry-over", value = signedMinutes(state.carryInMinutes))
            }
        }
    }
}

@Composable
private fun CorrectionReview(
    suggestion: CorrectionSuggestion,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider()
        Spacer(Modifier.height(14.dp))
        Text("Review gap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "${formatTime(suggestion.gapStart)} - ${formatTime(suggestion.gapEnd)} (${formatDuration(suggestion.gapStart, suggestion.gapEnd)})",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(10.dp))
        Row {
            Button(onClick = onAccept) { Text("Mark as work") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onReject) { Text("Ignore") }
        }
    }
}

@Composable
private fun SetupPrompt(onSetup: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Set up work tracking", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text(
            "Add your work Wi-Fi and target schedule to start local automatic tracking.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSetup) { Text("Start setup") }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
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
