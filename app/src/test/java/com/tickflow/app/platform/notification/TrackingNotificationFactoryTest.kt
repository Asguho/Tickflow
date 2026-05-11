package com.tickflow.app.platform.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tickflow.app.R
import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.model.WorkSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TrackingNotificationFactoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val factory = TrackingNotificationFactory(context)

    @Test
    fun ensureChannelCreatesLowImportanceTrackingChannel() {
        factory.ensureChannel()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channel = notificationManager.getNotificationChannel(TrackingNotificationFactory.CHANNEL_ID)

        assertNotNull(channel)
        assertEquals(context.getString(R.string.tracking_channel_name), channel.name)
        assertEquals(NotificationManager.IMPORTANCE_LOW, channel.importance)
    }

    @Test
    fun activeNotificationShowsWorkedTimeAndStopAction() {
        val notification = factory.activeTrackingNotification(
            session = WorkSession(
                start = Instant.parse("2026-05-11T08:00:00Z"),
                source = SessionSource.Manual,
            ),
            now = Instant.parse("2026-05-11T14:23:00Z"),
        )

        assertEquals(
            "Tickflow tracking workday",
            notification.extras.getString(Notification.EXTRA_TITLE),
        )
        assertEquals(
            "6h 23m worked",
            notification.extras.getString(Notification.EXTRA_TEXT),
        )
        assertTrue(notification.flags and Notification.FLAG_ONGOING_EVENT != 0)
        assertEquals(1, notification.actions.size)
        assertEquals("Stop", notification.actions.single().title.toString())
        assertNotNull(notification.contentIntent)
    }
}
