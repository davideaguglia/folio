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
import com.personal.financeapp.ui.theme.Forest
import com.personal.financeapp.ui.theme.IncomeGreen
import com.personal.financeapp.util.CurrencyFormatter

private val PREDEFINED_TYPES = listOf("STOCK", "ETF", "CRYPTO", "BOND", "GOLD", "OTHER")

private val TYPE_COLORS = mapOf(
    "STOCK"  to Color(0xFF2196F3),
    "ETF"    to Color(0xFF4CAF50),
    "CRYPTO" to Color(0xFFFF9800),
    "BOND"   to Color(0xFF009688),
    "GOLD"   to Color(0xFFFFC107),
    "OTHER"  to Color(0xFF9C27B0)
)
private val EXTRA_COLORS = listOf(
    Color(0xFFFF5722), Color(0xFF00BCD4), Color(0xFFE91E63),
    Color(0xFF607D8B), Color(0xFF795548)
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
    val tickerLookup by viewModel.tickerLookup.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<InvestmentEntity?>(null) }
    var showUpdatePriceFor by remember { mutableStateOf<InvestmentEntity?>(null) }
    var showSetCashDialog by remember { mutableStateOf(false) }
    var cashInput by remember { mutableStateOf("") }

    // Refresh prices on screen open
    LaunchedEffect(Unit) { viewModel.refreshAllPrices() }

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("PORTFOLIO", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(2.dp))
                        Text("Investments", style = MaterialTheme.typography.headlineLarge)
                    }
                    if (state.isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp).padding(bottom = 4.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { viewModel.refreshAllPrices() }) {
                            Icon(Icons.Default.Refresh, "Refresh prices",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp))
                        }
                    }
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
            tickerLookup = tickerLookup,
            onDismiss = { showAddDialog = false; viewModel.resetTickerLookup() },
            onSave = { inv ->
                if (editTarget == null) viewModel.insertAndFetch(inv)
                else viewModel.update(inv)
                showAddDialog = false
                viewModel.resetTickerLookup()
            },
            onLookupTicker = viewModel::lookupTicker,
            onResetLookup = viewModel::resetTickerLookup
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
            dismissButton = { TextButton(onClick = { showSetCashDialog = false }) { Text("Cancel") } }
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
                Text("CASH AVAILABLE", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(
                    CurrencyFormatter.format(state.cashAvailable),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (state.cashAvailable >= 0) IncomeGreen else ExpenseRed
                )
                if (state.initialCash > 0) {
                    Spacer(Modifier.height(2.dp))
                    Text("Starting: ${CurrencyFormatter.format(state.initialCash)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.Edit, "Set cash",
                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PortfolioSummaryCard(state: InvestmentsUiState) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Portfolio Value", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(CurrencyFormatter.format(state.totalValue),
                style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            val gainColor = if (state.gainLoss >= 0) IncomeGreen else ExpenseRed
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${if (state.gainLoss >= 0) "+" else ""}${CurrencyFormatter.format(state.gainLoss)}",
                    color = gainColor, fontWeight = FontWeight.SemiBold)
                Text("(${String.format("%.1f", state.gainLossPct)}%)",
                    color = gainColor, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun PortfolioTypeChart(breakdown: List<Pair<String, Double>>) {
    val total = breakdown.sumOf { it.second }
    val donutData = breakdown.mapIndexed { i, (type, value) -> typeColor(type, i) to value }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Allocation by Type", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                DonutChart(data = donutData, modifier = Modifier.size(130.dp))
                Spacer(Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    breakdown.forEachIndexed { i, (type, value) ->
                        val color = typeColor(type, i)
                        val pct = if (total > 0) value / total * 100 else 0.0
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(10.dp).background(color, CircleShape))
                            Column {
                                Text(type, style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium)
                                Text("${String.format("%.1f", pct)}%  ·  ${CurrencyFormatter.format(value)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

private val TYPE_BG = mapOf(
    "CRYPTO" to Color(0xFFFDE4CD), "ETF"   to Color(0xFFD9E4D1),
    "STOCK"  to Color(0xFFE1DCF0), "BOND"  to Color(0xFFD1E4E0),
    "GOLD"   to Color(0xFFF5E9CC)
)
private fun typeBg(type: String) = TYPE_BG[type] ?: Color(0xFFE8E8E8)

@Composable
private fun InvestmentCard(
    investment: InvestmentEntity,
    onViewDetail: () -> Unit, onEdit: () -> Unit,
    onDelete: () -> Unit, onUpdatePrice: () -> Unit
) {
    val value = investment.currentPrice * investment.quantity
    val cost = investment.purchasePrice * investment.quantity
    val gainLoss = value - cost
    val gainPct = if (cost > 0) (gainLoss / cost) * 100 else 0.0
    val gainColor = if (gainLoss >= 0) IncomeGreen else ExpenseRed
    val abbrev = investment.ticker.take(4).ifBlank { investment.name.take(2).uppercase() }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(), onClick = onViewDetail,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(40.dp).background(typeBg(investment.type), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center) {
                Text(abbrev, style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(investment.ticker.ifBlank { investment.name },
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(investment.type, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp)
                    if (investment.autoFetch) {
                        Icon(Icons.Default.Sync, null, modifier = Modifier.size(12.dp),
                            tint = Forest.copy(alpha = 0.7f))
                    }
                }
                Text(investment.name, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(CurrencyFormatter.format(value), style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium)
                Text("${if (gainLoss >= 0) "+" else ""}${String.format("%.2f", gainPct)}%",
                    style = MaterialTheme.typography.labelMedium, color = gainColor,
                    fontWeight = FontWeight.SemiBold)
                if (investment.currency != "EUR") {
                    Text(investment.currency, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(start = 66.dp, end = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.End) {
            if (!investment.autoFetch) {
                IconButton(onClick = onUpdatePrice, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Update, "Update price", modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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

@Composable
private fun InvestmentDialog(
    investment: InvestmentEntity?,
    customTypes: List<String>,
    tickerLookup: TickerLookupState,
    onDismiss: () -> Unit,
    onSave: (InvestmentEntity) -> Unit,
    onLookupTicker: (String) -> Unit,
    onResetLookup: () -> Unit
) {
    val isEditing = investment != null
    var tickerInput    by remember { mutableStateOf(investment?.ticker ?: "") }
    var name           by remember { mutableStateOf(investment?.name ?: "") }
    var selectedType   by remember { mutableStateOf(investment?.type ?: "STOCK") }
    var customTypeName by remember { mutableStateOf("") }
    var quantity       by remember { mutableStateOf(investment?.quantity?.toString() ?: "") }
    var purchasePrice  by remember { mutableStateOf(investment?.purchasePrice?.toString() ?: "") }
    var manualPrice    by remember { mutableStateOf(investment?.currentPrice?.toString() ?: "") }
    var autoFetch      by remember { mutableStateOf(investment?.autoFetch ?: false) }
    var currency       by remember { mutableStateOf(investment?.currency ?: "EUR") }
    var typeExpanded   by remember { mutableStateOf(false) }

    // Auto-fill from successful ticker lookup
    LaunchedEffect(tickerLookup) {
        if (!isEditing && tickerLookup is TickerLookupState.Found) {
            name = tickerLookup.name
            selectedType = tickerLookup.type
            manualPrice = tickerLookup.price.toString()
            currency = tickerLookup.currency
            autoFetch = true
        }
    }

    val found = tickerLookup as? TickerLookupState.Found
    val effectiveType = if (selectedType == "Custom") customTypeName.trim() else selectedType
    val currentPrice = if (found != null && autoFetch) found.price
                       else manualPrice.toDoubleOrNull() ?: (investment?.currentPrice ?: 0.0)

    val canSave = name.isNotBlank() && quantity.toDoubleOrNull() != null &&
                  purchasePrice.toDoubleOrNull() != null && effectiveType.isNotBlank()

    AlertDialog(
        onDismissRequest = { onDismiss(); onResetLookup() },
        title = { Text(if (isEditing) "Edit Investment" else "New Investment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // ── Ticker search (add mode) ──────────────────────────────
                if (!isEditing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tickerInput,
                            onValueChange = { tickerInput = it.uppercase(); onResetLookup() },
                            label = { Text("Ticker symbol") },
                            placeholder = { Text("AAPL, BTC-EUR…") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = { onLookupTicker(tickerInput) },
                            enabled = tickerInput.isNotBlank() && tickerLookup !is TickerLookupState.Loading,
                            modifier = Modifier.padding(top = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Forest)
                        ) {
                            if (tickerLookup is TickerLookupState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Search")
                            }
                        }
                    }

                    when (tickerLookup) {
                        is TickerLookupState.Found -> {
                            OutlinedCard(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Forest.copy(alpha = 0.4f)),
                                colors = CardDefaults.outlinedCardColors(containerColor = Forest.copy(alpha = 0.06f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null,
                                        tint = Forest, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text(tickerLookup.name, fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium)
                                        Text("${tickerLookup.type} · ${String.format("%.2f", tickerLookup.price)} ${tickerLookup.currency}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        is TickerLookupState.NotFound ->
                            Text("Ticker not found — fill in details manually below.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error)
                        is TickerLookupState.Error ->
                            Text("Network error — fill in details manually below.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error)
                        else -> {}
                    }

                    HorizontalDivider()
                }

                // ── Ticker field (edit mode only) ─────────────────────────
                if (isEditing) {
                    OutlinedTextField(value = tickerInput, onValueChange = { tickerInput = it.uppercase() },
                        label = { Text("Ticker symbol") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }

                // ── Name ─────────────────────────────────────────────────
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                // ── Type ─────────────────────────────────────────────────
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedType, onValueChange = {}, readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        PREDEFINED_TYPES.forEach { t ->
                            DropdownMenuItem(text = { Text(t) },
                                onClick = { selectedType = t; typeExpanded = false })
                        }
                        if (customTypes.isNotEmpty()) {
                            HorizontalDivider()
                            customTypes.forEach { t ->
                                DropdownMenuItem(text = { Text(t) },
                                    onClick = { selectedType = t; typeExpanded = false })
                            }
                        }
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("Custom…") },
                            onClick = { selectedType = "Custom"; typeExpanded = false })
                    }
                    // Invisible overlay to trigger the dropdown
                    Surface(
                        modifier = Modifier.matchParentSize(),
                        color = Color.Transparent,
                        onClick = { typeExpanded = true }
                    ) {}
                }

                if (selectedType == "Custom") {
                    OutlinedTextField(value = customTypeName, onValueChange = { customTypeName = it },
                        label = { Text("Custom type name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }

                // ── Quantity ──────────────────────────────────────────────
                OutlinedTextField(value = quantity, onValueChange = { quantity = it },
                    label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

                // ── Purchase price ────────────────────────────────────────
                OutlinedTextField(value = purchasePrice, onValueChange = { purchasePrice = it },
                    label = { Text("Purchase price per unit") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), prefix = { Text("€") })

                // ── Current price (manual only when not auto-fetch) ───────
                if (found == null || !autoFetch) {
                    OutlinedTextField(value = manualPrice, onValueChange = { manualPrice = it },
                        label = { Text("Current price per unit") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        prefix = { Text("€") })
                }

                // ── Auto-fetch toggle ─────────────────────────────────────
                if (tickerInput.isNotBlank() && (found != null || isEditing)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-update price", style = MaterialTheme.typography.bodyMedium)
                            Text("Fetches live price on app open",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoFetch,
                            onCheckedChange = { autoFetch = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Forest,
                                checkedTrackColor = Forest.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(InvestmentEntity(
                        id = investment?.id ?: 0L,
                        name = name.trim(),
                        ticker = tickerInput.trim(),
                        type = effectiveType,
                        quantity = quantity.toDoubleOrNull() ?: 0.0,
                        purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                        currentPrice = currentPrice,
                        purchaseDate = investment?.purchaseDate ?: System.currentTimeMillis(),
                        notes = investment?.notes ?: "",
                        autoFetch = autoFetch && tickerInput.isNotBlank(),
                        currency = currency,
                        lastFetchedAt = investment?.lastFetchedAt
                    ))
                },
                enabled = canSave
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = { onDismiss(); onResetLookup() }) { Text("Cancel") } }
    )
}

@Composable
private fun UpdatePriceDialog(
    investment: InvestmentEntity, onDismiss: () -> Unit, onUpdate: (Double) -> Unit
) {
    var price by remember { mutableStateOf(investment.currentPrice.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Price — ${investment.name}") },
        text = {
            OutlinedTextField(
                value = price, onValueChange = { price = it },
                label = { Text("Current price per unit") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("€") }
            )
        },
        confirmButton = {
            TextButton(onClick = { price.toDoubleOrNull()?.let { onUpdate(it) } },
                enabled = price.toDoubleOrNull() != null) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
