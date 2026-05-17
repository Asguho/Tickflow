package com.tickflow.app.platform.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import com.tickflow.app.MainActivity
import com.tickflow.app.R
import com.tickflow.app.core.model.WorkSession
import com.tickflow.app.platform.service.TrackingForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingNotificationFactory @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val notificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.tracking_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.tracking_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun activeTrackingNotification(
        session: WorkSession,
        now: Instant,
        targetMinutes: Int,
    ): Notification {
        val safeTarget = targetMinutes.coerceAtLeast(1)
        val workedMinutes = Duration.between(session.start, now).toMinutes()
            .coerceAtLeast(0)
            .toInt()
        val progressPercent = ((workedMinutes.toLong() * 100L) / safeTarget)
            .coerceIn(0L, 100L)
            .toInt()
        val remainingMinutes = (safeTarget - workedMinutes).coerceAtLeast(0)
        val targetReached = workedMinutes >= safeTarget

        val stopIntent = PendingIntent.getService(
            context,
            10,
            TrackingForegroundService.intent(context, TrackingForegroundService.Action.Stop),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val contentIntent = PendingIntent.getActivity(
            context,
            11,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val trackerIcon = Icon.createWithResource(context, R.drawable.ic_launcher_foreground)
        val progressStyle = Notification.ProgressStyle()
            .setProgress(progressPercent)
            .setProgressTrackerIcon(trackerIcon)

        val subText = if (targetReached) "Target reached" else "${formatMinutes(remainingMinutes.toLong())} to go"

        return Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tickflow tracking workday")
            .setContentText(
                "${formatMinutes(workedMinutes.toLong())} of ${formatMinutes(safeTarget.toLong())} worked",
            )
            .setSubText(subText)
            .setStyle(progressStyle)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(context, R.drawable.ic_launcher_foreground),
                    "Stop",
                    stopIntent,
                ).build(),
            )
            .build()
    }

    private fun formatMinutes(minutes: Long): String {
        val hours = minutes / 60
        val remainder = minutes % 60
        return if (hours > 0) "${hours}h ${remainder}m" else "${remainder}m"
    }

    companion object {
        const val CHANNEL_ID = "active_tracking"
        const val NOTIFICATION_ID = 1001
    }
}
