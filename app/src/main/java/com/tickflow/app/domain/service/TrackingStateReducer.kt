package com.tickflow.app.domain.service

import com.tickflow.app.core.model.PresenceEvent
import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.model.TrackingState
import com.tickflow.app.core.model.WorkSession
import javax.inject.Inject

class TrackingStateReducer @Inject constructor() {
    fun reduce(current: TrackingState, event: PresenceEvent): TrackingState {
        return when (event) {
            is PresenceEvent.Arrival -> {
                when (current) {
                    is TrackingState.Tracking -> current
                    else -> TrackingState.Tracking(
                        WorkSession(
                            start = event.occurredAt,
                            source = SessionSource.Wifi,
                            confidence = 95,
                        ),
                    )
                }
            }
            is PresenceEvent.Departure -> {
                when (current) {
                    is TrackingState.Tracking -> TrackingState.Paused(
                        pausedAt = event.occurredAt,
                        lastSession = current.session.copy(end = event.occurredAt),
                    )
                    else -> current
                }
            }
            is PresenceEvent.ManualStart -> {
                when (current) {
                    is TrackingState.Tracking -> current
                    else -> TrackingState.Tracking(
                        WorkSession(
                            start = event.occurredAt,
                            source = SessionSource.Manual,
                            confidence = 100,
                        ),
                    )
                }
            }
            is PresenceEvent.ManualStop -> {
                when (current) {
                    is TrackingState.Tracking -> TrackingState.Paused(
                        pausedAt = event.occurredAt,
                        lastSession = current.session.copy(end = event.occurredAt),
                    )
                    else -> current
                }
            }
            is PresenceEvent.Recovery -> {
                event.activeSession?.let { TrackingState.Tracking(it) } ?: TrackingState.Idle
            }
            is PresenceEvent.CorrectionAccepted -> TrackingState.Idle
            is PresenceEvent.CorrectionRejected -> TrackingState.Idle
        }
    }
}
