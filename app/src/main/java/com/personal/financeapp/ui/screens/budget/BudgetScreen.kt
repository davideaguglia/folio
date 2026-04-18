package com.personal.financeapp.ui.screens.budget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.financeapp.data.local.entity.CategoryEntity
import com.personal.financeapp.ui.components.DonutChart
import com.personal.financeapp.ui.theme.Crimson
import com.personal.financeapp.ui.theme.Forest
import com.personal.financeapp.ui.theme.ForestSoft
import com.personal.financeapp.ui.theme.GoldTone
import com.personal.financeapp.util.CurrencyFormatter
import java.util.Calendar

@Composable
fun BudgetScreen(viewModel: BudgetViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val allCategories by viewModel.allExpenseCategories.collectAsStateWithLifecycle()
    var editTarget by remember { mutableStateOf<CategoryEntity?>(null) }
    var showNoBudgetCategories by remember { mutableStateOf(false) }

    val monthName = remember {
        val months = arrayOf("January","February","March","April","May","June",
            "July","August","September","October","November","December")
        months[Calendar.getInstance().get(Calendar.MONTH)]
    }
    val daysLeft = remember {
        val cal = Calendar.getInstance()
        cal.getActualMaximum(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH)
    }
    val spendPct = if (state.totalBudget > 0) (state.totalSpent / state.totalBudget).toFloat() else 0f

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNoBudgetCategories = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) { Icon(Icons.Default.Add, "Add budget") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // ── Header ────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)
                ) {
                    Text(
                        "$daysLeft DAYS REMAINING",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("$monthName Budget", style = MaterialTheme.typography.headlineLarge)
                }
            }

            if (state.items.isNotEmpty()) {
                // ── Donut hero card ───────────────────────────────
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                                    val donutData = state.items
                                        .filter { it.spent > 0 }
                                        .map { parseCategoryColor(it.category.color) to it.spent }
                                        .ifEmpty { listOf(MaterialTheme.colorScheme.outlineVariant to 1.0) }
                                    DonutChart(data = donutData, modifier = Modifier.fillMaxSize())
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${(spendPct * 100).toInt()}%",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = progressColor(spendPct)
                                        )
                                        Text(
                                            "USED",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "SPENT SO FAR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        CurrencyFormatter.format(state.totalSpent),
                                        style = MaterialTheme.typography.displaySmall,
                                        color = progressColor(spendPct)
                                    )
                                    Text(
                                        "of ${CurrencyFormatter.format(state.totalBudget)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    val left = (state.totalBudget - state.totalSpent).coerceAtLeast(0.0)
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = if (spendPct < 0.85f) ForestSoft else GoldTone.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            "${CurrencyFormatter.format(left)} left",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (spendPct < 0.85f) Forest else GoldTone,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Category section header ───────────────────────
                item {
                    Text(
                        "BY CATEGORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
                    )
                }

                // ── Category rows card ────────────────────────────
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            state.items.forEachIndexed { i, item ->
                                val p = item.progress.coerceAtLeast(0f)
                                val over = p > 1f
                                val catColor = parseCategoryColor(item.category.color)
                                val last = i == state.items.lastIndex
                                Column(
                                    modifier = Modifier.padding(
                                        start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(28.dp).background(catColor.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(Modifier.size(10.dp).background(catColor, CircleShape))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.category.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                            Text(
                                                if (over) "Over by ${CurrencyFormatter.format(item.spent - item.budget)}"
                                                else "${CurrencyFormatter.format((item.budget - item.spent).coerceAtLeast(0.0))} left",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (over) Crimson else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "${CurrencyFormatter.format(item.spent)} / ${CurrencyFormatter.format(item.budget)}",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            IconButton(
                                                onClick = { editTarget = item.category },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { p.coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(5.dp),
                                        color = progressColor(p),
                                        trackColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                                if (!last) HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("No budgets set", style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Tap + to add a monthly budget for a category",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    editTarget?.let { cat ->
        BudgetDialog(
            category = cat,
            onDismiss = { editTarget = null },
            onSave = { budget -> viewModel.updateBudget(cat, budget); editTarget = null }
        )
    }

    if (showNoBudgetCategories) {
        val noBudget = allCategories.filter { it.monthlyBudget == null || it.monthlyBudget == 0.0 }
        AlertDialog(
            onDismissRequest = { showNoBudgetCategories = false },
            title = { Text("Set Budget") },
            text = {
                Column {
                    if (noBudget.isEmpty()) {
                        Text("All expense categories already have budgets.")
                    } else {
                        noBudget.forEach { cat ->
                            TextButton(
                                onClick = { editTarget = cat; showNoBudgetCategories = false },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(cat.name) }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showNoBudgetCategories = false }) { Text("Close") } }
        )
    }
}

private fun parseCategoryColor(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)

private fun progressColor(progress: Float): Color = when {
    progress >= 1f    -> Crimson
    progress >= 0.75f -> GoldTone
    else              -> Forest
}

@Composable
private fun BudgetDialog(category: CategoryEntity, onDismiss: () -> Unit, onSave: (Double?) -> Unit) {
    var budget by remember { mutableStateOf(category.monthlyBudget?.toString() ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Budget — ${category.name}") },
        text = {
            OutlinedTextField(
                value = budget, onValueChange = { budget = it },
                label = { Text("Monthly limit (€)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("€") }
            )
        },
        confirmButton = { TextButton(onClick = { onSave(budget.toDoubleOrNull()) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = { onSave(null) }) { Text("Remove budget") } }
    )
}
