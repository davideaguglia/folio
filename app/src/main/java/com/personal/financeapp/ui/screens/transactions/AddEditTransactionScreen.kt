package com.personal.financeapp.ui.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.financeapp.data.local.entity.TransactionEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    transactionId: Long = -1L,
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditing = transactionId != -1L
    val existing = remember(transactionId, state.transactions) {
        state.transactions.find { it.transaction.id == transactionId }?.transaction
    }

    var type by remember { mutableStateOf(existing?.type ?: "EXPENSE") }
    var amount by remember { mutableStateOf(existing?.amount?.toString() ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var selectedCategoryId by remember { mutableStateOf(existing?.categoryId ?: 0L) }
    var selectedAccountId by remember { mutableStateOf(existing?.accountId ?: 0L) }
    var selectedDate by remember { mutableStateOf(existing?.date ?: System.currentTimeMillis()) }
    var isRecurring by remember { mutableStateOf(existing?.isRecurring ?: false) }
    var recurringPeriod by remember { mutableStateOf(existing?.recurringPeriod ?: "MONTHLY") }

    var showDatePicker by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }
    var periodExpanded by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val filteredCategories = state.categories.filter { it.type == type }

    // Auto-select first category when type changes or categories load
    LaunchedEffect(type, filteredCategories) {
        if (selectedCategoryId == 0L && filteredCategories.isNotEmpty()) {
            selectedCategoryId = filteredCategories.first().id
        }
    }
    LaunchedEffect(state.accounts) {
        if (selectedAccountId == 0L && state.accounts.isNotEmpty()) {
            selectedAccountId = state.accounts.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Transaction" else "Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("EXPENSE", "INCOME").forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { type = t; selectedCategoryId = 0L },
                        label = { Text(t.replaceFirstChar { it.titlecase() }) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (€)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                prefix = { Text("€") }
            )

            // Category
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = filteredCategories.find { it.id == selectedCategoryId }?.name ?: "Select category",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    filteredCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = { selectedCategoryId = cat.id; categoryExpanded = false }
                        )
                    }
                }
            }

            // Account
            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { accountExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.accounts.find { it.id == selectedAccountId }?.name ?: "Select account",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(accountExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    state.accounts.forEach { acc ->
                        DropdownMenuItem(
                            text = { Text(acc.name) },
                            onClick = { selectedAccountId = acc.id; accountExpanded = false }
                        )
                    }
                }
            }

            // Date
            OutlinedTextField(
                value = dateFormat.format(Date(selectedDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, "Pick date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Recurring
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Recurring transaction", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isRecurring, onCheckedChange = { isRecurring = it })
            }

            if (isRecurring) {
                ExposedDropdownMenuBox(
                    expanded = periodExpanded,
                    onExpandedChange = { periodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = recurringPeriod.replaceFirstChar { it.titlecase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeats") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(periodExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = periodExpanded,
                        onDismissRequest = { periodExpanded = false }
                    ) {
                        listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY").forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.replaceFirstChar { it.titlecase() }) },
                                onClick = { recurringPeriod = p; periodExpanded = false }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull() ?: return@Button
                    val nextDate = if (isRecurring) calculateNextDate(selectedDate, recurringPeriod) else null
                    val entity = TransactionEntity(
                        id = if (isEditing) transactionId else 0L,
                        amount = amountVal,
                        type = type,
                        categoryId = selectedCategoryId,
                        accountId = selectedAccountId,
                        date = selectedDate,
                        description = description,
                        isRecurring = isRecurring,
                        recurringPeriod = if (isRecurring) recurringPeriod else null,
                        recurringNextDate = nextDate
                    )
                    if (isEditing) viewModel.update(entity) else viewModel.insert(entity)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.toDoubleOrNull() != null && selectedCategoryId != 0L && selectedAccountId != 0L
            ) {
                Text(if (isEditing) "Update" else "Save Transaction")
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = pickerState) }
    }
}

private fun calculateNextDate(from: Long, period: String): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = from }
    when (period) {
        "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
        "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
        "MONTHLY" -> cal.add(Calendar.MONTH, 1)
        "YEARLY" -> cal.add(Calendar.YEAR, 1)
    }
    return cal.timeInMillis
}
