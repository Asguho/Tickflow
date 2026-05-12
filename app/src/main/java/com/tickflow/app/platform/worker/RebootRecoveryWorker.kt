package com.tickflow.app.platform.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tickflow.app.domain.repository.WorkSessionRepository
import com.tickflow.app.platform.service.TrackingForegroundService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RebootRecoveryWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: WorkSessionRepository,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (repository.getActiveSession() != null) {
            TrackingForegroundService.start(context, TrackingForegroundService.Action.Recover)
        }
        return Result.success()
    }
}
