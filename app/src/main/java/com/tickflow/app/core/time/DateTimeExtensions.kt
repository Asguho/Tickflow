package com.tickflow.app.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun LocalDate.startOfDayInstant(zoneId: ZoneId): Instant = atStartOfDay(zoneId).toInstant()

fun LocalDate.nextStartOfDayInstant(zoneId: ZoneId): Instant =
    plusDays(1).atStartOfDay(zoneId).toInstant()
