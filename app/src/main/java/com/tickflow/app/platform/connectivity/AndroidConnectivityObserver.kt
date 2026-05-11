package com.tickflow.app.platform.connectivity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import com.tickflow.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidConnectivityObserver @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
) : WifiPresenceMonitor {
    override fun officeWifiPresence(): Flow<WifiPresence> {
        val networkEvents = callbackFlow {
            val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    trySend(Unit)
                }

                override fun onLost(network: Network) {
                    trySend(Unit)
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    trySend(Unit)
                }
            }
            connectivityManager.registerDefaultNetworkCallback(callback)
            trySend(Unit)
            awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
        }

        return combine(settingsRepository.settings, networkEvents) { settings, _ ->
            val configured = settings.officeWifi
            val current = currentWifiInfo()
            val matched = configured?.takeIf { identifier ->
                current != null &&
                    current.ssid.trim('"') == identifier.ssid.trim('"') &&
                    (identifier.bssid == null || current.bssid.equals(identifier.bssid, ignoreCase = true))
            }
            WifiPresence(matched?.stableKey)
        }
    }

    @Suppress("DEPRECATION")
    private fun currentWifiInfo(): CurrentWifiInfo? {
        val hasLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasLocation) return null

        val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
            ?: return null
        val info: WifiInfo = wifiManager.connectionInfo ?: return null
        val ssid = info.ssid?.takeUnless { it == WifiManager.UNKNOWN_SSID } ?: return null
        return CurrentWifiInfo(ssid = ssid, bssid = info.bssid)
    }

    private data class CurrentWifiInfo(
        val ssid: String,
        val bssid: String?,
    )
}
