package com.tickflow.app.data.datastore

import com.tickflow.app.core.model.WorkSchedule
import java.time.DayOfWeek

data class OfficeWifiIdentifier(
    val ssid: String,
    val bssid: String? = null,
) {
    val stableKey: String = listOfNotNull(ssid.trim(), bssid?.trim()).joinToString("|")
}

data class AppSettings(
    val schedule: WorkSchedule = WorkSchedule(),
    val officeWifi: OfficeWifiIdentifier? = null,
    val notificationsEnabled: Boolean = true,
    val rejectedCorrectionSuggestionIds: Set<String> = emptySet(),
) {
    val setupComplete: Boolean = officeWifi != null && schedule.workdays.isNotEmpty()
}

internal fun Set<DayOfWeek>.encode(): String =
    sortedBy { it.value }.joinToString(",") { it.name }

internal fun String.decodeWorkdays(): Set<DayOfWeek> =
    split(",")
        .mapNotNull { value -> value.takeIf { it.isNotBlank() }?.let(DayOfWeek::valueOf) }
        .toSet()

internal fun Set<String>.encodeStringSet(): String =
    sorted().joinToString("\n")

internal fun String.decodeStringSet(): Set<String> =
    lines().filter { it.isNotBlank() }.toSet()
