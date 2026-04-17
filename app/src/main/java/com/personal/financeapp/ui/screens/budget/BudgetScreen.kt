package com.personal.financeapp.ui.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.financeapp.data.local.entity.CategoryEntity
import com.personal.financeapp.ui.theme.ExpenseRed
import com.personal.financeapp.ui.theme.IncomeGreen
import com.personal.financeapp.ui.theme.WarningAmber
import com.personal.financeapp.util.CurrencyFormatter

@Composable
fun BudgetScreen(viewModel: BudgetViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val allCategories by viewModel.allExpenseCategories.collectAsStateWithLifecycle()
    var editTarget by remember { mutableStateOf<CategoryEntity?>(null) }
    var showNoBudgetCategories by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showNoBudgetCategories = true }) {
                Icon(Icons.Default.Add, "Add budget")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.items.isNotEmpty()) {
                item { OverallBudgetCard(state.totalSpent, state.totalBudget) }
                items(state.items, key = { it.category.id }) { item ->
                    BudgetProgressCard(item, onEdit = { editTarget = item.category })
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
        val noBudgetCats = allCategories.filter { it.monthlyBudget == null || it.monthlyBudget == 0.0 }
        AlertDialog(
            onDismissRequest = { showNoBudgetCategories = false },
            title = { Text("Set Budget") },
            text = {
                Column {
                    if (noBudgetCats.isEmpty()) {
                        Text("All expense categories already have budgets.")
                    } else {
                        noBudgetCats.forEach { cat ->
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

@Composable
private fun OverallBudgetCard(spent: Double, budget: Double) {
    val progress = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Budget", style = MaterialTheme.typography.titleMedium)
                Text(CurrencyFormatter.format(budget), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = progressColor(progress),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Spent: ${CurrencyFormatter.format(spent)}",
                    style = MaterialTheme.typography.bodyMedium)
                Text("Left: ${CurrencyFormatter.format((budget - spent).coerceAtLeast(0.0))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BudgetProgressCard(item: CategoryBudgetProgress, onEdit: () -> Unit) {
    val progress = item.progress.coerceIn(0f, 1.5f)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.category.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${CurrencyFormatter.format(item.spent)} / ${CurrencyFormatter.format(item.budget)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, "Edit budget", modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = progressColor(progress),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            if (item.progress > 1f) {
                Text(
                    "Over budget by ${CurrencyFormatter.format(item.spent - item.budget)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ExpenseRed
                )
            }
        }
    }
}

private fun progressColor(progress: Float): Color =
    when {
        progress >= 1f -> ExpenseRed
        progress >= 0.7f -> WarningAmber
        else -> IncomeGreen
    }

@Composable
private fun BudgetDialog(
    category: CategoryEntity,
    onDismiss: () -> Unit,
    onSave: (Double?) -> Unit
) {
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
        confirmButton = {
            TextButton(onClick = {
                onSave(budget.toDoubleOrNull())
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = { onSave(null) }) { Text("Remove budget") }
        }
    )
}
