package com.example.otterhub.ui.navigation

sealed class Screen(val route: String) {
    data object Setup : Screen("setup")
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Favorites : Screen("favorites")
    data object Trash : Screen("trash")
    data object Settings : Screen("settings")
    data object Preview : Screen("preview/{fileKey}") {
        fun createRoute(fileKey: String) = "preview/${java.net.URLEncoder.encode(fileKey, "UTF-8")}"
    }
    data object Share : Screen("share/{token}") {
        fun createRoute(token: String) = "share/$token"
    }
}
