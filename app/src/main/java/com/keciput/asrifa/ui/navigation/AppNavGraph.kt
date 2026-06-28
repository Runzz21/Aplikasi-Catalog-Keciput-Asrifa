package com.keciput.asrifa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.keciput.asrifa.ui.auth.LoginScreen
import com.keciput.asrifa.ui.auth.RegisterScreen
import com.keciput.asrifa.ui.detail.DetailScreen
import com.keciput.asrifa.ui.home.SearchScreen
import com.keciput.asrifa.ui.riwayat.RiwayatPesananScreen
import com.keciput.asrifa.ui.splash.SplashScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {
        // Splash Screen
        composable(Routes.Splash.route) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate("main") {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Screen (Bottom Navigation Host)
        composable("main") {
            MainScreen(rootNavController = navController)
        }

        // Detail Screen
        composable(
            route = Routes.Detail.route,
            arguments = listOf(navArgument("snackId") { type = NavType.IntType })
        ) { backStackEntry ->
            val snackId = backStackEntry.arguments?.getInt("snackId") ?: return@composable
                DetailScreen(
                    snackId = snackId,
                    onBack = { navController.popBackStack() },
                    onSnackClick = { id ->
                        navController.navigate(Routes.Detail.createRoute(id))
                    },
                    onLoginClick = {
                        navController.navigate(Routes.Login.route)
                    }
                )
        }

        // Login Screen
        composable(Routes.Login.route) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Routes.Register.route)
                }
            )
        }

        // Register Screen
        composable(Routes.Register.route) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        // Riwayat Pesanan Screen
        composable(Routes.RiwayatPesanan.route) {
            RiwayatPesananScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Search Screen
        composable(
            route = Routes.Search.route,
            arguments = listOf(navArgument("query") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query")
            SearchScreen(
                initialQuery = query,
                onBack = { navController.popBackStack() },
                onSnackClick = { id ->
                    navController.navigate(Routes.Detail.createRoute(id))
                }
            )
        }
    }
}
