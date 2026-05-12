package com.tickflow.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tickflow.app.core.model.WorkSchedule
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tickflow_settings",
)

@Singleton
class SettingsDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            val targetMinutes = preferences[Keys.DAILY_TARGET_MINUTES] ?: 8 * 60
            val workdays = preferences[Keys.WORKDAYS]?.decodeWorkdays()
                ?: WorkSchedule().workdays
            AppSettings(
                schedule = WorkSchedule(
                    workdays = workdays,
                    dailyTargetMinutes = targetMinutes,
                    carryOverEnabled = preferences[Keys.CARRY_OVER_ENABLED] ?: true,
                ),
                officeWifi = preferences[Keys.OFFICE_WIFI_SSID]?.let { ssid ->
                    OfficeWifiIdentifier(
                        ssid = ssid,
                        bssid = preferences[Keys.OFFICE_WIFI_BSSID],
                    )
                },
                notificationsEnabled = preferences[Keys.NOTIFICATIONS_ENABLED] ?: true,
                rejectedCorrectionSuggestionIds = preferences[Keys.REJECTED_CORRECTION_IDS]
                    ?.decodeStringSet()
                    ?: emptySet(),
            )
        }

    suspend fun updateDailyTarget(minutes: Int) {
        context.settingsDataStore.edit { it[Keys.DAILY_TARGET_MINUTES] = minutes }
    }

    suspend fun updateWorkdays(workdays: Set<DayOfWeek>) {
        context.settingsDataStore.edit { it[Keys.WORKDAYS] = workdays.encode() }
    }

    suspend fun updateOfficeWifi(identifier: OfficeWifiIdentifier?) {
        context.settingsDataStore.edit { preferences ->
            if (identifier == null) {
                preferences.remove(Keys.OFFICE_WIFI_SSID)
                preferences.remove(Keys.OFFICE_WIFI_BSSID)
            } else {
                preferences[Keys.OFFICE_WIFI_SSID] = identifier.ssid
                identifier.bssid?.let { preferences[Keys.OFFICE_WIFI_BSSID] = it }
                    ?: preferences.remove(Keys.OFFICE_WIFI_BSSID)
            }
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun updateCarryOverEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.CARRY_OVER_ENABLED] = enabled }
    }

    suspend fun rejectCorrectionSuggestion(id: String) {
        context.settingsDataStore.edit { preferences ->
            val current = preferences[Keys.REJECTED_CORRECTION_IDS]?.decodeStringSet() ?: emptySet()
            preferences[Keys.REJECTED_CORRECTION_IDS] = (current + id).encodeStringSet()
        }
    }

    suspend fun clear() {
        context.settingsDataStore.edit { it.clear() }
    }

    private object Keys {
        val DAILY_TARGET_MINUTES = intPreferencesKey("daily_target_minutes")
        val WORKDAYS = stringPreferencesKey("workdays")
        val OFFICE_WIFI_SSID = stringPreferencesKey("office_wifi_ssid")
        val OFFICE_WIFI_BSSID = stringPreferencesKey("office_wifi_bssid")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val CARRY_OVER_ENABLED = booleanPreferencesKey("carry_over_enabled")
        val REJECTED_CORRECTION_IDS = stringPreferencesKey("rejected_correction_ids")
    }
}
