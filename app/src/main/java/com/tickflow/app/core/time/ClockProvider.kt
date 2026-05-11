package com.tickflow.app.core.time

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

interface ClockProvider {
    val clock: Clock
    fun now(): Instant = clock.instant()
    fun zoneId(): ZoneId = clock.zone
}

@Singleton
class SystemClockProvider @Inject constructor() : ClockProvider {
    override val clock: Clock = Clock.systemDefaultZone()
}
