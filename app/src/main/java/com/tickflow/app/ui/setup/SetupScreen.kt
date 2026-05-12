package com.tickflow.app.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tickflow.app.core.permissions.PermissionExplainer

@Composable
fun SetupScreen(
    onRequestPermissions: () -> Unit,
    onSave: (ssid: String, bssid: String?, targetHours: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var ssid by remember { mutableStateOf("") }
    var bssid by remember { mutableStateOf("") }
    var targetHoursText by remember { mutableStateOf("8") }

    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Work setup", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                PermissionExplainer.WIFI_LOCATION_RATIONALE,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRequestPermissions, modifier = Modifier.fillMaxWidth()) {
                Text("Allow Wi-Fi detection")
            }
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = ssid,
                onValueChange = { ssid = it },
                label = { Text("Office Wi-Fi name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = bssid,
                onValueChange = { bssid = it },
                label = { Text("BSSID, optional") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = targetHoursText,
                onValueChange = { targetHoursText = it.filter(Char::isDigit).take(2) },
                label = { Text("Daily target hours") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onSave(ssid, bssid, targetHoursText.toIntOrNull() ?: 8) },
                enabled = ssid.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save setup")
            }
        }
    }
}
