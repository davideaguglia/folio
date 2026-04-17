package com.personal.financeapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.*
import androidx.navigation.compose.*
import com.personal.financeapp.ui.screens.accounts.AccountsScreen
import com.personal.financeapp.ui.screens.budget.BudgetScreen
import com.personal.financeapp.ui.screens.dashboard.DashboardScreen
import com.personal.financeapp.ui.screens.investments.InvestmentDetailScreen
import com.personal.financeapp.ui.screens.investments.InvestmentsScreen
import com.personal.financeapp.ui.screens.reports.ReportsScreen
import com.personal.financeapp.ui.screens.transactions.AddEditTransactionScreen
import com.personal.financeapp.ui.screens.transactions.TransactionListScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard    : Screen("dashboard",    "Home",     Icons.Default.Home)
    object Transactions : Screen("transactions", "History",  Icons.Default.Receipt)
    object Investments  : Screen("investments",  "Assets",   Icons.Default.TrendingUp)
    object Budget       : Screen("budget",       "Budget",   Icons.Default.PieChart)
    object Reports      : Screen("reports",      "Reports",  Icons.Default.BarChart)
    object AddTransaction  : Screen("add_transaction?id={id}", "Add",    Icons.Default.Add)
    object InvestmentDetail: Screen("investment/{id}",         "Detail", Icons.Default.Info)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Transactions,
    Screen.Investments,
    Screen.Budget,
    Screen.Reports
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = bottomNavItems.find { it.route == currentRoute }
    val showChrome = currentScreen != null // show top bar + bottom bar on main tabs only

    Scaffold(
        topBar = {
            if (showChrome) {
                TopAppBar(
                    title = { Text(currentScreen?.label ?: "Finance Tracker") },
                    actions = {
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode
                                              else Icons.Default.DarkMode,
                                contentDescription = "Toggle theme"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showChrome) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onAddTransaction = { navController.navigate("add_transaction?id=-1") }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionListScreen(
                    onAddTransaction = { navController.navigate("add_transaction?id=-1") },
                    onEditTransaction = { id -> navController.navigate("add_transaction?id=$id") }
                )
            }
            composable(
                route = "add_transaction?id={id}",
                arguments = listOf(navArgument("id") {
                    type = NavType.LongType; defaultValue = -1L
                })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: -1L
                AddEditTransactionScreen(
                    transactionId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Investments.route) {
                InvestmentsScreen(
                    onViewDetail = { id -> navController.navigate("investment/$id") }
                )
            }
            composable(
                route = "investment/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: return@composable
                InvestmentDetailScreen(
                    investmentId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Budget.route) {
                BudgetScreen()
            }
            composable(Screen.Reports.route) {
                ReportsScreen()
            }
        }
    }
}
