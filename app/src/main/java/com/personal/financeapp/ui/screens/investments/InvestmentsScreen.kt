package com.personal.financeapp.ui.screens.investments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.financeapp.data.local.entity.InvestmentEntity
import com.personal.financeapp.ui.theme.ExpenseRed
import com.personal.financeapp.ui.theme.IncomeGreen
import com.personal.financeapp.util.CurrencyFormatter

@Composable
fun InvestmentsScreen(
    onViewDetail: (Long) -> Unit,
    viewModel: InvestmentsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<InvestmentEntity?>(null) }
    var showUpdatePriceFor by remember { mutableStateOf<InvestmentEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { editTarget = null; showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add investment")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { PortfolioSummaryCard(state) }
            items(state.investments, key = { it.id }) { inv ->
                InvestmentCard(
                    investment = inv,
                    onViewDetail = { onViewDetail(inv.id) },
                    onEdit = { editTarget = inv; showAddDialog = true },
                    onDelete = { viewModel.delete(inv) },
                    onUpdatePrice = { showUpdatePriceFor = inv }
                )
            }
        }
    }

    if (showAddDialog) {
        InvestmentDialog(
            investment = editTarget,
            onDismiss = { showAddDialog = false },
            onSave = { inv ->
                if (editTarget == null) viewModel.insert(inv) else viewModel.update(inv)
                showAddDialog = false
            }
        )
    }

    showUpdatePriceFor?.let { inv ->
        UpdatePriceDialog(
            investment = inv,
            onDismiss = { showUpdatePriceFor = null },
            onUpdate = { price ->
                viewModel.recordPrice(inv.id, price, System.currentTimeMillis())
                showUpdatePriceFor = null
            }
        )
    }
}

@Composable
private fun PortfolioSummaryCard(state: InvestmentsUiState) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Portfolio Value", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                CurrencyFormatter.format(state.totalValue),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            val gainColor = if (state.gainLoss >= 0) IncomeGreen else ExpenseRed
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${if (state.gainLoss >= 0) "+" else ""}${CurrencyFormatter.format(state.gainLoss)}",
                    color = gainColor, fontWeight = FontWeight.SemiBold
                )
                Text(
                    "(${String.format("%.1f", state.gainLossPct)}%)",
                    color = gainColor, style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun InvestmentCard(
    investment: InvestmentEntity,
    onViewDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdatePrice: () -> Unit
) {
    val value = investment.currentPrice * investment.quantity
    val cost = investment.purchasePrice * investment.quantity
    val gainLoss = value - cost
    val gainColor = if (gainLoss >= 0) IncomeGreen else ExpenseRed

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onViewDetail
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(investment.name, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    if (investment.ticker.isNotBlank()) {
                        Text(investment.ticker, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        investment.type.replace('_', ' ').lowercase().replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(CurrencyFormatter.format(value), fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${if (gainLoss >= 0) "+" else ""}${CurrencyFormatter.format(gainLoss)}",
                        color = gainColor, style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${investment.quantity} units × ${CurrencyFormatter.format(investment.currentPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row {
                    IconButton(onClick = onUpdatePrice, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Update, "Update price", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvestmentDialog(
    investment: InvestmentEntity?,
    onDismiss: () -> Unit,
    onSave: (InvestmentEntity) -> Unit
) {
    var name by remember { mutableStateOf(investment?.name ?: "") }
    var ticker by remember { mutableStateOf(investment?.ticker ?: "") }
    var type by remember { mutableStateOf(investment?.type ?: "STOCK") }
    var quantity by remember { mutableStateOf(investment?.quantity?.toString() ?: "") }
    var purchasePrice by remember { mutableStateOf(investment?.purchasePrice?.toString() ?: "") }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (investment == null) "New Investment" else "Edit Investment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = ticker, onValueChange = { ticker = it },
                    label = { Text("Ticker / Symbol (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = type.replace('_', ' ').replaceFirstChar { it.titlecase() },
                        onValueChange = {}, readOnly = true, label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        listOf("STOCK", "CRYPTO", "ETF", "REAL_ESTATE", "OTHER").forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.replace('_', ' ').replaceFirstChar { it.titlecase() }) },
                                onClick = { type = t; typeExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(value = quantity, onValueChange = { quantity = it },
                    label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = purchasePrice, onValueChange = { purchasePrice = it },
                    label = { Text("Purchase price per unit (€)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), prefix = { Text("€") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(InvestmentEntity(
                        id = investment?.id ?: 0L,
                        name = name, ticker = ticker, type = type,
                        quantity = quantity.toDoubleOrNull() ?: 0.0,
                        purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                        currentPrice = investment?.currentPrice ?: (purchasePrice.toDoubleOrNull() ?: 0.0),
                        purchaseDate = investment?.purchaseDate ?: System.currentTimeMillis(),
                        notes = investment?.notes ?: ""
                    ))
                },
                enabled = name.isNotBlank() && quantity.toDoubleOrNull() != null && purchasePrice.toDoubleOrNull() != null
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun UpdatePriceDialog(
    investment: InvestmentEntity,
    onDismiss: () -> Unit,
    onUpdate: (Double) -> Unit
) {
    var price by remember { mutableStateOf(investment.currentPrice.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Price — ${investment.name}") },
        text = {
            OutlinedTextField(
                value = price, onValueChange = { price = it },
                label = { Text("Current price per unit (€)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("€") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { price.toDoubleOrNull()?.let { onUpdate(it) } },
                enabled = price.toDoubleOrNull() != null
            ) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
