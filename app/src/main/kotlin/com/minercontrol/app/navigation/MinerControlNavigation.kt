package com.minercontrol.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.minercontrol.app.model.SettingsRepository
import com.minercontrol.app.network.UdpListener
import com.minercontrol.app.ui.screens.DashboardScreen
import com.minercontrol.app.ui.screens.SettingsScreen

@Composable
fun MinerControlNavigation(
    udpListener: UdpListener,
    settingsRepository: SettingsRepository
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(
                udpListener = udpListener,
                onNavigateToSettings = { navController.navigate("settings") },
                settingsRepository = settingsRepository
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                settingsRepository = settingsRepository,
                udpListener = udpListener
            )
        }
    }
}
