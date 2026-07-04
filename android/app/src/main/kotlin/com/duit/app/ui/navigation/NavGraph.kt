package com.duit.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.AccountBalanceWallet
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
import com.duit.app.ui.category.CategoryScreen
import com.duit.app.ui.home.HomeScreen
import com.duit.app.ui.transaction.AddTransactionScreen
import com.duit.app.ui.transaction.TransactionListScreen
import com.duit.app.ui.wallet.WalletScreen
import javax.inject.Inject

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Beranda", Icons.Default.Home)
    object Add : Screen("add_transaction", "Tambah", Icons.Default.Add)
    object History : Screen("transactions", "Riwayat", Icons.Default.List)
    object Wallet : Screen("wallets", "Dompet", Icons.Default.AccountBalanceWallet)
}

val bottomNavItems = listOf(Screen.Home, Screen.Add, Screen.History, Screen.Wallet)

@Composable
fun NavGraph(tokenStorage: TokenStorage = hiltViewModel<NavViewModel>().tokenStorage) {
    val navController = rememberNavController()
    val startDestination = if (tokenStorage.getToken() != null) "main" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            })
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

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on AddTransaction
    val showBottomBar = currentDestination?.route != Screen.Add.route

    Scaffold(
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
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Add.route) {
                AddTransactionScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.History.route) { TransactionListScreen() }
            composable(Screen.Wallet.route) { WalletScreen() }
            composable("categories") { CategoryScreen() }
        }
    }
}
