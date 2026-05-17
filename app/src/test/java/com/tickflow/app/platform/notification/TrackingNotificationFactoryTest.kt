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
        val targetCompleteChannel =
            notificationManager.getNotificationChannel(TrackingNotificationFactory.TARGET_COMPLETE_CHANNEL_ID)

        assertNotNull(channel)
        assertEquals(context.getString(R.string.tracking_channel_name), channel.name)
        assertEquals(NotificationManager.IMPORTANCE_LOW, channel.importance)
        assertNotNull(targetCompleteChannel)
        assertEquals(context.getString(R.string.target_complete_channel_name), targetCompleteChannel.name)
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, targetCompleteChannel.importance)
        assertEquals(null, targetCompleteChannel.sound)
        assertTrue(targetCompleteChannel.shouldVibrate())
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

    @Test
    fun targetCompleteNotificationHasNoActions() {
        val notification = factory.targetCompleteNotification()

        assertEquals(
            "Workday complete",
            notification.extras.getString(Notification.EXTRA_TITLE),
        )
        assertEquals(
            "You've reached your target for today.",
            notification.extras.getString(Notification.EXTRA_TEXT),
        )
        assertEquals(0, notification.actions?.size ?: 0)
        assertNotNull(notification.contentIntent)
    }
}
