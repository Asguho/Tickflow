package com.tickflow.app.platform.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tickflow.app.core.time.ClockProvider
import com.tickflow.app.data.local.TickflowDatabase
import com.tickflow.app.data.local.entity.toEntity
import com.tickflow.app.domain.repository.SettingsRepository
import com.tickflow.app.domain.repository.WorkSessionRepository
import com.tickflow.app.domain.usecase.CalculateCarryOverUseCase
import com.tickflow.app.domain.usecase.CalculateDayBalanceUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class MidnightRolloverWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val database: TickflowDatabase,
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: WorkSessionRepository,
    private val clockProvider: ClockProvider,
    private val calculateDayBalance: CalculateDayBalanceUseCase,
    private val calculateCarryOver: CalculateCarryOverUseCase,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return runCatching {
            val zoneId = clockProvider.zoneId()
            val today = LocalDate.now(zoneId)
            val startDate = today.minusDays(30)
            val historyStart = startDate.atStartOfDay(zoneId).toInstant()
            val tomorrowStart = today.plusDays(1).atStartOfDay(zoneId).toInstant()
            val settings = settingsRepository.settings.first()
            val sessions = sessionRepository.observeSessionsBetween(historyStart, tomorrowStart).first()
            val balances = mutableListOf<com.tickflow.app.core.model.DayBalance>()
            var carry = 0

            generateSequence(startDate) { it.plusDays(1) }
                .takeWhile { it <= today }
                .forEach { date ->
                    val balance = calculateDayBalance(
                        date = date,
                        sessions = sessions,
                        schedule = settings.schedule,
                        carryInMinutes = carry,
                        zoneId = zoneId,
                        now = clockProvider.now(),
                    )
                    balances += balance
                    carry = calculateCarryOver(carry, balance)
                }

            balances.takeLast(2).forEach { balance ->
                database.dayBalanceDao().upsert(balance.toEntity())
            }
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }
}
