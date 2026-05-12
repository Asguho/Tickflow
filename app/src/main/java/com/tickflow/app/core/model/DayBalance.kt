package com.tickflow.app.core.model

import java.time.LocalDate

data class DayBalance(
    val date: LocalDate,
    val targetMinutes: Int,
    val actualMinutes: Int,
    val carryInMinutes: Int,
    val isWorkday: Boolean,
    val manuallyAppliedOnNonWorkday: Boolean = false,
    val hasTimeRecord: Boolean = actualMinutes > 0,
) {
    val countsAsWorkday: Boolean = isWorkday || manuallyAppliedOnNonWorkday || hasTimeRecord

    val effectiveTargetMinutes: Int =
        if (countsAsWorkday) {
            (targetMinutes - carryInMinutes).coerceAtLeast(0)
        } else {
            0
        }

    val creditDeficitMinutes: Int =
        if (countsAsWorkday) {
            actualMinutes - effectiveTargetMinutes
        } else {
            0
        }
}
