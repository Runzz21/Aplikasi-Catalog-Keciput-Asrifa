package com.keciput.asrifa.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.keciput.asrifa.ui.catalog.CatalogScreen
import com.keciput.asrifa.ui.home.HomeScreen
import com.keciput.asrifa.ui.home.SeeAllType
import com.keciput.asrifa.ui.pesanan.PesananScreen
import com.keciput.asrifa.ui.pesanan.PesananViewModel
import com.keciput.asrifa.ui.profile.ProfileScreen
import com.keciput.asrifa.ui.theme.*

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    pesananViewModel: PesananViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val pesananUiState by pesananViewModel.uiState.collectAsStateWithLifecycle()
    val cartCount = pesananUiState.cartItems.sumOf { it.quantity }

    // Trigger notifikasi status toko saat aplikasi pertama kali dibuka
    LaunchedEffect(Unit) {
        mainViewModel.checkStoreStatusAndNotify()
    }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                BottomNavItem.items.forEach { item ->
                    val selected = currentRoute?.startsWith(item.route.split("?")[0]) == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CoralDark,
                            selectedTextColor = CoralDark,
                            indicatorColor = CoralSoft.copy(alpha = 0.2f),
                            unselectedIconColor = InkMuted,
                            unselectedTextColor = InkMuted
                        ),
                        icon = {
                            if (item is BottomNavItem.Pesanan && cartCount > 0) {
                                BadgedBox(
                                    badge = { 
                                        Badge(containerColor = Gold, contentColor = Color.White) { 
                                            Text(cartCount.toString()) 
                                        } 
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            }
                        },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.Home.route) {
                HomeScreen(
                    onSnackClick = { id -> rootNavController.navigate(Routes.Detail.createRoute(id)) },
                    onSearchClick = { rootNavController.navigate(Routes.Search.createRoute()) },
                    onSeeAllClick = { type ->
                        val filterType = when(type) {
                            SeeAllType.FEATURED -> "featured"
                            SeeAllType.POPULAR -> "popular"
                        }
                        bottomNavController.navigate(Routes.Catalog.createRoute(filterType = filterType)) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = Routes.Catalog.route,
                arguments = listOf(
                    navArgument("category") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("filterType") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category")
                val filterType = backStackEntry.arguments?.getString("filterType")
                CatalogScreen(
                    initialCategory = category,
                    initialFilterType = filterType,
                    onBackClick = { bottomNavController.popBackStack() },
                    onSnackClick = { id -> rootNavController.navigate(Routes.Detail.createRoute(id)) }
                )
            }
            composable(Routes.Pesanan.route) {
                PesananScreen(
                    onExploreClick = {
                        bottomNavController.navigate(Routes.Catalog.route) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSnackClick = { id -> rootNavController.navigate(Routes.Detail.createRoute(id)) },
                    onLoginClick = {
                        rootNavController.navigate(Routes.Login.route)
                    }
                )
            }
            composable(Routes.Info.route) {
                ProfileScreen(
                    onRiwayatClick = {
                        rootNavController.navigate(Routes.RiwayatPesanan.route)
                    },
                    onLoginClick = {
                        rootNavController.navigate(Routes.Login.route)
                    },
                    onRegisterClick = {
                        rootNavController.navigate(Routes.Register.route)
                    },
                    onAdminClick = {
                        rootNavController.navigate(Routes.AdminList.route)
                    }
                )
            }
        }
    }
}
