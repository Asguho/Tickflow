package com.tickflow.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tickflow.app.data.datastore.OfficeWifiIdentifier
import com.tickflow.app.data.datastore.SettingsDataStore
import com.tickflow.app.data.repository.SettingsRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.DayOfWeek

@RunWith(RobolectricTestRunner::class)
class SettingsRepositoryImplTest {
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = SettingsRepositoryImpl(SettingsDataStore(context))
        repository.clear()
    }

    @After
    fun tearDown() = runTest {
        repository.clear()
    }

    @Test
    fun defaultsRepresentUnconfiguredWeekdaySchedule() = runTest {
        val settings = repository.settings.first()

        assertEquals(480, settings.schedule.dailyTargetMinutes)
        assertEquals(
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
            ),
            settings.schedule.workdays,
        )
        assertTrue(settings.schedule.carryOverEnabled)
        assertTrue(settings.notificationsEnabled)
        assertEquals(emptySet<String>(), settings.rejectedCorrectionSuggestionIds)
        assertNull(settings.officeWifi)
        assertFalse(settings.setupComplete)
    }

    @Test
    fun updatesPersistScheduleWifiAndNotificationSettings() = runTest {
        repository.updateDailyTarget(420)
        repository.updateWorkdays(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        repository.updateOfficeWifi(OfficeWifiIdentifier(ssid = "Office", bssid = "aa:bb:cc:dd:ee:ff"))
        repository.updateNotificationsEnabled(false)
        repository.updateCarryOverEnabled(false)
        repository.rejectCorrectionSuggestion("gap-1")
        repository.rejectCorrectionSuggestion("gap-2")

        val settings = repository.settings.first()

        assertEquals(420, settings.schedule.dailyTargetMinutes)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), settings.schedule.workdays)
        assertEquals("Office", settings.officeWifi?.ssid)
        assertEquals("aa:bb:cc:dd:ee:ff", settings.officeWifi?.bssid)
        assertFalse(settings.notificationsEnabled)
        assertFalse(settings.schedule.carryOverEnabled)
        assertEquals(setOf("gap-1", "gap-2"), settings.rejectedCorrectionSuggestionIds)
        assertTrue(settings.setupComplete)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonPositiveDailyTarget() = runTest {
        repository.updateDailyTarget(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsEmptyWorkdays() = runTest {
        repository.updateWorkdays(emptySet())
    }

    @Test
    fun clearResetsSettingsToDefaults() = runTest {
        repository.updateDailyTarget(360)
        repository.updateOfficeWifi(OfficeWifiIdentifier(ssid = "Office"))

        repository.clear()

        val settings = repository.settings.first()
        assertEquals(480, settings.schedule.dailyTargetMinutes)
        assertNull(settings.officeWifi)
    }
}
