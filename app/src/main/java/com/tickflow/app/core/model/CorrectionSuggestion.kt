package com.tickflow.app.core.model

import java.time.Instant

data class CorrectionSuggestion(
    val id: String,
    val gapStart: Instant,
    val gapEnd: Instant,
    val reason: String,
) {
    init {
        require(gapEnd > gapStart) { "correction gap must have positive duration" }
    }
}
