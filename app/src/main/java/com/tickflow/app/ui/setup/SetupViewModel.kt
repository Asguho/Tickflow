package com.tickflow.app.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickflow.app.data.datastore.OfficeWifiIdentifier
import com.tickflow.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    fun save(
        ssid: String,
        bssid: String?,
        targetHours: Int,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            settingsRepository.updateOfficeWifi(
                OfficeWifiIdentifier(ssid = ssid.trim(), bssid = bssid?.trim()?.takeIf { it.isNotBlank() }),
            )
            settingsRepository.updateDailyTarget((targetHours.coerceAtLeast(1)) * 60)
            onDone()
        }
    }
}
