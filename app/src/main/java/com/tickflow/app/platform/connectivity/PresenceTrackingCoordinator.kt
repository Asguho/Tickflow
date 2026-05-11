package com.tickflow.app.platform.connectivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceTrackingCoordinator @Inject constructor(
    private val wifiPresenceMonitor: WifiPresenceMonitor,
    private val processor: PresenceTrackingProcessor,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            wifiPresenceMonitor.officeWifiPresence()
                .distinctUntilChanged()
                .collect(processor::handle)
        }
    }
}
