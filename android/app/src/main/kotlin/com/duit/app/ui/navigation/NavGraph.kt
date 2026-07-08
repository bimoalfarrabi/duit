package com.duit.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duit.app.data.local.TokenStorage
import com.duit.app.ui.auth.LoginScreen
import com.duit.app.ui.auth.TotpScreen
import com.duit.app.ui.budget.BudgetScreen
import com.duit.app.ui.category.CategoryScreen
import com.duit.app.ui.home.HomeScreen
import com.duit.app.ui.ocr.OcrScreen
import com.duit.app.ui.voice.VoiceInputScreen
import com.duit.app.ui.savings.SavingsScreen
import com.duit.app.ui.transaction.AddTransactionScreen
import com.duit.app.ui.transaction.TransactionListScreen
import com.duit.app.ui.wallet.WalletScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Beranda", Icons.Default.Home)
    // ponytail: query params as nullable strings — no new wrapper class needed
    object Add : Screen("add_transaction?ocr_title={ocr_title}&ocr_amount={ocr_amount}&ocr_date={ocr_date}&voice_title={voice_title}&voice_amount={voice_amount}&voice_type={voice_type}", "Tambah", Icons.Default.Add) {
        val baseRoute = "add_transaction"
        fun withOcr(title: String, amount: String, date: String) =
            "add_transaction?ocr_title=${title}&ocr_amount=${amount}&ocr_date=${date}"
        fun withVoice(title: String, amount: String, type: String) =
            "add_transaction?voice_title=${title}&voice_amount=${amount}&voice_type=${type}"
    }
    object History : Screen("transactions", "Riwayat", Icons.Default.List)
    object Wallet : Screen("wallets", "Dompet", Icons.Default.AccountBox)
    object Budget : Screen("budgets", "Budget", Icons.Default.List)
    object Savings : Screen("savings", "Tabungan", Icons.Default.Person)
    object Ocr : Screen("ocr", "Scan Struk", Icons.Default.CameraAlt)
    object Voice : Screen("voice", "Input Suara", Icons.Default.Add)
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
        && currentRoute != Screen.Ocr.route
        && currentRoute != Screen.Voice.route
        && currentRoute?.startsWith(Screen.Add.baseRoute) != true

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
                var fabExpanded by remember { mutableStateOf(false) }
                Column(horizontalAlignment = Alignment.End) {
                    // ponytail: AnimatedVisibility per item — slide up + fade, no custom animator
                    AnimatedVisibility(
                        visible = fabExpanded,
                        enter = slideInVertically(tween(200)) { it / 2 } + fadeIn(tween(200)),
                        exit = slideOutVertically(tween(150)) { it / 2 } + fadeOut(tween(150))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FabMenuItem(
                                icon = Icons.Default.CameraAlt,
                                label = "Scan Struk",
                                onClick = {
                                    fabExpanded = false
                                    navController.navigate(Screen.Ocr.route)
                                }
                            )
                            FabMenuItem(
                                icon = Icons.Default.Mic,
                                label = "Input Suara",
                                onClick = {
                                    fabExpanded = false
                                    navController.navigate(Screen.Voice.route)
                                }
                            )
                            FabMenuItem(
                                icon = Icons.Default.Add,
                                label = "Tambah Manual",
                                onClick = {
                                    fabExpanded = false
                                    navController.navigate(Screen.Add.baseRoute) { launchSingleTop = true }
                                }
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                    FloatingActionButton(onClick = { fabExpanded = !fabExpanded }) {
                        Icon(
                            if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (fabExpanded) "Tutup" else "Tambah"
                        )
                    }
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
                arguments = listOf(
                    navArgument("ocr_title") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("ocr_amount") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("ocr_date") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("voice_title") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("voice_amount") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("voice_type") { type = NavType.StringType; nullable = true; defaultValue = null }
                ),
                enterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { it } },
                exitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } },
                popEnterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { -it } },
                popExitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } }
            ) { backStackEntry ->
                val ocrTitle = backStackEntry.arguments?.getString("ocr_title")
                val ocrAmount = backStackEntry.arguments?.getString("ocr_amount")
                val ocrDate = backStackEntry.arguments?.getString("ocr_date")
                val ocrPrefill = if (ocrTitle != null || ocrAmount != null) {
                    Triple(ocrTitle.orEmpty(), ocrAmount.orEmpty(), ocrDate.orEmpty())
                } else null
                val voiceTitle = backStackEntry.arguments?.getString("voice_title")
                val voiceAmount = backStackEntry.arguments?.getString("voice_amount")
                val voiceType = backStackEntry.arguments?.getString("voice_type")
                val voicePrefill = if (voiceTitle != null || voiceAmount != null) {
                    Triple(voiceTitle.orEmpty(), voiceAmount.orEmpty(), voiceType.orEmpty())
                } else null
                AddTransactionScreen(
                    onBack = { navController.popBackStack() },
                    ocrPrefill = ocrPrefill,
                    voicePrefill = voicePrefill
                )
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

            composable(
                route = Screen.Ocr.route,
                enterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { it } },
                exitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } },
                popEnterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { -it } },
                popExitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } }
            ) {
                OcrScreen(
                    onBack = { navController.popBackStack() },
                    onResult = { title, amount, date ->
                        navController.navigate(Screen.Add.withOcr(title, amount, date)) {
                            popUpTo(Screen.Ocr.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.Voice.route,
                enterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { it } },
                exitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } },
                popEnterTransition = { slideInHorizontally(tween(ANIM_DURATION)) { -it } },
                popExitTransition = { slideOutHorizontally(tween(ANIM_DURATION)) { it } }
            ) {
                VoiceInputScreen(
                    onBack = { navController.popBackStack() },
                    onResult = { title, amount, type ->
                        navController.navigate(
                            "add_transaction?voice_title=$title&voice_amount=$amount&voice_type=$type"
                        ) {
                            popUpTo(Screen.Voice.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

// ponytail: private helper — pill FAB menu item (icon + label), used in speed dial only
@Composable
private fun FabMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.secondaryContainer,
            shadowElevation = 2.dp,
            tonalElevation = 2.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}