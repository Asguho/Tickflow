package com.tickflow.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.model.WorkSession
import com.tickflow.app.data.local.TickflowDatabase
import com.tickflow.app.data.repository.WorkSessionRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class WorkSessionRepositoryImplTest {
    private lateinit var database: TickflowDatabase
    private lateinit var repository: WorkSessionRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TickflowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WorkSessionRepositoryImpl(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndQuerySessionBetweenRangeReturnsDomainModel() = runTest {
        val session = WorkSession(
            start = Instant.parse("2026-05-11T08:00:00Z"),
            end = Instant.parse("2026-05-11T10:00:00Z"),
            source = SessionSource.Manual,
            note = "remote",
        )

        val id = repository.insert(session)

        repository.observeSessionsBetween(
            start = Instant.parse("2026-05-11T00:00:00Z"),
            end = Instant.parse("2026-05-12T00:00:00Z"),
        ).test {
            val sessions = awaitItem()
            assertEquals(1, sessions.size)
            assertEquals(id, sessions.single().id)
            assertEquals(SessionSource.Manual, sessions.single().source)
            assertEquals("remote", sessions.single().note)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun startSessionIsIdempotentWhileActiveAndCanRecoverActiveSession() = runTest {
        val first = repository.startSession(
            start = Instant.parse("2026-05-11T08:00:00Z"),
            source = SessionSource.Wifi,
        )
        val second = repository.startSession(
            start = Instant.parse("2026-05-11T08:05:00Z"),
            source = SessionSource.Wifi,
        )

        assertEquals(first.id, second.id)
        assertNotNull(repository.getActiveSession())

        repository.observeActiveSession().test {
            assertEquals(first.id, awaitItem()?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun stopActiveSessionClosesSessionAndClearsActiveLookup() = runTest {
        val active = repository.startSession(
            start = Instant.parse("2026-05-11T08:00:00Z"),
            source = SessionSource.Manual,
        )

        val closed = repository.stopActiveSession(Instant.parse("2026-05-11T09:00:00Z"))

        assertEquals(active.id, closed?.id)
        assertEquals(Instant.parse("2026-05-11T09:00:00Z"), closed?.end)
        assertNull(repository.getActiveSession())
    }

    @Test
    fun stopActiveSessionAtStartDeletesZeroLengthSession() = runTest {
        repository.startSession(
            start = Instant.parse("2026-05-11T08:00:00Z"),
            source = SessionSource.Wifi,
        )

        val closed = repository.stopActiveSession(Instant.parse("2026-05-11T08:00:00Z"))

        assertNull(closed)
        assertNull(repository.getActiveSession())
        repository.observeSessionsBetween(
            start = Instant.parse("2026-05-11T00:00:00Z"),
            end = Instant.parse("2026-05-12T00:00:00Z"),
        ).test {
            assertEquals(emptyList<WorkSession>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun overlappingClosedSessionsAreRejected() = runTest {
        repository.insert(
            WorkSession(
                start = Instant.parse("2026-05-11T08:00:00Z"),
                end = Instant.parse("2026-05-11T10:00:00Z"),
                source = SessionSource.Manual,
            ),
        )

        val result = runCatching {
            repository.insert(
                WorkSession(
                    start = Instant.parse("2026-05-11T09:00:00Z"),
                    end = Instant.parse("2026-05-11T11:00:00Z"),
                    source = SessionSource.Manual,
                ),
            )
        }

        assertTrue(result.isFailure)
    }

    @Test
    fun updateSessionRejectsOverlapButAllowsSelfUpdate() = runTest {
        val firstId = repository.insert(
            WorkSession(
                start = Instant.parse("2026-05-11T08:00:00Z"),
                end = Instant.parse("2026-05-11T09:00:00Z"),
                source = SessionSource.Manual,
            ),
        )
        repository.insert(
            WorkSession(
                start = Instant.parse("2026-05-11T10:00:00Z"),
                end = Instant.parse("2026-05-11T11:00:00Z"),
                source = SessionSource.Manual,
            ),
        )

        repository.update(
            WorkSession(
                id = firstId,
                start = Instant.parse("2026-05-11T08:15:00Z"),
                end = Instant.parse("2026-05-11T09:15:00Z"),
                source = SessionSource.Edited,
                corrected = true,
            ),
        )

        val overlappingUpdate = runCatching {
            repository.update(
                WorkSession(
                    id = firstId,
                    start = Instant.parse("2026-05-11T10:15:00Z"),
                    end = Instant.parse("2026-05-11T10:45:00Z"),
                    source = SessionSource.Edited,
                ),
            )
        }

        assertTrue(overlappingUpdate.isFailure)
    }

    @Test
    fun deleteAllClearsSessions() = runTest {
        repository.insert(
            WorkSession(
                start = Instant.parse("2026-05-11T08:00:00Z"),
                end = Instant.parse("2026-05-11T09:00:00Z"),
                source = SessionSource.Manual,
            ),
        )

        repository.deleteAll()

        repository.observeSessionsBetween(
            start = Instant.parse("2026-05-11T00:00:00Z"),
            end = Instant.parse("2026-05-12T00:00:00Z"),
        ).test {
            assertEquals(emptyList<WorkSession>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
