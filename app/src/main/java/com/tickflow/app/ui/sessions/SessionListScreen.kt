package com.tickflow.app.ui.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tickflow.app.core.model.WorkSession
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SessionListScreen(
    state: SessionListUiState,
    onBack: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (WorkSession) -> Unit,
    onDelete: (WorkSession) -> Unit,
    onDismissEditor: () -> Unit,
    onDateChanged: (String) -> Unit,
    onStartChanged: (String) -> Unit,
    onEndChanged: (String) -> Unit,
    onSaveEditor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.editor.visible) {
        SessionEditorDialog(
            state = state.editor,
            onDismiss = onDismissEditor,
            onDateChanged = onDateChanged,
            onStartChanged = onStartChanged,
            onEndChanged = onEndChanged,
            onSave = onSaveEditor,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text("Back") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onPreviousDay) { Text("<") }
                    Text(
                        state.date.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = onNextDay) { Text(">") }
                }
                Button(onClick = onAdd) { Text("Add") }
            }
        },
    ) { padding ->
        if (state.sessions.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No sessions today", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("Manual and Wi-Fi sessions appear here.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(state.sessions, key = { it.id }) { session ->
                    SessionRow(
                        session = session,
                        onEdit = { onEdit(session) },
                        onDelete = { onDelete(session) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SessionRow(
    session: WorkSession,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("${formatTime(session.start)} - ${session.end?.let(::formatTime) ?: "Active"}")
            Text(
                "${formatDuration(session.duration(Instant.now()))} • ${session.source.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }
        Row {
            TextButton(onClick = onEdit, enabled = session.end != null) { Text("Edit") }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

@Composable
private fun SessionEditorDialog(
    state: SessionEditorUiState,
    onDismiss: () -> Unit,
    onDateChanged: (String) -> Unit,
    onStartChanged: (String) -> Unit,
    onEndChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.title) },
        text = {
            Column {
                OutlinedTextField(
                    value = state.dateText,
                    onValueChange = onDateChanged,
                    label = { Text("Date") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.startText,
                    onValueChange = onStartChanged,
                    label = { Text("Start") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.endText,
                    onValueChange = onEndChanged,
                    label = { Text("End") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                )
                state.error?.let { error ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

private fun formatTime(instant: Instant): String = TimeFormatter.format(instant)

private fun formatDuration(duration: Duration): String {
    val minutes = duration.toMinutes().coerceAtLeast(0)
    val hours = minutes / 60
    val remainder = minutes % 60
    return if (hours > 0) "${hours}h ${remainder}m" else "${remainder}m"
}
