package com.tickflow.app.data.repository

import com.tickflow.app.data.datastore.AppSettings
import com.tickflow.app.data.datastore.OfficeWifiIdentifier
import com.tickflow.app.data.datastore.SettingsDataStore
import com.tickflow.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore,
) : SettingsRepository {
    override val settings: Flow<AppSettings> = dataStore.settings

    override suspend fun updateDailyTarget(minutes: Int) {
        require(minutes > 0) { "daily target must be positive" }
        dataStore.updateDailyTarget(minutes)
    }

    override suspend fun updateWorkdays(workdays: Set<DayOfWeek>) {
        require(workdays.isNotEmpty()) { "at least one workday is required" }
        dataStore.updateWorkdays(workdays)
    }

    override suspend fun updateOfficeWifi(identifier: OfficeWifiIdentifier?) {
        dataStore.updateOfficeWifi(identifier)
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.updateNotificationsEnabled(enabled)
    }

    override suspend fun updateCarryOverEnabled(enabled: Boolean) {
        dataStore.updateCarryOverEnabled(enabled)
    }

    override suspend fun rejectCorrectionSuggestion(id: String) {
        require(id.isNotBlank()) { "correction suggestion id must not be blank" }
        dataStore.rejectCorrectionSuggestion(id)
    }

    override suspend fun clear() {
        dataStore.clear()
    }
}
