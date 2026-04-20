package com.personal.financeapp.ui.screens.investments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.financeapp.data.local.entity.InvestmentEntity
import com.personal.financeapp.ui.components.DonutChart
import com.personal.financeapp.ui.theme.ExpenseRed
import com.personal.financeapp.ui.theme.IncomeGreen
import com.personal.financeapp.util.CurrencyFormatter

private val PREDEFINED_TYPES = listOf("STOCK", "BOND", "GOLD", "OTHER")

private val TYPE_COLORS = mapOf(
    "STOCK" to Color(0xFF2196F3),
    "BOND"  to Color(0xFF4CAF50),
    "GOLD"  to Color(0xFFFFC107),
    "OTHER" to Color(0xFF9C27B0)
)
private val EXTRA_COLORS = listOf(
    Color(0xFFFF5722), Color(0xFF00BCD4), Color(0xFFE91E63),
    Color(0xFF607D8B), Color(0xFF795548), Color(0xFF009688)
)

private fun typeColor(type: String, index: Int): Color =
    TYPE_COLORS[type] ?: EXTRA_COLORS[index % EXTRA_COLORS.size]

@Composable
fun InvestmentsScreen(
    onViewDetail: (Long) -> Unit,
    viewModel: InvestmentsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val customTypes by viewModel.customTypes.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<InvestmentEntity?>(null) }
    var showUpdatePriceFor by remember { mutableStateOf<InvestmentEntity?>(null) }
    var showSetCashDialog by remember { mutableStateOf(false) }
    var cashInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editTarget = null; showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) { Icon(Icons.Default.Add, "Add investment") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 8.dp)
                ) {
                    Text(
                        "PORTFOLIO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("Investments", style = MaterialTheme.typography.headlineLarge)
                }
            }
            item { PortfolioSummaryCard(state) }
            item {
                CashAvailableCard(state, onSetCash = {
                    cashInput = state.initialCash.let {
                        if (it == 0.0) "" else it.toBigDecimal().stripTrailingZeros().toPlainString()
                    }
                    showSetCashDialog = true
                })
            }
            if (state.typeBreakdown.isNotEmpty()) {
                item { PortfolioTypeChart(state.typeBreakdown) }
            }
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
            customTypes = customTypes,
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

    if (showSetCashDialog) {
        AlertDialog(
            onDismissRequest = { showSetCashDialog = false },
            title = { Text("Set starting cash") },
            text = {
                Column {
                    Text(
                        "Enter your current cash on hand. Income and expenses will adjust this automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cashInput,
                        onValueChange = { if (it.matches(Regex("[0-9]*\\.?[0-9]*"))) cashInput = it },
                        label = { Text("Amount (€)") },
                        prefix = { Text("€") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    cashInput.toDoubleOrNull()?.let { viewModel.setInitialCash(it) }
                    showSetCashDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSetCashDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CashAvailableCard(state: InvestmentsUiState, onSetCash: () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onSetCash
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "CASH AVAILABLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    CurrencyFormatter.format(state.cashAvailable),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (state.cashAvailable >= 0) IncomeGreen else ExpenseRed
                )
                if (state.initialCash > 0) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Starting: ${CurrencyFormatter.format(state.initialCash)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Default.Edit, "Set cash",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PortfolioSummaryCard(state: InvestmentsUiState) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
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
private fun PortfolioTypeChart(breakdown: List<Pair<String, Double>>) {
    val total = breakdown.sumOf { it.second }
    val donutData = breakdown.mapIndexed { i, (type, value) -> typeColor(type, i) to value }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Allocation by Type", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DonutChart(
                    data = donutData,
                    modifier = Modifier.size(130.dp)
                )
                Spacer(Modifier.width(20.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    breakdown.forEachIndexed { i, (type, value) ->
                        val color = typeColor(type, i)
                        val pct = if (total > 0) value / total * 100 else 0.0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(Modifier.size(10.dp).background(color, CircleShape))
                            Column {
                                Text(type, style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium)
                                Text(
                                    "${String.format("%.1f", pct)}%  ·  ${CurrencyFormatter.format(value)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val TYPE_BG = mapOf(
    "CRYPTO" to Color(0xFFFDE4CD),
    "ETF"    to Color(0xFFD9E4D1),
    "STOCK"  to Color(0xFFE1DCF0),
    "BOND"   to Color(0xFFD1E4E0),
    "GOLD"   to Color(0xFFF5E9CC),
)
private fun typeBg(type: String) = TYPE_BG[type] ?: Color(0xFFE8E8E8)

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
    val gainPct = if (cost > 0) (gainLoss / cost) * 100 else 0.0
    val positive = gainLoss >= 0
    val gainColor = if (positive) IncomeGreen else ExpenseRed

    val abbrev = investment.ticker.take(2).ifBlank {
        investment.name.take(2).uppercase()
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onViewDetail,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Type-colored abbreviation box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(typeBg(investment.type), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    abbrev,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            // Name + ticker + type
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        investment.ticker.ifBlank { investment.name },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        investment.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp
                    )
                }
                Text(
                    investment.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            // Value + gain
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyFormatter.format(value),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${if (positive) "+" else ""}${String.format("%.2f", gainPct)}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = gainColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        // Actions row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 66.dp, end = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onUpdatePrice, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Update, "Update price", modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvestmentDialog(
    investment: InvestmentEntity?,
    customTypes: List<String>,
    onDismiss: () -> Unit,
    onSave: (InvestmentEntity) -> Unit
) {
    val initialType = investment?.type ?: "STOCK"
    val isInitiallyCustom = initialType !in PREDEFINED_TYPES

    var name by remember { mutableStateOf(investment?.name ?: "") }
    var ticker by remember { mutableStateOf(investment?.ticker ?: "") }
    var selectedType by remember { mutableStateOf(if (isInitiallyCustom) "Custom" else initialType) }
    var customTypeName by remember { mutableStateOf(if (isInitiallyCustom) initialType else "") }
    var quantity by remember { mutableStateOf(investment?.quantity?.toString() ?: "") }
    var purchasePrice by remember { mutableStateOf(investment?.purchasePrice?.toString() ?: "") }
    var typeExpanded by remember { mutableStateOf(false) }

    val effectiveType = if (selectedType == "Custom") customTypeName.trim() else selectedType

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
                        value = selectedType,
                        onValueChange = {}, readOnly = true, label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        PREDEFINED_TYPES.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = { selectedType = t; typeExpanded = false }
                            )
                        }
                        if (customTypes.isNotEmpty()) {
                            HorizontalDivider()
                            customTypes.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t) },
                                    onClick = { selectedType = t; typeExpanded = false }
                                )
                            }
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Custom…") },
                            onClick = { selectedType = "Custom"; typeExpanded = false }
                        )
                    }
                }
                if (selectedType == "Custom") {
                    OutlinedTextField(
                        value = customTypeName,
                        onValueChange = { customTypeName = it },
                        label = { Text("Custom type name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
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
                        name = name, ticker = ticker, type = effectiveType,
                        quantity = quantity.toDoubleOrNull() ?: 0.0,
                        purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                        currentPrice = investment?.currentPrice ?: (purchasePrice.toDoubleOrNull() ?: 0.0),
                        purchaseDate = investment?.purchaseDate ?: System.currentTimeMillis(),
                        notes = investment?.notes ?: ""
                    ))
                },
                enabled = name.isNotBlank()
                        && quantity.toDoubleOrNull() != null
                        && purchasePrice.toDoubleOrNull() != null
                        && effectiveType.isNotBlank()
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
