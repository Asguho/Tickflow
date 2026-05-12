package com.tickflow.app.domain.usecase

import com.tickflow.app.core.model.DayBalance
import com.tickflow.app.core.model.WorkSchedule
import com.tickflow.app.core.model.WorkSession
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class CalculateDayBalanceUseCase @Inject constructor(
    private val sliceSessionsByDay: SliceSessionsByDayUseCase,
) {
    operator fun invoke(
        date: LocalDate,
        sessions: List<WorkSession>,
        schedule: WorkSchedule,
        carryInMinutes: Int,
        zoneId: ZoneId,
        now: Instant,
        manuallyAppliedOnNonWorkday: Boolean = false,
    ): DayBalance {
        val dayStart = date.atStartOfDay(zoneId).toInstant()
        val dayEnd = date.plusDays(1).atStartOfDay(zoneId).toInstant()
        val hasTimeRecord = sessions.any { session ->
            val sessionEnd = session.end ?: now
            session.start < dayEnd && sessionEnd > dayStart && sessionEnd > session.start
        }
        val daySlices = sliceSessionsByDay(sessions, zoneId, now)
            .filter { it.date == date }
        val actualMinutes = daySlices.sumOf { it.minutes }

        return DayBalance(
            date = date,
            targetMinutes = schedule.dailyTargetMinutes,
            actualMinutes = actualMinutes,
            carryInMinutes = if (schedule.carryOverEnabled) carryInMinutes else 0,
            isWorkday = schedule.isWorkday(date),
            manuallyAppliedOnNonWorkday = manuallyAppliedOnNonWorkday,
            hasTimeRecord = hasTimeRecord,
        )
    }
}
