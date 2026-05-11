package com.tickflow.app.core.model

import java.time.DayOfWeek
import java.time.LocalDate

data class WorkSchedule(
    val workdays: Set<DayOfWeek> = setOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
    ),
    val dailyTargetMinutes: Int = 8 * 60,
    val carryOverEnabled: Boolean = true,
) {
    init {
        require(dailyTargetMinutes > 0) { "daily target must be positive" }
    }

    fun isWorkday(date: LocalDate): Boolean = date.dayOfWeek in workdays
}
