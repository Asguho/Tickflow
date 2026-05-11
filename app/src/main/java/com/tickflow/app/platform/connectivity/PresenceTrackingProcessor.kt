package com.tickflow.app.platform.connectivity

import com.tickflow.app.core.model.PresenceEvent
import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.time.ClockProvider
import com.tickflow.app.domain.service.PresenceDecisionEngine
import com.tickflow.app.domain.usecase.StartTrackingUseCase
import com.tickflow.app.domain.usecase.StopTrackingUseCase
import com.tickflow.app.platform.service.TrackingForegroundService
import com.tickflow.app.platform.service.TrackingServiceController
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceTrackingProcessor @Inject constructor(
    private val presenceDecisionEngine: PresenceDecisionEngine,
    private val clockProvider: ClockProvider,
    private val startTracking: StartTrackingUseCase,
    private val stopTracking: StopTrackingUseCase,
    private val serviceController: TrackingServiceController,
) {
    private var previousOfficeWifiKey: String? = null

    suspend fun handle(presence: WifiPresence) {
        val event = presenceDecisionEngine.eventForWifiState(
            connectedOfficeWifiKey = presence.connectedOfficeWifiKey,
            previousOfficeWifiKey = previousOfficeWifiKey,
            occurredAt = clockProvider.now(),
        )
        previousOfficeWifiKey = presence.connectedOfficeWifiKey

        when (event) {
            is PresenceEvent.Arrival -> {
                startTracking(event.occurredAt, SessionSource.Wifi)
                serviceController.start(TrackingForegroundService.Action.StartWifi)
            }
            is PresenceEvent.Departure -> {
                stopTracking(event.occurredAt)
                serviceController.start(TrackingForegroundService.Action.Stop)
            }
            else -> Unit
        }
    }
}
