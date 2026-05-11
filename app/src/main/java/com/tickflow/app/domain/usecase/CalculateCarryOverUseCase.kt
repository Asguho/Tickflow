package com.tickflow.app.domain.usecase

import com.tickflow.app.core.model.DayBalance
import javax.inject.Inject

class CalculateCarryOverUseCase @Inject constructor() {
    operator fun invoke(previousCarryInMinutes: Int, previousDay: DayBalance): Int {
        if (!previousDay.isWorkday && !previousDay.manuallyAppliedOnNonWorkday) {
            return previousCarryInMinutes
        }
        return previousDay.creditDeficitMinutes
    }
}
