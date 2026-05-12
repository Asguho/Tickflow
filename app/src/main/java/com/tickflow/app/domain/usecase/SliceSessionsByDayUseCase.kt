package com.tickflow.app.domain.usecase

import com.tickflow.app.core.model.WorkSession
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class SliceSessionsByDayUseCase @Inject constructor() {
    operator fun invoke(
        sessions: List<WorkSession>,
        zoneId: ZoneId,
        now: Instant,
    ): List<SessionDaySlice> {
        return sessions.flatMap { session ->
            sliceSession(session, zoneId, now)
        }
    }

    private fun sliceSession(
        session: WorkSession,
        zoneId: ZoneId,
        now: Instant,
    ): List<SessionDaySlice> {
        val end = session.end ?: now
        require(end >= session.start) { "session end must not be before start" }
        if (end == session.start) return emptyList()

        val slices = mutableListOf<SessionDaySlice>()
        var cursor = session.start
        while (cursor < end) {
            val date = LocalDate.ofInstant(cursor, zoneId)
            val nextDayStart = date.plusDays(1).atStartOfDay(zoneId).toInstant()
            val sliceEnd = minOf(end, nextDayStart)
            val minutes = Duration.between(cursor, sliceEnd).toMinutes().toInt()
            if (minutes > 0) {
                slices += SessionDaySlice(date, minutes)
            }
            cursor = sliceEnd
        }
        return slices
    }
}
