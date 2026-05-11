package com.tickflow.app.platform.connectivity

import kotlinx.coroutines.flow.Flow

interface WifiPresenceMonitor {
    fun officeWifiPresence(): Flow<WifiPresence>
}

data class WifiPresence(
    val connectedOfficeWifiKey: String?,
)
