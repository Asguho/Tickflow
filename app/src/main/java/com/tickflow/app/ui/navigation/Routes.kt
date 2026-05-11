package com.tickflow.app.ui.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Setup : Routes("setup")
    data object Sessions : Routes("sessions")
    data object Settings : Routes("settings")
}
