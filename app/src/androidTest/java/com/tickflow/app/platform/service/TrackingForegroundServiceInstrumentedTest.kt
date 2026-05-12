package com.tickflow.app.platform.service

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.tickflow.app.MainActivity
import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.model.WorkSession
import com.tickflow.app.domain.repository.WorkSessionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackingForegroundServiceInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private lateinit var repository: WorkSessionRepository
    private var originalSessions: List<WorkSession> = emptyList()

    @Before
    fun setUp() = runBlocking {
        repository = EntryPointAccessors
            .fromApplication(context, TestEntryPoint::class.java)
            .workSessionRepository()
        originalSessions = repository.getAllSessions()
        stopService()
        repository.deleteAll()
        grantNotificationPermission()
        launchApp()
    }

    @After
    fun tearDown() = runBlocking {
        stopService()
        repository.deleteAll()
        originalSessions.forEach { session ->
            repository.insert(session.copy(id = 0))
        }
        if (originalSessions.any { it.isActive }) {
            TrackingForegroundService.start(context, TrackingForegroundService.Action.Recover)
        }
        device.pressHome()
    }

    @Test
    fun startManualTrackingAndStopFromNotificationAction() = runBlocking {
        assertTrue(TrackingForegroundService.start(context, TrackingForegroundService.Action.StartManual))

        val active = waitForActiveSession()
        assertEquals(SessionSource.Manual, active.source)
        waitForNotification("Tickflow tracking workday")
        assertTrue(notificationDump().contains("\"Stop\""))

        openNotificationAndTapStop()

        waitUntilSuspending(timeoutMs = 5_000) { repository.getActiveSession() == null }
        assertNull(repository.getActiveSession())
        waitUntil(timeoutMs = 5_000) { !notificationDump().contains("Tickflow tracking workday") }
    }

    @Test
    fun recoverPromotesExistingActiveSessionToForegroundNotification() = runBlocking {
        repository.startSession(Instant.now().minusSeconds(120), SessionSource.Wifi)

        assertTrue(TrackingForegroundService.start(context, TrackingForegroundService.Action.Recover))

        val active = waitForActiveSession()
        assertEquals(SessionSource.Wifi, active.source)
        val notification = waitForNotification("Tickflow tracking workday")
        assertTrue(notification.contains("2m worked") || notification.contains("1m worked") || notification.contains("0m worked"))
        assertTrue(serviceDump().contains("isForeground=true"))
    }

    private fun grantNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            device.executeShellCommand("pm grant ${context.packageName} ${Manifest.permission.POST_NOTIFICATIONS}")
        }
    }

    private fun launchApp() {
        context.startActivity(
            android.content.Intent(context, MainActivity::class.java)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        device.wait(Until.hasObject(By.pkg(context.packageName).depth(0)), 5_000)
    }

    private fun stopService() {
        TrackingForegroundService.start(context, TrackingForegroundService.Action.Stop)
        waitUntil(timeoutMs = 5_000) { !notificationDump().contains("Tickflow tracking workday") }
    }

    private suspend fun waitForActiveSession(): WorkSession {
        waitUntilSuspending(timeoutMs = 5_000) { repository.getActiveSession() != null }
        return requireNotNull(repository.getActiveSession())
    }

    private fun waitForNotification(text: String): String {
        waitUntil(timeoutMs = 5_000) { notificationDump().contains(text) }
        return notificationDump()
    }

    private fun openNotificationAndTapStop() {
        device.openNotification()
        assertTrue(device.wait(Until.hasObject(By.text("Tickflow tracking workday")), 5_000))
        device.findObject(By.text("Tickflow tracking workday"))?.click()
        assertTrue(device.wait(Until.hasObject(By.text("Stop")), 5_000))
        val stop = device.findObject(By.text("Stop"))
        assertNotNull(stop)
        stop!!.click()
    }

    private fun notificationDump(): String =
        device.executeShellCommand("dumpsys notification --noredact")

    private fun serviceDump(): String =
        device.executeShellCommand("dumpsys activity services ${context.packageName}")

    private fun waitUntil(timeoutMs: Long, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        var lastError: Throwable? = null
        while (System.currentTimeMillis() < deadline) {
            try {
                if (condition()) return
            } catch (error: Throwable) {
                lastError = error
            }
            Thread.sleep(100)
        }
        lastError?.let { throw it }
        error("Condition was not met within ${timeoutMs}ms")
    }

    private suspend fun waitUntilSuspending(timeoutMs: Long, condition: suspend () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        var lastError: Throwable? = null
        while (System.currentTimeMillis() < deadline) {
            try {
                if (condition()) return
            } catch (error: Throwable) {
                lastError = error
            }
            delay(100)
        }
        lastError?.let { throw it }
        error("Condition was not met within ${timeoutMs}ms")
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TestEntryPoint {
        fun workSessionRepository(): WorkSessionRepository
    }
}
