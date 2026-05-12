package com.tickflow.app.platform.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tickflow.app.core.time.ClockProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaintenanceScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val clockProvider: ClockProvider,
) {
    fun schedule() {
        val now = clockProvider.now()
        val zoneId = clockProvider.zoneId()
        val nextRun = LocalDate.now(zoneId)
            .plusDays(1)
            .atStartOfDay(zoneId)
            .plusMinutes(5)
            .toInstant()
        val initialDelayMillis = Duration.between(now, nextRun).toMillis().coerceAtLeast(0)
        val request = PeriodicWorkRequestBuilder<MidnightRolloverWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "midnight_rollover",
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
