package com.tickflow.app.domain.repository

import com.tickflow.app.data.datastore.AppSettings
import com.tickflow.app.data.datastore.OfficeWifiIdentifier
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun updateDailyTarget(minutes: Int)
    suspend fun updateWorkdays(workdays: Set<DayOfWeek>)
    suspend fun updateOfficeWifi(identifier: OfficeWifiIdentifier?)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateCarryOverEnabled(enabled: Boolean)
    suspend fun rejectCorrectionSuggestion(id: String)
    suspend fun clear()
}
