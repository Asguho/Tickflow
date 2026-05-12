package com.tickflow.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.tickflow.app.platform.connectivity.PresenceTrackingCoordinator
import com.tickflow.app.platform.worker.MaintenanceScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TickflowApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var presenceTrackingCoordinator: PresenceTrackingCoordinator
    @Inject lateinit var maintenanceScheduler: MaintenanceScheduler

    override fun onCreate() {
        super.onCreate()
        presenceTrackingCoordinator.start()
        maintenanceScheduler.schedule()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
