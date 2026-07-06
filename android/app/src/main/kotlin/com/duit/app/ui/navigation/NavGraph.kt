package com.duit.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.duit.app.data.local.TokenStorage
import com.duit.app.ui.auth.LoginScreen
import com.duit.app.ui.auth.TotpScreen
import com.duit.app.ui.budget.BudgetScreen
import com.duit.app.ui.category.CategoryScreen
import com.duit.app.ui.home.HomeScreen
import com.duit.app.ui.savings.SavingsScreen
import com.duit.app.ui.transaction.AddTransactionScreen
import com.duit.app.ui.transaction.TransactionListScreen
import com.duit.app.ui.wallet.WalletScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Beranda", Icons.Default.Home)
    object Add : Screen("add_transaction", "Tambah", Icons.Default.Add)
    object History : Screen("transactions", "Riwayat", Icons.Default.List)
    object Wallet : Screen("wallets", "Dompet", Icons.Default.AccountBox)
    object Budget : Screen("budgets", "Budget", Icons.Default.List)
    object Savings : Screen("savings", "Tabungan", Icons.Default.Person)
}

val bottomNavItems = listOf(Screen.Home, Screen.History, Screen.Wallet)

private fun routeTitle(route: String?): String = when (route) {
    Screen.Home.route -> "Duit"
    Screen.History.route -> "Riwayat"
    Screen.Wallet.route -> "Dompet"
    Screen.Budget.route -> "Budget"
    Screen.Savings.route -> "Tabungan"
    "categories" -> "Kategori"
    else -> "Duit"
}

// ponytail: index -1 = non-tab route (Add, Categories) — skip slide animation for these
private fun tabIndex(route: String?): Int =
    bottomNavItems.indexOfFirst { it.route == route }

private const val ANIM_DURATION = 300

@Composable
fun NavGraph(tokenStorage: TokenStorage = hiltViewModel<NavViewModel>().tokenStorage) {
    val navController = rememberNavController()
    val startDestination = if (tokenStorage.getToken() != null) "main" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRequires2FA = { tempToken ->
                    navController.navigate("totp/$tempToken")
                }
            )
        }
        composable("totp/{tempToken}") { backStackEntry ->
            val tempToken = backStackEntry.arguments?.getString("tempToken") ?: ""
            TotpScreen(
                tempToken = tempToken,
                onSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("main") {
            MainScreen(onLogout = {
                navController.navigate("login") {
                    popUpTo("main") { inclusive = true }
                }
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBottomBar = currentRoute != Screen.Add.route

    Scaffold(
        topBar = {
            if (showBottomBar) {
                TopAppBar(
                    title = { Text(routeTitle(currentRoute)) },
                    actions = {
                        if (currentRoute == Screen.Home.route) {
                            IconButton(onClick = onLogout) {
                                Icon(Icons.Default.Person, contentDescription = "Logout")
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (currentRoute == Screen.Home.route) {
                FloatingActionButton(onClick = {
                    navController.navigate(Screen.Add.route) {
                        launchSingleTop = true
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding),
            // Default: no animation — individual composables override for tab slides
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(
                route = Screen.Home.route,
                enterTransition = {
                    val from = tabIndex(initialState.destination.route)
                    val to = tabIndex(targetState.destination.route)
                    if (from < 0 || to < 0) EnterTransition.None
                    else slideInHorizontally(tween(ANIM_DURATION)) { if (to > from) it else -it }
                },
                exitTransition = {
                    val from = tabIndex(initialState.destination.route)
                    val to = tabIndex(targetState.destination.route)
                    if (from < 0 || to < 0) ExitTransition.None
                    else slideOutHorizontally(tween(ANIM_DURATION)) { if (to > from) -it else it }
                }
            ) { HomeScreen() }

            composable(
                route = Screen.Add.route,
                enterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { it } },
                exitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } },
                popEnterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { -it } },
                popExitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } }
            ) {
                AddTransactionScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.History.route,
                enterTransition = {
                    val from = tabIndex(initialState.destination.route)
                    val to = tabIndex(targetState.destination.route)
                    if (from < 0 || to < 0) EnterTransition.None
                    else slideInHorizontally(tween(ANIM_DURATION)) { if (to > from) it else -it }
                },
                exitTransition = {
                    val from = tabIndex(initialState.destination.route)
                    val to = tabIndex(targetState.destination.route)
                    if (from < 0 || to < 0) ExitTransition.None
                    else slideOutHorizontally(tween(ANIM_DURATION)) { if (to > from) -it else it }
                }
            ) { TransactionListScreen() }

            composable(
                route = Screen.Wallet.route,
                enterTransition = {
                    val from = tabIndex(initialState.destination.route)
                    val to = tabIndex(targetState.destination.route)
                    if (from < 0 || to < 0) EnterTransition.None
                    else slideInHorizontally(tween(ANIM_DURATION)) { if (to > from) it else -it }
                },
                exitTransition = {
                    val from = tabIndex(initialState.destination.route)
                    val to = tabIndex(targetState.destination.route)
                    if (from < 0 || to < 0) ExitTransition.None
                    else slideOutHorizontally(tween(ANIM_DURATION)) { if (to > from) -it else it }
                }
            ) { WalletScreen() }

            composable(
                route = "categories",
                enterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { it } },
                exitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } },
                popEnterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { -it } },
                popExitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } }
            ) { CategoryScreen() }

            composable(
                route = Screen.Budget.route,
                enterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { it } },
                exitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } },
                popEnterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { -it } },
                popExitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } }
            ) { BudgetScreen() }

            composable(
                route = Screen.Savings.route,
                enterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { it } },
                exitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } },
                popEnterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { -it } },
                popExitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } }
            ) { SavingsScreen() }
        }
    }
}