package com.tickflow.app.platform.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface TrackingServiceController {
    fun start(action: TrackingForegroundService.Action): Boolean
}

@Singleton
class AndroidTrackingServiceController @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : TrackingServiceController {
    override fun start(action: TrackingForegroundService.Action): Boolean =
        TrackingForegroundService.start(context, action)
}
