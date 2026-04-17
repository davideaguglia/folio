package com.personal.financeapp.ui.screens.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.financeapp.data.local.dao.TransactionWithDetails
import com.personal.financeapp.ui.theme.ExpenseRed
import com.personal.financeapp.ui.theme.IncomeGreen
import com.personal.financeapp.util.CurrencyFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var searchVisible by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filter row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.filterType == null,
                    onClick = { viewModel.setFilter(null) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = state.filterType == "INCOME",
                    onClick = { viewModel.setFilter("INCOME") },
                    label = { Text("Income") }
                )
                FilterChip(
                    selected = state.filterType == "EXPENSE",
                    onClick = { viewModel.setFilter("EXPENSE") },
                    label = { Text("Expenses") }
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { searchVisible = !searchVisible }) {
                    Icon(Icons.Default.Search, "Search")
                }
            }
            AnimatedVisibility(searchVisible) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearch(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text("Search transactions…") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearch("") }) {
                                Icon(Icons.Default.Close, "Clear")
                            }
                        }
                    }
                )
            }

            if (state.transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.transactions, key = { it.transaction.id }) { item ->
                        SwipeToDismissTransactionItem(
                            item = item,
                            onEdit = { onEditTransaction(item.transaction.id) },
                            onDelete = {
                                viewModel.delete(item.transaction)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Transaction deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.insert(item.transaction)
                                    }
                                }
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissTransactionItem(
    item: TransactionWithDetails,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true }
            else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        }
    ) {
        TransactionItem(item = item, onClick = onEdit)
    }
}

@Composable
private fun TransactionItem(item: TransactionWithDetails, onClick: () -> Unit) {
    val tx = item.transaction
    val isIncome = tx.type == "INCOME"
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(tx.date))

    ListItem(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        headlineContent = {
            Text(
                item.category?.name ?: "Uncategorized",
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Column {
                if (tx.description.isNotBlank()) Text(tx.description, maxLines = 1)
                Text(
                    "${item.account?.name ?: "—"} · $dateStr",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyFormatter.formatWithSign(tx.amount, isIncome),
                    color = if (isIncome) IncomeGreen else ExpenseRed,
                    fontWeight = FontWeight.SemiBold
                )
                if (tx.isRecurring) {
                    Text("Recurring", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        leadingContent = {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape)
                    .background(if (isIncome) IncomeGreen.copy(.15f) else ExpenseRed.copy(.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (isIncome) IncomeGreen else ExpenseRed
                )
            }
        }
    )
}
