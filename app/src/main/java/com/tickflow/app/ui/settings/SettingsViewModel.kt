package com.tickflow.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickflow.app.core.time.ClockProvider
import com.tickflow.app.data.datastore.AppSettings
import com.tickflow.app.data.datastore.OfficeWifiIdentifier
import com.tickflow.app.domain.repository.SettingsRepository
import com.tickflow.app.domain.repository.WorkSessionRepository
import com.tickflow.app.domain.usecase.ExportSessionsCsvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: WorkSessionRepository,
    private val exportSessionsCsv: ExportSessionsCsvUseCase,
    private val clockProvider: ClockProvider,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun updateTargetHours(hours: Int) {
        viewModelScope.launch { settingsRepository.updateDailyTarget(hours.coerceAtLeast(1) * 60) }
    }

    fun updateWifi(ssid: String, bssid: String?) {
        viewModelScope.launch {
            settingsRepository.updateOfficeWifi(
                OfficeWifiIdentifier(ssid.trim(), bssid?.trim()?.takeIf { it.isNotBlank() }),
            )
        }
    }

    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateNotificationsEnabled(enabled) }
    }

    fun updateCarryOver(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateCarryOverEnabled(enabled) }
    }

    fun deleteLocalData() {
        viewModelScope.launch {
            sessionRepository.deleteAll()
            settingsRepository.clear()
        }
    }

    fun exportLocalData(onReady: (String) -> Unit) {
        viewModelScope.launch {
            onReady(exportSessionsCsv(clockProvider.zoneId()))
        }
    }
}
