package com.tickflow.app.core.model

import java.time.Duration
import java.time.Instant

data class WorkSession(
    val id: Long = 0,
    val start: Instant,
    val end: Instant? = null,
    val source: SessionSource,
    val confidence: Int = 100,
    val corrected: Boolean = false,
    val editedAt: Instant? = null,
    val note: String? = null,
) {
    init {
        require(confidence in 0..100) { "confidence must be between 0 and 100" }
        require(end == null || end > start) { "session end must be after start" }
    }

    val isActive: Boolean = end == null

    fun duration(now: Instant): Duration {
        val resolvedEnd = end ?: now
        require(resolvedEnd >= start) { "duration cannot be calculated before session start" }
        return Duration.between(start, resolvedEnd)
    }
}
