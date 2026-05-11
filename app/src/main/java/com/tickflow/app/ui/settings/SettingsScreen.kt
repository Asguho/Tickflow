package com.tickflow.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tickflow.app.data.datastore.AppSettings

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onTargetHours: (Int) -> Unit,
    onWifi: (String, String?) -> Unit,
    onNotifications: (Boolean) -> Unit,
    onCarryOver: (Boolean) -> Unit,
    onExportData: () -> Unit,
    onDeleteData: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var ssid by remember { mutableStateOf("") }
    var bssid by remember { mutableStateOf("") }
    var targetHours by remember { mutableStateOf("8") }

    LaunchedEffect(settings) {
        ssid = settings.officeWifi?.ssid.orEmpty()
        bssid = settings.officeWifi?.bssid.orEmpty()
        targetHours = (settings.schedule.dailyTargetMinutes / 60).toString()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text("Back") }
                Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp),
        ) {
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
                value = targetHours,
                onValueChange = { targetHours = it.filter(Char::isDigit).take(2) },
                label = { Text("Daily target hours") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onWifi(ssid, bssid)
                    onTargetHours(targetHours.toIntOrNull() ?: 8)
                },
                enabled = ssid.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save settings")
            }
            Spacer(Modifier.height(20.dp))
            SettingsSwitchRow(
                label = "Notifications",
                checked = settings.notificationsEnabled,
                onCheckedChange = onNotifications,
            )
            SettingsSwitchRow(
                label = "Carry-over",
                checked = settings.schedule.carryOverEnabled,
                onCheckedChange = onCarryOver,
            )
            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))
            Text("Privacy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Tickflow stores sessions, schedule, Wi-Fi identifiers, and correction decisions locally on this device. Export uses the Android share sheet and is only started by you.",
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onExportData, modifier = Modifier.fillMaxWidth()) {
                Text("Export sessions CSV")
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDeleteData) {
                Text("Delete local Tickflow data")
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
