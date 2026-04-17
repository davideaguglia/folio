package com.personal.financeapp.ui.screens.investments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.financeapp.data.local.entity.InvestmentEntity
import com.personal.financeapp.data.local.entity.InvestmentPriceEntity
import com.personal.financeapp.ui.components.LineChart
import com.personal.financeapp.ui.theme.ExpenseRed
import com.personal.financeapp.ui.theme.IncomeGreen
import com.personal.financeapp.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentDetailScreen(
    investmentId: Long,
    onBack: () -> Unit,
    viewModel: InvestmentsViewModel = hiltViewModel()
) {
    val investment by remember(investmentId) {
        viewModel.uiState
    }.collectAsStateWithLifecycle()
    val inv = investment.investments.find { it.id == investmentId }
    val priceHistory by viewModel.getPriceHistory(investmentId)
        .collectAsStateWithLifecycle(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(inv?.name ?: "Investment") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        if (inv == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InvestmentStatsCard(inv)
            if (priceHistory.size >= 2) {
                PriceHistoryCard(priceHistory)
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Update price at least twice to see the chart",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (inv.notes.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Notes", style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(inv.notes)
                    }
                }
            }
        }
    }
}

@Composable
private fun InvestmentStatsCard(inv: InvestmentEntity) {
    val value = inv.currentPrice * inv.quantity
    val cost = inv.purchasePrice * inv.quantity
    val gainLoss = value - cost
    val gainPct = if (cost > 0) gainLoss / cost * 100 else 0.0
    val gainColor = if (gainLoss >= 0) IncomeGreen else ExpenseRed
    val purchaseDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(inv.purchaseDate))

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Current Value", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.format(value), style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Gain / Loss", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.format(gainLoss), color = gainColor,
                        fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
                    Text("${String.format("%.2f", gainPct)}%", color = gainColor,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("Quantity", "${inv.quantity}")
                StatItem("Buy Price", CurrencyFormatter.format(inv.purchasePrice))
                StatItem("Current Price", CurrencyFormatter.format(inv.currentPrice))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("Type", inv.type.replace('_', ' ').replaceFirstChar { it.titlecase() })
                StatItem("Purchased", purchaseDate)
                if (inv.ticker.isNotBlank()) StatItem("Ticker", inv.ticker)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PriceHistoryCard(history: List<InvestmentPriceEntity>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Price History", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            val chartData = history.mapIndexed { i, p -> i.toFloat() to p.price.toFloat() }
            val lineColor = if (history.last().price >= history.first().price) IncomeGreen else ExpenseRed
            LineChart(
                data = chartData,
                lineColor = lineColor,
                modifier = Modifier.fillMaxWidth().height(160.dp)
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val fmt = SimpleDateFormat("MMM dd", Locale.getDefault())
                Text(fmt.format(Date(history.first().date)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(fmt.format(Date(history.last().date)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
