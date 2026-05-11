package com.tickflow.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tickflow.app.ui.home.HomeRoute
import com.tickflow.app.ui.sessions.SessionListRoute
import com.tickflow.app.ui.settings.SettingsRoute
import com.tickflow.app.ui.setup.SetupRoute

@Composable
fun TickflowNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Home.route) {
        composable(Routes.Home.route) {
            HomeRoute(
                onSetup = { navController.navigate(Routes.Setup.route) },
                onSessions = { navController.navigate(Routes.Sessions.route) },
                onSettings = { navController.navigate(Routes.Settings.route) },
            )
        }
        composable(Routes.Setup.route) {
            SetupRoute(onDone = { navController.navigate(Routes.Home.route) { popUpTo(Routes.Home.route) } })
        }
        composable(Routes.Sessions.route) {
            SessionListRoute(onBack = { navController.popBackStack() })
        }
        composable(Routes.Settings.route) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}
