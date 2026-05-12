package com.tickflow.app.domain.usecase

import com.tickflow.app.core.model.DayBalance
import com.tickflow.app.core.model.TrackingState
import java.time.Instant
import javax.inject.Inject

class PredictLeaveTimeUseCase @Inject constructor() {
    operator fun invoke(
        dayBalance: DayBalance,
        trackingState: TrackingState,
        now: Instant,
    ): LeavePrediction {
        val remainingMinutes = dayBalance.effectiveTargetMinutes - dayBalance.actualMinutes
        if (remainingMinutes <= 0) {
            return LeavePrediction.Complete(overageMinutes = -remainingMinutes)
        }

        return when (trackingState) {
            is TrackingState.Tracking -> LeavePrediction.LeaveAt(
                at = now.plusSeconds(remainingMinutes * 60L),
                remainingMinutes = remainingMinutes,
            )
            TrackingState.Idle,
            is TrackingState.NeedsReview,
            is TrackingState.Paused,
            is TrackingState.Uncertain,
            -> LeavePrediction.NotTracking(remainingMinutes = remainingMinutes)
        }
    }
}

sealed interface LeavePrediction {
    data class LeaveAt(
        val at: Instant,
        val remainingMinutes: Int,
    ) : LeavePrediction

    data class Complete(
        val overageMinutes: Int,
    ) : LeavePrediction

    data class NotTracking(
        val remainingMinutes: Int,
    ) : LeavePrediction
}
