package com.example.otterhub.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.otterhub.data.api.RetrofitClient
import com.example.otterhub.data.local.PrefsManager
import com.example.otterhub.ui.screen.*
import kotlinx.coroutines.flow.first

@Composable
fun OtterHubNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }
    val isSetup by prefs.isSetup.collectAsState(initial = null)
    val baseUrl by prefs.baseUrl.collectAsState(initial = "")
    val authToken by prefs.authToken.collectAsState(initial = "")

    LaunchedEffect(isSetup, baseUrl, authToken) {
        if (isSetup == null) return@LaunchedEffect
        if (!isSetup!!) {
            navController.navigate(Screen.Setup.route) {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val token = if (authToken.isNotEmpty()) authToken else null
            RetrofitClient.configure(baseUrl, token)
            if (authToken.isEmpty()) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            } else {
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Setup.route,
        enterTransition = { fadeIn(tween(200)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(200)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(200)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(200)) },
        popExitTransition = { fadeOut(tween(200)) }
    ) {
        composable(Screen.Setup.route) {
            SetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onFileClick = { key -> navController.navigate(Screen.Preview.createRoute(key)) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onFavoritesClick = { navController.navigate(Screen.Favorites.route) },
                onTrashClick = { navController.navigate(Screen.Trash.route) },
                onUploadClick = { uri -> /* TODO: Handle upload */ }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onFileClick = { key -> navController.navigate(Screen.Preview.createRoute(key)) }
            )
        }

        composable(Screen.Trash.route) {
            TrashScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Preview.route,
            arguments = listOf(navArgument("fileKey") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileKey = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("fileKey") ?: "",
                "UTF-8"
            )
            PreviewScreen(
                fileKey = fileKey,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Share.route) {
            ShareScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
