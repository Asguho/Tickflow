package com.tickflow.app.ui.sessions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SessionListRoute(
    onBack: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    SessionListScreen(
        state = state,
        onBack = onBack,
        onPreviousDay = viewModel::previousDay,
        onNextDay = viewModel::nextDay,
        onAdd = viewModel::openCreateEditor,
        onEdit = viewModel::openEditEditor,
        onDelete = viewModel::delete,
        onDismissEditor = viewModel::dismissEditor,
        onDateChanged = viewModel::updateEditorDate,
        onStartChanged = viewModel::updateEditorStart,
        onEndChanged = viewModel::updateEditorEnd,
        onSaveEditor = viewModel::saveEditor,
    )
}
