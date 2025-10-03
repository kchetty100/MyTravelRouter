package com.kentonprojects.mytravelrouter.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kentonprojects.mytravelrouter.ui.components.AddConfigScreen
import com.kentonprojects.mytravelrouter.ui.components.ConfigScreen
import com.kentonprojects.mytravelrouter.ui.components.DashboardScreen
import com.kentonprojects.mytravelrouter.ui.components.EditConfigScreen
import com.kentonprojects.mytravelrouter.viewmodel.VpnViewModel

@Composable
fun TravelRouterNavigation(
    navController: NavHostController,
    vpnViewModel: VpnViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                vpnViewModel = vpnViewModel,
                onNavigateToSettings = {
                    navController.navigate(Screen.Configs.route)
                }
            )
        }
        
        composable(Screen.Configs.route) {
            ConfigScreen(
                vpnViewModel = vpnViewModel,
                onNavigateToAddConfig = {
                    navController.navigate(Screen.AddConfig.route)
                },
                onNavigateToEditConfig = { configName ->
                    navController.navigate(Screen.EditConfig.createRoute(configName))
                }
            )
        }
        
        composable(Screen.AddConfig.route) {
            AddConfigScreen(
                vpnViewModel = vpnViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.EditConfig.route) {
            val configName = it.arguments?.getString("configName") ?: ""
            EditConfigScreen(
                vpnViewModel = vpnViewModel,
                configName = configName,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Configs : Screen("configs")
    object AddConfig : Screen("add_config")
    object EditConfig : Screen("edit_config/{configName}") {
        fun createRoute(configName: String) = "edit_config/$configName"
    }
}
