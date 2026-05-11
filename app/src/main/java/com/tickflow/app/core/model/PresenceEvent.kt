package com.tickflow.app.core.model

import java.time.Instant

sealed interface PresenceEvent {
    val occurredAt: Instant

    data class Arrival(
        override val occurredAt: Instant,
        val wifiKey: String,
    ) : PresenceEvent

    data class Departure(
        override val occurredAt: Instant,
        val wifiKey: String?,
    ) : PresenceEvent

    data class ManualStart(
        override val occurredAt: Instant,
    ) : PresenceEvent

    data class ManualStop(
        override val occurredAt: Instant,
    ) : PresenceEvent

    data class Recovery(
        override val occurredAt: Instant,
        val activeSession: WorkSession?,
    ) : PresenceEvent

    data class CorrectionAccepted(
        override val occurredAt: Instant,
        val suggestion: CorrectionSuggestion,
    ) : PresenceEvent

    data class CorrectionRejected(
        override val occurredAt: Instant,
        val suggestionId: String,
    ) : PresenceEvent
}
