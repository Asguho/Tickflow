package com.tickflow.app.core.model

import java.time.LocalDate

data class DayBalance(
    val date: LocalDate,
    val targetMinutes: Int,
    val actualMinutes: Int,
    val carryInMinutes: Int,
    val isWorkday: Boolean,
    val manuallyAppliedOnNonWorkday: Boolean = false,
) {
    val effectiveTargetMinutes: Int =
        if (isWorkday || manuallyAppliedOnNonWorkday) {
            (targetMinutes - carryInMinutes).coerceAtLeast(0)
        } else {
            0
        }

    val creditDeficitMinutes: Int =
        if (isWorkday || manuallyAppliedOnNonWorkday) {
            actualMinutes - effectiveTargetMinutes
        } else {
            0
        }
}
