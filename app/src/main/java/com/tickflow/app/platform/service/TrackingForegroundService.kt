package com.tickflow.app.platform.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.time.ClockProvider
import com.tickflow.app.domain.repository.WorkSessionRepository
import com.tickflow.app.domain.usecase.StartTrackingUseCase
import com.tickflow.app.domain.usecase.StopTrackingUseCase
import com.tickflow.app.platform.notification.TrackingNotificationFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TrackingForegroundService : Service() {
    @Inject lateinit var clockProvider: ClockProvider
    @Inject lateinit var repository: WorkSessionRepository
    @Inject lateinit var startTracking: StartTrackingUseCase
    @Inject lateinit var stopTracking: StopTrackingUseCase
    @Inject lateinit var notificationFactory: TrackingNotificationFactory

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        notificationFactory.ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra(EXTRA_ACTION)?.let(Action::valueOf) ?: Action.Recover) {
            Action.StartManual -> handleStart(SessionSource.Manual)
            Action.StartWifi -> handleStart(SessionSource.Wifi)
            Action.Stop -> handleStop()
            Action.Recover -> handleRecover()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun handleStart(source: SessionSource) {
        scope.launch {
            val session = startTracking(clockProvider.now(), source)
            startForeground(
                TrackingNotificationFactory.NOTIFICATION_ID,
                notificationFactory.activeTrackingNotification(session, clockProvider.now()),
            )
            maintainNotification()
        }
    }

    private fun handleStop() {
        scope.launch {
            stopTracking(clockProvider.now())
            ServiceCompat.stopForeground(this@TrackingForegroundService, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun handleRecover() {
        scope.launch {
            val active = repository.getActiveSession()
            if (active == null) {
                stopSelf()
            } else {
                startForeground(
                    TrackingNotificationFactory.NOTIFICATION_ID,
                    notificationFactory.activeTrackingNotification(active, clockProvider.now()),
                )
                maintainNotification()
            }
        }
    }

    private fun maintainNotification() {
        scope.launch {
            while (true) {
                val active = repository.getActiveSession() ?: break
                startForeground(
                    TrackingNotificationFactory.NOTIFICATION_ID,
                    notificationFactory.activeTrackingNotification(active, clockProvider.now()),
                )
                delay(60_000)
            }
            stopSelf()
        }
    }

    enum class Action {
        StartManual,
        StartWifi,
        Stop,
        Recover,
    }

    companion object {
        private const val EXTRA_ACTION = "action"

        fun intent(context: Context, action: Action): Intent =
            Intent(context, TrackingForegroundService::class.java)
                .putExtra(EXTRA_ACTION, action.name)

        fun start(context: Context, action: Action): Boolean {
            return runCatching {
                ContextCompat.startForegroundService(context, intent(context, action))
            }.isSuccess
        }
    }
}
