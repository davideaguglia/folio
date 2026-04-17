package com.personal.financeapp.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.financeapp.ui.components.DonutChart
import com.personal.financeapp.ui.theme.ChartColors
import com.personal.financeapp.ui.theme.ExpenseRed
import com.personal.financeapp.ui.theme.IncomeGreen
import com.personal.financeapp.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    onAddTransaction: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val balance = state.monthlyIncome - state.monthlyExpense

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Add transaction")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { NetWorthCard(state.monthlyIncome, state.monthlyExpense) }
            item { MonthSummaryRow(state.monthlyIncome, state.monthlyExpense) }
            if (state.categoryExpenses.isNotEmpty()) {
                item { SpendingBreakdownCard(state.categoryExpenses) }
            }
            if (state.recentTransactions.isNotEmpty()) {
                item {
                    Text("Recent Transactions", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                items(state.recentTransactions) { item ->
                    TransactionRow(item)
                }
            }
        }
    }
}

@Composable
private fun NetWorthCard(income: Double, expense: Double) {
    val net = income - expense
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("This Month Balance", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(
                CurrencyFormatter.format(net),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (net >= 0) IncomeGreen else ExpenseRed
            )
        }
    }
}

@Composable
private fun MonthSummaryRow(income: Double, expense: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryChip(
            label = "Income",
            amount = income,
            color = IncomeGreen,
            icon = Icons.Default.ArrowUpward,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Expenses",
            amount = expense,
            color = ExpenseRed,
            icon = Icons.Default.ArrowDownward,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryChip(
    label: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(CurrencyFormatter.format(amount), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SpendingBreakdownCard(expenses: List<CategoryExpense>) {
    val total = expenses.sumOf { it.amount }
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Spending by Category", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DonutChart(
                    data = expenses.mapIndexed { i, e ->
                        ChartColors[i % ChartColors.size] to e.amount
                    },
                    modifier = Modifier.size(120.dp),
                    centerLabel = CurrencyFormatter.format(total)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    expenses.take(5).forEachIndexed { i, exp ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(10.dp).clip(CircleShape)
                                    .background(ChartColors[i % ChartColors.size])
                            )
                            Text(
                                exp.category.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            Text(
                                CurrencyFormatter.format(exp.amount),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(item: com.personal.financeapp.data.local.dao.TransactionWithDetails) {
    val tx = item.transaction
    val isIncome = tx.type == "INCOME"
    val dateStr = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(tx.date))
    ListItem(
        headlineContent = { Text(item.category?.name ?: "—") },
        supportingContent = {
            Text("${item.account?.name ?: "—"} · $dateStr",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingContent = {
            Text(
                CurrencyFormatter.formatWithSign(tx.amount, isIncome),
                color = if (isIncome) IncomeGreen else ExpenseRed,
                fontWeight = FontWeight.SemiBold
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(if (isIncome) IncomeGreen.copy(.15f) else ExpenseRed.copy(.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (isIncome) IncomeGreen else ExpenseRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}
