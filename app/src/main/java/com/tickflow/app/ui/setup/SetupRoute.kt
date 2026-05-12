package com.tickflow.app.ui.setup

import android.Manifest
import android.os.Build
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
            val permissions = buildList {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            permissionLauncher.launch(permissions.toTypedArray())
        },
        onSave = { ssid, bssid, targetHours ->
            viewModel.save(ssid, bssid, targetHours, onDone)
        },
    )
}
