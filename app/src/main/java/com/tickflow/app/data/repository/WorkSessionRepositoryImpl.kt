package com.tickflow.app.data.repository

import androidx.room.withTransaction
import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.model.WorkSession
import com.tickflow.app.data.local.TickflowDatabase
import com.tickflow.app.data.local.entity.toDomain
import com.tickflow.app.data.local.entity.toEntity
import com.tickflow.app.domain.repository.WorkSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkSessionRepositoryImpl @Inject constructor(
    private val database: TickflowDatabase,
) : WorkSessionRepository {
    private val dao = database.workSessionDao()

    override fun observeSessionsBetween(start: Instant, end: Instant): Flow<List<WorkSession>> =
        dao.observeBetween(start, end).map { entities -> entities.map { it.toDomain() } }

    override fun observeActiveSession(): Flow<WorkSession?> =
        dao.observeActive().map { it?.toDomain() }

    override suspend fun getActiveSession(): WorkSession? = dao.getActive()?.toDomain()

    override suspend fun getAllSessions(): List<WorkSession> =
        dao.getAll().map { it.toDomain() }

    override suspend fun insert(session: WorkSession): Long =
        database.withTransaction {
            val overlapEnd = session.end ?: session.start.plusSeconds(366L * 24L * 60L * 60L)
            check(dao.countOverlaps(session.start, overlapEnd, session.id) == 0) {
                "work sessions must not overlap"
            }
            dao.insert(session.toEntity())
        }

    override suspend fun update(session: WorkSession) {
        database.withTransaction {
            val overlapEnd = session.end ?: session.start.plusSeconds(366L * 24L * 60L * 60L)
            check(dao.countOverlaps(session.start, overlapEnd, session.id) == 0) {
                "work sessions must not overlap"
            }
            dao.update(session.toEntity())
        }
    }

    override suspend fun delete(session: WorkSession) {
        dao.delete(session.toEntity())
    }

    override suspend fun startSession(start: Instant, source: SessionSource): WorkSession =
        database.withTransaction {
            dao.getActive()?.toDomain() ?: run {
                val session = WorkSession(start = start, source = source)
                val id = dao.insert(session.toEntity())
                session.copy(id = id)
            }
        }

    override suspend fun stopActiveSession(end: Instant): WorkSession? =
        database.withTransaction {
            val active = dao.getActive()?.toDomain() ?: return@withTransaction null
            if (!end.isAfter(active.start)) {
                dao.delete(active.toEntity())
                return@withTransaction null
            }
            val closed = active.copy(end = end)
            dao.update(closed.toEntity())
            closed
        }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }
}
