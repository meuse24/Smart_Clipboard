package com.smartclipboardmanager.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smartclipboardmanager.R
import com.smartclipboardmanager.ui.screen.detail.DetailRoute
import com.smartclipboardmanager.ui.screen.help.HelpRoute
import com.smartclipboardmanager.ui.screen.history.HistoryRoute
import com.smartclipboardmanager.ui.screen.home.HomeRoute
import com.smartclipboardmanager.ui.screen.info.InfoRoute
import com.smartclipboardmanager.ui.screen.settings.SettingsRoute
import com.smartclipboardmanager.ui.viewmodel.HomeViewModel

private val bottomNavRoutes = setOf(
    AppDestination.Home.route,
    AppDestination.History.route,
    AppDestination.Settings.route
)

@Composable
fun AppNavHost(
    homeViewModel: HomeViewModel,
    onImportClipboardRequest: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_home)) },
                        selected = currentRoute == AppDestination.Home.route,
                        onClick = {
                            navController.navigate(AppDestination.Home.route) {
                                popUpTo(AppDestination.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.History, contentDescription = null) },
                        label = { Text(stringResource(R.string.history_title)) },
                        selected = currentRoute == AppDestination.History.route,
                        onClick = {
                            navController.navigate(AppDestination.History.route) {
                                popUpTo(AppDestination.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_settings)) },
                        selected = currentRoute == AppDestination.Settings.route,
                        onClick = {
                            navController.navigate(AppDestination.Settings.route) {
                                popUpTo(AppDestination.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null) },
                        label = { Text(stringResource(R.string.help_title)) },
                        selected = currentRoute == AppDestination.Help.route,
                        onClick = { navController.navigate(AppDestination.Help.route) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(route = AppDestination.Home.route) {
                HomeRoute(
                    viewModel = homeViewModel,
                    onOpenDetail = { id -> navController.navigate(AppDestination.Detail.createRoute(id)) },
                    onOpenInfo = { navController.navigate(AppDestination.Info.route) },
                    onImportClipboardRequest = onImportClipboardRequest
                )
            }

            composable(route = AppDestination.History.route) {
                HistoryRoute(
                    onOpenDetail = { id -> navController.navigate(AppDestination.Detail.createRoute(id)) }
                )
            }

            composable(route = AppDestination.Settings.route) {
                SettingsRoute()
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
}
