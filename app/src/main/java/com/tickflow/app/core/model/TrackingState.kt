package com.tickflow.app.core.model

import java.time.Instant

sealed interface TrackingState {
    data object Idle : TrackingState

    data class Tracking(
        val session: WorkSession,
    ) : TrackingState

    data class Paused(
        val pausedAt: Instant,
        val lastSession: WorkSession?,
    ) : TrackingState

    data class Uncertain(
        val message: String,
        val since: Instant,
    ) : TrackingState

    data class NeedsReview(
        val suggestion: CorrectionSuggestion,
    ) : TrackingState
}
