package com.tickflow.app.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickflow.app.core.model.CorrectionSuggestion
import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.model.TrackingState
import com.tickflow.app.core.time.ClockProvider
import com.tickflow.app.domain.repository.SettingsRepository
import com.tickflow.app.domain.repository.WorkSessionRepository
import com.tickflow.app.domain.service.PresenceDecisionEngine
import com.tickflow.app.domain.usecase.ApplySessionCorrectionUseCase
import com.tickflow.app.domain.usecase.CalculateCarryOverUseCase
import com.tickflow.app.domain.usecase.CalculateDayBalanceUseCase
import com.tickflow.app.domain.usecase.LeavePrediction
import com.tickflow.app.domain.usecase.PredictLeaveTimeUseCase
import com.tickflow.app.platform.service.TrackingForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val clockProvider: ClockProvider,
    private val settingsRepository: SettingsRepository,
    workSessionRepository: WorkSessionRepository,
    private val calculateDayBalance: CalculateDayBalanceUseCase,
    private val calculateCarryOver: CalculateCarryOverUseCase,
    private val predictLeaveTime: PredictLeaveTimeUseCase,
    private val presenceDecisionEngine: PresenceDecisionEngine,
    private val applySessionCorrection: ApplySessionCorrectionUseCase,
) : ViewModel() {
    private val zoneId = clockProvider.zoneId()
    private val today = LocalDate.now(zoneId)
    private val historyStart = today.minusDays(30).atStartOfDay(zoneId).toInstant()
    private val dayEnd = today.plusDays(1).atStartOfDay(zoneId).toInstant()

    val uiState = combine(
        settingsRepository.settings,
        workSessionRepository.observeActiveSession(),
        workSessionRepository.observeSessionsBetween(historyStart, dayEnd),
    ) { settings, activeSession, sessions ->
        val now = clockProvider.now()
        val trackingState = activeSession?.let { TrackingState.Tracking(it) } ?: TrackingState.Idle
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
        HomeUiState(
            setupComplete = settings.setupComplete,
            tracking = activeSession != null,
            workedMinutes = balance.actualMinutes,
            remainingMinutes = (balance.effectiveTargetMinutes - balance.actualMinutes).coerceAtLeast(0),
            targetMinutes = balance.effectiveTargetMinutes,
            carryInMinutes = balance.carryInMinutes,
            prediction = predictLeaveTime(balance, trackingState, now),
            correctionSuggestion = sessions
                .filter { session -> session.end != null }
                .sortedBy { session -> session.start }
                .zipWithNext()
                .asSequence()
                .mapNotNull { (previous, next) ->
                    presenceDecisionEngine.suggestReturnGap(
                        departedAt = previous.end ?: return@mapNotNull null,
                        returnedAt = next.start,
                    )
                }
                .firstOrNull { suggestion -> suggestion.id !in settings.rejectedCorrectionSuggestionIds },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun startManualTracking() {
        TrackingForegroundService.start(context, TrackingForegroundService.Action.StartManual)
    }

    fun stopTracking() {
        TrackingForegroundService.start(context, TrackingForegroundService.Action.Stop)
    }

    fun acceptCorrection(suggestion: CorrectionSuggestion) {
        viewModelScope.launch {
            runCatching {
                applySessionCorrection(suggestion, clockProvider.now())
            }.onFailure {
                settingsRepository.rejectCorrectionSuggestion(suggestion.id)
            }
        }
    }

    fun rejectCorrection(suggestion: CorrectionSuggestion) {
        viewModelScope.launch {
            settingsRepository.rejectCorrectionSuggestion(suggestion.id)
        }
    }
}

data class HomeUiState(
    val setupComplete: Boolean = false,
    val tracking: Boolean = false,
    val workedMinutes: Int = 0,
    val remainingMinutes: Int = 0,
    val targetMinutes: Int = 8 * 60,
    val carryInMinutes: Int = 0,
    val prediction: LeavePrediction = LeavePrediction.NotTracking(8 * 60),
    val correctionSuggestion: CorrectionSuggestion? = null,
) {
    val progress: Float = if (targetMinutes <= 0) 1f else (workedMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)
}
