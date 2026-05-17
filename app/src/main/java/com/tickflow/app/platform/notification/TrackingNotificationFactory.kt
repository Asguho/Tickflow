package com.tickflow.app.platform.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.tracking_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.tracking_channel_description)
            }
            NotificationManagerCompat.from(context).createNotificationChannel(channel)

            val targetCompleteChannel = NotificationChannel(
                TARGET_COMPLETE_CHANNEL_ID,
                context.getString(R.string.target_complete_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.target_complete_channel_description)
                setSound(null, null)
                enableVibration(true)
                vibrationPattern = TARGET_COMPLETE_VIBRATION_PATTERN
                setBypassDnd(false)
            }
            NotificationManagerCompat.from(context).createNotificationChannel(targetCompleteChannel)
        }
    }

    fun activeTrackingNotification(session: WorkSession, now: Instant): Notification {
        val workedMinutes = Duration.between(session.start, now).toMinutes().coerceAtLeast(0)
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

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tickflow tracking workday")
            .setContentText("${formatMinutes(workedMinutes)} worked")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Stop",
                stopIntent,
            )
            .build()
    }

    fun targetCompleteNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            context,
            12,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(context, TARGET_COMPLETE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Workday complete")
            .setContentText("You've reached your target for today.")
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setVibrate(TARGET_COMPLETE_VIBRATION_PATTERN)
            .setContentIntent(contentIntent)
            .build()
    }

    private fun formatMinutes(minutes: Long): String {
        val hours = minutes / 60
        val remainder = minutes % 60
        return if (hours > 0) "${hours}h ${remainder}m" else "${remainder}m"
    }

    companion object {
        const val CHANNEL_ID = "active_tracking"
        const val TARGET_COMPLETE_CHANNEL_ID = "target_complete"
        const val NOTIFICATION_ID = 1001
        const val TARGET_COMPLETE_NOTIFICATION_ID = 1002
        private val TARGET_COMPLETE_VIBRATION_PATTERN = longArrayOf(0, 120, 80, 120)
    }
}
