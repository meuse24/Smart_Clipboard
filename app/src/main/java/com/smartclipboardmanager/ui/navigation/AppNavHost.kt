package com.smartclipboardmanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smartclipboardmanager.ui.screen.detail.DetailRoute
import com.smartclipboardmanager.ui.screen.help.HelpRoute
import com.smartclipboardmanager.ui.screen.history.HistoryRoute
import com.smartclipboardmanager.ui.screen.home.HomeRoute
import com.smartclipboardmanager.ui.screen.info.InfoRoute
import com.smartclipboardmanager.ui.screen.settings.SettingsRoute
import com.smartclipboardmanager.ui.viewmodel.HomeViewModel

@Composable
fun AppNavHost(
    homeViewModel: HomeViewModel,
    onImportClipboardRequest: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route
    ) {
        composable(route = AppDestination.Home.route) {
            HomeRoute(
                viewModel = homeViewModel,
                onNavigateHistory = { navController.navigate(AppDestination.History.route) },
                onNavigateSettings = { navController.navigate(AppDestination.Settings.route) },
                onNavigateHelp = { navController.navigate(AppDestination.Help.route) },
                onNavigateInfo = { navController.navigate(AppDestination.Info.route) },
                onOpenDetail = { id -> navController.navigate(AppDestination.Detail.createRoute(id)) },
                onImportClipboardRequest = onImportClipboardRequest
            )
        }

        composable(route = AppDestination.History.route) {
            HistoryRoute(
                onBack = { navController.popBackStack() },
                onOpenDetail = { id -> navController.navigate(AppDestination.Detail.createRoute(id)) }
            )
        }

        composable(route = AppDestination.Settings.route) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }

        composable(route = AppDestination.Help.route) {
            HelpRoute(onBack = { navController.popBackStack() })
        }

        composable(route = AppDestination.Info.route) {
            InfoRoute(onBack = { navController.popBackStack() })
        }

        composable(
            route = AppDestination.Detail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) {
            DetailRoute(onBack = { navController.popBackStack() })
        }
    }
}
