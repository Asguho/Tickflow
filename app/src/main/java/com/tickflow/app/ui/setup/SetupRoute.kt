package com.tickflow.app.ui.setup

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SetupRoute(
    onDone: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { }
    SetupScreen(
        onRequestPermissions = {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS,
                ),
            )
        },
        onSave = { ssid, bssid, targetHours ->
            viewModel.save(ssid, bssid, targetHours, onDone)
        },
    )
}
