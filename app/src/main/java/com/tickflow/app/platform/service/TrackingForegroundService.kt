package com.tickflow.app.platform.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.time.ClockProvider
import com.tickflow.app.domain.repository.SettingsRepository
import com.tickflow.app.domain.repository.WorkSessionRepository
import com.tickflow.app.domain.usecase.CalculateCarryOverUseCase
import com.tickflow.app.domain.usecase.CalculateDayBalanceUseCase
import com.tickflow.app.domain.usecase.StartTrackingUseCase
import com.tickflow.app.domain.usecase.StopTrackingUseCase
import com.tickflow.app.platform.notification.TrackingNotificationFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class TrackingForegroundService : Service() {
    @Inject lateinit var clockProvider: ClockProvider
    @Inject lateinit var repository: WorkSessionRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var calculateDayBalance: CalculateDayBalanceUseCase
    @Inject lateinit var calculateCarryOver: CalculateCarryOverUseCase
    @Inject lateinit var startTracking: StartTrackingUseCase
    @Inject lateinit var stopTracking: StopTrackingUseCase
    @Inject lateinit var notificationFactory: TrackingNotificationFactory

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var targetCompleteNotificationDate: LocalDate? = null

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
                notifyIfTargetReached()
                delay(60_000)
            }
            stopSelf()
        }
    }

    private suspend fun notifyIfTargetReached() {
        val now = clockProvider.now()
        val zoneId = clockProvider.zoneId()
        val today = LocalDate.now(zoneId)
        val settings = settingsRepository.settings.first()
        if (!settings.notificationsEnabled || targetCompleteNotificationDate == today) {
            return
        }

        val historyStart = today.minusDays(30).atStartOfDay(zoneId).toInstant()
        val dayEnd = today.plusDays(1).atStartOfDay(zoneId).toInstant()
        val sessions = repository.getAllSessions()
            .filter { session ->
                val sessionEnd = session.end ?: now
                session.start < dayEnd && sessionEnd > historyStart
            }

        val carryIn = generateSequence(today.minusDays(30)) { it.plusDays(1) }
            .takeWhile { it < today }
            .fold(0) { carry, date ->
                val previousBalance = calculateDayBalance(
                    date = date,
                    sessions = sessions,
                    schedule = settings.schedule,
                    carryInMinutes = carry,
                    zoneId = zoneId,
                    now = now,
                )
                calculateCarryOver(carry, previousBalance)
            }
        val balance = calculateDayBalance(
            date = today,
            sessions = sessions,
            schedule = settings.schedule,
            carryInMinutes = carryIn,
            zoneId = zoneId,
            now = now,
        )

        if (balance.effectiveTargetMinutes > 0 && balance.actualMinutes >= balance.effectiveTargetMinutes) {
            targetCompleteNotificationDate = today
            if (canPostNotification()) {
                NotificationManagerCompat.from(this).notify(
                    TrackingNotificationFactory.TARGET_COMPLETE_NOTIFICATION_ID,
                    notificationFactory.targetCompleteNotification(),
                )
            }
        }
    }

    private fun canPostNotification(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

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
