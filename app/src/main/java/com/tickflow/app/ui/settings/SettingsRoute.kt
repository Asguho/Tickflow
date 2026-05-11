package com.tickflow.app.ui.settings

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    SettingsScreen(
        settings = settings,
        onBack = onBack,
        onTargetHours = viewModel::updateTargetHours,
        onWifi = viewModel::updateWifi,
        onNotifications = viewModel::updateNotifications,
        onCarryOver = viewModel::updateCarryOver,
        onExportData = {
            viewModel.exportLocalData { csv ->
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "Tickflow sessions export")
                    putExtra(Intent.EXTRA_TEXT, csv)
                }
                context.startActivity(
                    Intent.createChooser(sendIntent, "Export Tickflow data"),
                )
            }
        },
        onDeleteData = viewModel::deleteLocalData,
    )
}
