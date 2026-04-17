package com.personal.financeapp.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.financeapp.ui.components.BarChart
import com.personal.financeapp.ui.components.DonutChart
import com.personal.financeapp.ui.components.LineChart
import com.personal.financeapp.ui.components.MonthlyBarData
import com.personal.financeapp.ui.theme.ChartColors
import com.personal.financeapp.ui.theme.ExpenseRed
import com.personal.financeapp.ui.theme.IncomeGreen
import com.personal.financeapp.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
    totalAccountsBalance: Double = 0.0,
    totalPortfolioValue: Double = 0.0
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSnapshotDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { MonthSelector(state, onMonthChange = { m, y -> viewModel.selectMonth(m, y) }) }
        item { MonthlyBarChartCard(state.monthlyData) }
        if (state.categoryBreakdown.isNotEmpty()) {
            item { CategoryBreakdownCard(state) }
        } else {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No expenses in this month",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { NetWorthHistoryCard(state, onRecordSnapshot = { showSnapshotDialog = true }) }
    }

    if (showSnapshotDialog) {
        AlertDialog(
            onDismissRequest = { showSnapshotDialog = false },
            title = { Text("Record Net Worth") },
            text = {
                Column {
                    Text("This will save a snapshot of your current net worth.")
                    Spacer(Modifier.height(8.dp))
                    Text("Make sure your account balances and investment prices are up to date.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val assets = totalAccountsBalance.coerceAtLeast(0.0) + totalPortfolioValue
                    val liabilities = (-totalAccountsBalance).coerceAtLeast(0.0)
                    viewModel.recordNetWorthSnapshot(assets, liabilities)
                    showSnapshotDialog = false
                }) { Text("Record") }
            },
            dismissButton = { TextButton(onClick = { showSnapshotDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun MonthSelector(state: ReportsUiState, onMonthChange: (Int, Int) -> Unit) {
    val monthNames = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    }
    val cal = remember(state.selectedMonth, state.selectedYear) {
        Calendar.getInstance().apply { set(state.selectedYear, state.selectedMonth, 1) }
    }
    val label = monthNames.format(cal.time)

    // Prevent selecting future months
    val now = Calendar.getInstance()
    val isCurrentOrFuture = state.selectedYear > now.get(Calendar.YEAR) ||
        (state.selectedYear == now.get(Calendar.YEAR) && state.selectedMonth >= now.get(Calendar.MONTH))

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                val prev = Calendar.getInstance().apply { set(state.selectedYear, state.selectedMonth, 1) }
                prev.add(Calendar.MONTH, -1)
                onMonthChange(prev.get(Calendar.MONTH), prev.get(Calendar.YEAR))
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
            }

            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(
                onClick = {
                    val next = Calendar.getInstance().apply { set(state.selectedYear, state.selectedMonth, 1) }
                    next.add(Calendar.MONTH, 1)
                    onMonthChange(next.get(Calendar.MONTH), next.get(Calendar.YEAR))
                },
                enabled = !isCurrentOrFuture
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
            }
        }
    }
}

@Composable
private fun MonthlyBarChartCard(data: List<MonthlyData>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Income vs Expenses (12 months)", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            if (data.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                    Text("No data", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                BarChart(
                    data = data.map { MonthlyBarData(it.label, it.income, it.expense) },
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    LegendItem(IncomeGreen, "Income")
                    LegendItem(ExpenseRed, "Expenses")
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(state: ReportsUiState) {
    val breakdown = state.categoryBreakdown
    val monthNames = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val cal = Calendar.getInstance().apply { set(state.selectedYear, state.selectedMonth, 1) }
    val totalSpent = breakdown.sumOf { it.amount }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Expenses — ${monthNames.format(cal.time)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text(CurrencyFormatter.format(totalSpent),
                    style = MaterialTheme.typography.titleMedium,
                    color = ExpenseRed, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DonutChart(
                    data = breakdown.mapIndexed { i, b -> ChartColors[i % ChartColors.size] to b.amount },
                    modifier = Modifier.size(100.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    breakdown.take(6).forEachIndexed { i, item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(10.dp).clip(CircleShape)
                                    .background(ChartColors[i % ChartColors.size])
                            )
                            Text(item.category.name, modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            Text(CurrencyFormatter.format(item.amount),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NetWorthHistoryCard(state: ReportsUiState, onRecordSnapshot: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Net Worth Over Time", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onRecordSnapshot) {
                    Icon(Icons.Default.Add, "Record snapshot")
                }
            }
            if (state.netWorthHistory.size < 2) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Record at least 2 snapshots to see the chart",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val chartData = state.netWorthHistory.mapIndexed { i, s -> i.toFloat() to s.netWorth.toFloat() }
                LineChart(
                    data = chartData,
                    lineColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val fmt = SimpleDateFormat("MMM yy", Locale.getDefault())
                    Text(fmt.format(Date(state.netWorthHistory.first().date)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(fmt.format(Date(state.netWorthHistory.last().date)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (state.netWorthHistory.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                val latest = state.netWorthHistory.last()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Latest Net Worth", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(CurrencyFormatter.format(latest.netWorth), fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Assets / Liabilities", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${CurrencyFormatter.format(latest.totalAssets)} / ${CurrencyFormatter.format(latest.totalLiabilities)}",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
