package com.tickflow.app.ui.sessions

import androidx.compose.runtime.Composable

@Composable
fun SessionEditorRoute() {
    SessionListScreen(
        state = SessionListUiState(),
        onBack = {},
        onPreviousDay = {},
        onNextDay = {},
        onAdd = {},
        onEdit = {},
        onDelete = {},
        onDismissEditor = {},
        onDateChanged = {},
        onStartChanged = {},
        onEndChanged = {},
        onSaveEditor = {},
    )
}
