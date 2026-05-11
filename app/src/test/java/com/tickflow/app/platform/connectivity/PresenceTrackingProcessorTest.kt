package com.tickflow.app.platform.connectivity

import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.model.WorkSession
import com.tickflow.app.core.time.ClockProvider
import com.tickflow.app.domain.repository.WorkSessionRepository
import com.tickflow.app.domain.service.PresenceDecisionEngine
import com.tickflow.app.domain.usecase.StartTrackingUseCase
import com.tickflow.app.domain.usecase.StopTrackingUseCase
import com.tickflow.app.platform.service.TrackingForegroundService
import com.tickflow.app.platform.service.TrackingServiceController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class PresenceTrackingProcessorTest {
    private val repository = FakeWorkSessionRepository()
    private val serviceController = FakeTrackingServiceController()
    private val clockProvider = MutableClockProvider(Instant.parse("2026-05-11T08:00:00Z"))
    private val processor = PresenceTrackingProcessor(
        presenceDecisionEngine = PresenceDecisionEngine(),
        clockProvider = clockProvider,
        startTracking = StartTrackingUseCase(repository),
        stopTracking = StopTrackingUseCase(repository),
        serviceController = serviceController,
    )

    @Test
    fun repeatedArrivalCreatesOneSessionAndOneServiceStart() = runTest {
        processor.handle(WifiPresence("office"))
        processor.handle(WifiPresence("office"))

        assertEquals(1, repository.startCalls)
        assertEquals(0, repository.stopCalls)
        assertEquals(listOf(TrackingForegroundService.Action.StartWifi), serviceController.actions)
        assertEquals(SessionSource.Wifi, repository.activeSession?.source)
    }

    @Test
    fun departureStopsActiveSessionOnce() = runTest {
        processor.handle(WifiPresence("office"))
        clockProvider.instant = Instant.parse("2026-05-11T09:00:00Z")
        processor.handle(WifiPresence(null))
        processor.handle(WifiPresence(null))

        assertEquals(1, repository.startCalls)
        assertEquals(1, repository.stopCalls)
        assertEquals(
            listOf(
                TrackingForegroundService.Action.StartWifi,
                TrackingForegroundService.Action.Stop,
            ),
            serviceController.actions,
        )
        assertNull(repository.activeSession)
        assertEquals(1, repository.sessions.single().id)
        assertEquals(Instant.parse("2026-05-11T09:00:00Z"), repository.sessions.single().end)
    }

    private class MutableClockProvider(
        var instant: Instant,
    ) : ClockProvider {
        override val clock: Clock
            get() = Clock.fixed(instant, ZoneId.of("UTC"))
    }

    private class FakeTrackingServiceController : TrackingServiceController {
        val actions = mutableListOf<TrackingForegroundService.Action>()

        override fun start(action: TrackingForegroundService.Action): Boolean {
            actions += action
            return true
        }
    }

    private class FakeWorkSessionRepository : WorkSessionRepository {
        private val activeFlow = MutableStateFlow<WorkSession?>(null)
        val sessions = mutableListOf<WorkSession>()
        var activeSession: WorkSession?
            get() = activeFlow.value
            private set(value) {
                activeFlow.value = value
            }
        var startCalls = 0
        var stopCalls = 0

        override fun observeSessionsBetween(start: Instant, end: Instant): Flow<List<WorkSession>> =
            MutableStateFlow(sessions)

        override fun observeActiveSession(): Flow<WorkSession?> = activeFlow

        override suspend fun getActiveSession(): WorkSession? = activeSession

        override suspend fun getAllSessions(): List<WorkSession> = sessions

        override suspend fun insert(session: WorkSession): Long {
            val id = sessions.size + 1L
            sessions += session.copy(id = id)
            return id
        }

        override suspend fun update(session: WorkSession) {
            sessions.replaceAll { existing -> if (existing.id == session.id) session else existing }
        }

        override suspend fun delete(session: WorkSession) {
            sessions.removeAll { it.id == session.id }
        }

        override suspend fun startSession(start: Instant, source: SessionSource): WorkSession {
            startCalls += 1
            return activeSession ?: WorkSession(
                id = sessions.size + 1L,
                start = start,
                source = source,
            ).also { activeSession = it }
        }

        override suspend fun stopActiveSession(end: Instant): WorkSession? {
            stopCalls += 1
            val active = activeSession ?: return null
            val closed = active.copy(end = end)
            sessions += closed
            activeSession = null
            return closed
        }

        override suspend fun deleteAll() {
            sessions.clear()
            activeSession = null
        }
    }
}
