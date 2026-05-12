package com.tickflow.app.domain

import com.tickflow.app.core.model.PresenceEvent
import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.model.TrackingState
import com.tickflow.app.core.model.WorkSchedule
import com.tickflow.app.core.model.WorkSession
import com.tickflow.app.domain.service.PresenceDecisionEngine
import com.tickflow.app.domain.service.TrackingStateReducer
import com.tickflow.app.domain.usecase.CalculateCarryOverUseCase
import com.tickflow.app.domain.usecase.CalculateDayBalanceUseCase
import com.tickflow.app.domain.usecase.LeavePrediction
import com.tickflow.app.domain.usecase.PredictLeaveTimeUseCase
import com.tickflow.app.domain.usecase.SliceSessionsByDayUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TimeEngineTest {
    private val zone = ZoneId.of("Europe/Copenhagen")
    private val slicer = SliceSessionsByDayUseCase()
    private val balance = CalculateDayBalanceUseCase(slicer)

    @Test
    fun closedSessionDurationUsesEndInstant() {
        val session = WorkSession(
            start = Instant.parse("2026-05-11T08:00:00Z"),
            end = Instant.parse("2026-05-11T10:30:00Z"),
            source = SessionSource.Manual,
        )

        assertEquals(Duration.ofMinutes(150), session.duration(Instant.parse("2026-05-11T12:00:00Z")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidEndBeforeStartIsRejected() {
        WorkSession(
            start = Instant.parse("2026-05-11T10:00:00Z"),
            end = Instant.parse("2026-05-11T09:00:00Z"),
            source = SessionSource.Manual,
        )
    }

    @Test
    fun sessionCrossingMidnightIsSlicedIntoBothDays() {
        val session = WorkSession(
            start = Instant.parse("2026-05-11T21:30:00Z"),
            end = Instant.parse("2026-05-12T01:15:00Z"),
            source = SessionSource.Manual,
        )

        val slices = slicer(listOf(session), ZoneId.of("UTC"), Instant.parse("2026-05-12T01:15:00Z"))

        assertEquals(2, slices.size)
        assertEquals(LocalDate.parse("2026-05-11"), slices[0].date)
        assertEquals(150, slices[0].minutes)
        assertEquals(LocalDate.parse("2026-05-12"), slices[1].date)
        assertEquals(75, slices[1].minutes)
    }

    @Test
    fun daylightSavingSpringForwardDurationUsesRealElapsedTime() {
        val session = WorkSession(
            start = Instant.parse("2026-03-29T00:30:00Z"),
            end = Instant.parse("2026-03-29T02:30:00Z"),
            source = SessionSource.Manual,
        )

        val day = balance(
            date = LocalDate.parse("2026-03-29"),
            sessions = listOf(session),
            schedule = WorkSchedule(workdays = setOf(DayOfWeek.SUNDAY), dailyTargetMinutes = 480),
            carryInMinutes = 0,
            zoneId = zone,
            now = Instant.parse("2026-03-29T02:30:00Z"),
        )

        assertEquals(120, day.actualMinutes)
    }

    @Test
    fun carryOverCreditReducesNextEffectiveTarget() {
        val day = com.tickflow.app.core.model.DayBalance(
            date = LocalDate.parse("2026-05-11"),
            targetMinutes = 480,
            actualMinutes = 540,
            carryInMinutes = 0,
            isWorkday = true,
        )

        val carry = CalculateCarryOverUseCase()(0, day)
        val next = day.copy(date = LocalDate.parse("2026-05-12"), actualMinutes = 0, carryInMinutes = carry)

        assertEquals(60, carry)
        assertEquals(420, next.effectiveTargetMinutes)
    }

    @Test
    fun carryOverIgnoresWorkdayWithoutTimeRecord() {
        val day = com.tickflow.app.core.model.DayBalance(
            date = LocalDate.parse("2026-05-11"),
            targetMinutes = 480,
            actualMinutes = 0,
            carryInMinutes = 0,
            isWorkday = true,
        )

        val carry = CalculateCarryOverUseCase()(0, day)

        assertEquals(0, carry)
    }

    @Test
    fun carryOverKeepsExistingDeficitAcrossWorkdayWithoutTimeRecord() {
        val day = com.tickflow.app.core.model.DayBalance(
            date = LocalDate.parse("2026-05-11"),
            targetMinutes = 480,
            actualMinutes = 0,
            carryInMinutes = -120,
            isWorkday = true,
        )

        val carry = CalculateCarryOverUseCase()(-120, day)

        assertEquals(-120, carry)
    }

    @Test
    fun trackedTimeOnUnscheduledDayCountsAsWorkday() {
        val session = WorkSession(
            start = Instant.parse("2026-05-16T08:00:00Z"),
            end = Instant.parse("2026-05-16T12:00:00Z"),
            source = SessionSource.Manual,
        )

        val day = balance(
            date = LocalDate.parse("2026-05-16"),
            sessions = listOf(session),
            schedule = WorkSchedule(
                workdays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                dailyTargetMinutes = 480,
            ),
            carryInMinutes = 0,
            zoneId = ZoneId.of("UTC"),
            now = Instant.parse("2026-05-16T12:00:00Z"),
        )
        val carry = CalculateCarryOverUseCase()(0, day)

        assertTrue(day.hasTimeRecord)
        assertTrue(day.countsAsWorkday)
        assertEquals(480, day.effectiveTargetMinutes)
        assertEquals(-240, day.creditDeficitMinutes)
        assertEquals(-240, carry)
    }

    @Test
    fun unscheduledDayWithoutTrackedTimeDoesNotConsumeCarryOver() {
        val day = balance(
            date = LocalDate.parse("2026-05-16"),
            sessions = emptyList(),
            schedule = WorkSchedule(
                workdays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                dailyTargetMinutes = 480,
            ),
            carryInMinutes = -120,
            zoneId = ZoneId.of("UTC"),
            now = Instant.parse("2026-05-16T12:00:00Z"),
        )
        val carry = CalculateCarryOverUseCase()(-120, day)

        assertEquals(0, day.effectiveTargetMinutes)
        assertEquals(0, day.creditDeficitMinutes)
        assertEquals(-120, carry)
    }

    @Test
    fun subMinuteSessionStillMarksDayAsHavingTimeRecord() {
        val session = WorkSession(
            start = Instant.parse("2026-05-11T08:00:00Z"),
            end = Instant.parse("2026-05-11T08:00:30Z"),
            source = SessionSource.Manual,
        )

        val day = balance(
            date = LocalDate.parse("2026-05-11"),
            sessions = listOf(session),
            schedule = WorkSchedule(workdays = setOf(DayOfWeek.MONDAY), dailyTargetMinutes = 480),
            carryInMinutes = 0,
            zoneId = ZoneId.of("UTC"),
            now = Instant.parse("2026-05-11T08:00:30Z"),
        )

        assertEquals(0, day.actualMinutes)
        assertTrue(day.hasTimeRecord)
    }

    @Test
    fun predictiveLeaveTimeAddsRemainingMinutesWhenTracking() {
        val now = Instant.parse("2026-05-11T12:00:00Z")
        val day = com.tickflow.app.core.model.DayBalance(
            date = LocalDate.parse("2026-05-11"),
            targetMinutes = 480,
            actualMinutes = 300,
            carryInMinutes = 0,
            isWorkday = true,
        )
        val tracking = TrackingState.Tracking(
            WorkSession(start = Instant.parse("2026-05-11T08:00:00Z"), source = SessionSource.Manual),
        )

        val prediction = PredictLeaveTimeUseCase()(day, tracking, now)

        assertEquals(
            Instant.parse("2026-05-11T15:00:00Z"),
            (prediction as LeavePrediction.LeaveAt).at,
        )
    }

    @Test
    fun reducerIgnoresRepeatedArrivalWhileTracking() {
        val reducer = TrackingStateReducer()
        val first = reducer.reduce(
            TrackingState.Idle,
            PresenceEvent.Arrival(Instant.parse("2026-05-11T08:00:00Z"), "office"),
        )
        val second = reducer.reduce(
            first,
            PresenceEvent.Arrival(Instant.parse("2026-05-11T08:05:00Z"), "office"),
        )

        assertEquals(first, second)
    }

    @Test
    fun returnGapSuggestionIsCreatedForShortGap() {
        val suggestion = PresenceDecisionEngine().suggestReturnGap(
            departedAt = Instant.parse("2026-05-11T10:00:00Z"),
            returnedAt = Instant.parse("2026-05-11T10:45:00Z"),
        )

        assertTrue(suggestion != null)
    }
}
