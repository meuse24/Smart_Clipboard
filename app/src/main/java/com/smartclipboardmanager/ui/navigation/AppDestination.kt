package com.smartclipboardmanager.ui.navigation

sealed class AppDestination(val route: String) {
    data object Home : AppDestination("home")
    data object History : AppDestination("history")
    data object Settings : AppDestination("settings")
    data object Help : AppDestination("help")
    data object Info : AppDestination("info")
    data object Detail : AppDestination("detail/{entryId}") {
        fun createRoute(entryId: Long): String = "detail/$entryId"
    }
}
