package com.tickflow.app.domain

import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.model.WorkSession
import com.tickflow.app.domain.repository.WorkSessionRepository
import com.tickflow.app.domain.usecase.ExportSessionsCsvUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class ExportSessionsCsvUseCaseTest {
    @Test
    fun csvExportEscapesNotesAndFormatsLocalOffset() {
        val csv = ExportSessionsCsvUseCase(repository = EmptyRepository)
            .buildCsv(
                sessions = listOf(
                    WorkSession(
                        id = 42,
                        start = Instant.parse("2026-05-11T08:00:00Z"),
                        end = Instant.parse("2026-05-11T09:30:00Z"),
                        source = SessionSource.Edited,
                        confidence = 90,
                        corrected = true,
                        editedAt = Instant.parse("2026-05-11T10:00:00Z"),
                        note = "Office, \"meeting\"",
                    ),
                ),
                zoneId = ZoneId.of("Europe/Copenhagen"),
            )

        assertEquals(
            "id,start,end,source,confidence,corrected,edited_at,note\n" +
                "42,2026-05-11T10:00:00+02:00,2026-05-11T11:30:00+02:00,Edited,90,true,2026-05-11T12:00:00+02:00,\"Office, \"\"meeting\"\"\"\n",
            csv,
        )
    }

    private object EmptyRepository : WorkSessionRepository {
        override fun observeSessionsBetween(start: Instant, end: Instant): Flow<List<WorkSession>> = emptyFlow()
        override fun observeActiveSession(): Flow<WorkSession?> = emptyFlow()
        override suspend fun getActiveSession(): WorkSession? = null
        override suspend fun getAllSessions(): List<WorkSession> = emptyList()
        override suspend fun insert(session: WorkSession): Long = error("not used")
        override suspend fun update(session: WorkSession) = error("not used")
        override suspend fun delete(session: WorkSession) = error("not used")
        override suspend fun startSession(start: Instant, source: SessionSource): WorkSession = error("not used")
        override suspend fun stopActiveSession(end: Instant): WorkSession? = error("not used")
        override suspend fun deleteAll() = error("not used")
    }
}
