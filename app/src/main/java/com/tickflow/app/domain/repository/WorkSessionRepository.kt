package com.tickflow.app.domain.repository

import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.model.WorkSession
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface WorkSessionRepository {
    fun observeSessionsBetween(start: Instant, end: Instant): Flow<List<WorkSession>>
    fun observeActiveSession(): Flow<WorkSession?>
    suspend fun getActiveSession(): WorkSession?
    suspend fun getAllSessions(): List<WorkSession>
    suspend fun insert(session: WorkSession): Long
    suspend fun update(session: WorkSession)
    suspend fun delete(session: WorkSession)
    suspend fun startSession(start: Instant, source: SessionSource): WorkSession
    suspend fun stopActiveSession(end: Instant): WorkSession?
    suspend fun deleteAll()
}
