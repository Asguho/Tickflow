package com.tickflow.app.di

import android.content.Context
import androidx.room.Room
import com.tickflow.app.core.time.ClockProvider
import com.tickflow.app.core.time.SystemClockProvider
import com.tickflow.app.data.local.TickflowDatabase
import com.tickflow.app.data.repository.SettingsRepositoryImpl
import com.tickflow.app.data.repository.WorkSessionRepositoryImpl
import com.tickflow.app.domain.repository.SettingsRepository
import com.tickflow.app.domain.repository.WorkSessionRepository
import com.tickflow.app.platform.connectivity.AndroidConnectivityObserver
import com.tickflow.app.platform.connectivity.WifiPresenceMonitor
import com.tickflow.app.platform.service.AndroidTrackingServiceController
import com.tickflow.app.platform.service.TrackingServiceController
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {
    @Binds
    abstract fun bindWorkSessionRepository(
        implementation: WorkSessionRepositoryImpl,
    ): WorkSessionRepository

    @Binds
    abstract fun bindSettingsRepository(
        implementation: SettingsRepositoryImpl,
    ): SettingsRepository

    @Binds
    abstract fun bindClockProvider(
        implementation: SystemClockProvider,
    ): ClockProvider

    @Binds
    abstract fun bindWifiPresenceMonitor(
        implementation: AndroidConnectivityObserver,
    ): WifiPresenceMonitor

    @Binds
    abstract fun bindTrackingServiceController(
        implementation: AndroidTrackingServiceController,
    ): TrackingServiceController
}

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): TickflowDatabase =
        Room.databaseBuilder(
            context,
            TickflowDatabase::class.java,
            "tickflow.db",
        ).build()
}
