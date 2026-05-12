package com.tickflow.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun HomeRoute(
    onSetup: () -> Unit,
    onSessions: () -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    HomeScreen(
        state = state,
        onStart = viewModel::startManualTracking,
        onStop = viewModel::stopTracking,
        onSetup = onSetup,
        onSessions = onSessions,
        onSettings = onSettings,
        onAcceptCorrection = viewModel::acceptCorrection,
        onRejectCorrection = viewModel::rejectCorrection,
    )
}
