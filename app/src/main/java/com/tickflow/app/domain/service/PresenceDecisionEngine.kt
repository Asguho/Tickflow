package com.tickflow.app.domain.service

import com.tickflow.app.core.model.CorrectionSuggestion
import com.tickflow.app.core.model.PresenceEvent
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class PresenceDecisionEngine @Inject constructor() {
    fun eventForWifiState(
        connectedOfficeWifiKey: String?,
        previousOfficeWifiKey: String?,
        occurredAt: Instant,
    ): PresenceEvent? {
        return when {
            connectedOfficeWifiKey != null && previousOfficeWifiKey == null ->
                PresenceEvent.Arrival(occurredAt, connectedOfficeWifiKey)
            connectedOfficeWifiKey == null && previousOfficeWifiKey != null ->
                PresenceEvent.Departure(occurredAt, previousOfficeWifiKey)
            else -> null
        }
    }

    fun suggestReturnGap(
        departedAt: Instant,
        returnedAt: Instant,
        maxReviewableGap: Duration = Duration.ofHours(3),
    ): CorrectionSuggestion? {
        val gap = Duration.between(departedAt, returnedAt)
        return if (!gap.isNegative && !gap.isZero && gap <= maxReviewableGap) {
            CorrectionSuggestion(
                id = "${departedAt.toEpochMilli()}-${returnedAt.toEpochMilli()}",
                gapStart = departedAt,
                gapEnd = returnedAt,
                reason = "You returned to the office after a short gap.",
            )
        } else {
            null
        }
    }
}
