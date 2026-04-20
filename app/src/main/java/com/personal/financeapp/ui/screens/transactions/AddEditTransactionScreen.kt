package com.personal.financeapp.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.financeapp.data.local.entity.TransactionEntity
import com.personal.financeapp.ui.theme.Forest
import com.personal.financeapp.ui.theme.GoldTone
import com.personal.financeapp.ui.theme.Terra
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

    var type            by remember { mutableStateOf("EXPENSE") }
    var amount          by remember { mutableStateOf("") }
    var description     by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(0L) }
    var selectedAccountId  by remember { mutableStateOf(0L) }
    var selectedDate    by remember { mutableStateOf(System.currentTimeMillis()) }
    var isRecurring     by remember { mutableStateOf(false) }
    var recurringPeriod by remember { mutableStateOf("MONTHLY") }

    var formInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(existing) {
        if (existing != null && !formInitialized) {
            type               = existing.type
            amount             = existing.amount.toString()
            description        = existing.description
            selectedCategoryId = existing.categoryId
            selectedAccountId  = existing.accountId
            selectedDate       = existing.date
            isRecurring        = existing.isRecurring
            recurringPeriod    = existing.recurringPeriod ?: "MONTHLY"
            formInitialized    = true
        }
    }

    var showDatePicker   by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var accountExpanded  by remember { mutableStateOf(false) }
    var periodExpanded   by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("EEE, MMM dd · HH:mm", Locale.getDefault()) }
    val eyebrowDateFormat = remember { SimpleDateFormat("MMMM dd · EEEE", Locale.getDefault()) }
    val filteredCategories = state.categories.filter { it.type == type }

    LaunchedEffect(type, filteredCategories) {
        if (selectedCategoryId == 0L && filteredCategories.isNotEmpty()) {
            selectedCategoryId = filteredCategories.first().id
        }
    }
    LaunchedEffect(type) {
        if (!isEditing) selectedCategoryId = 0L
    }
    LaunchedEffect(state.accounts) {
        if (selectedAccountId == 0L && state.accounts.isNotEmpty()) {
            selectedAccountId = (state.accounts.firstOrNull { it.type == "CREDIT_CARD" }
                ?: state.accounts.first()).id
        }
    }

    val selectedCategory = filteredCategories.find { it.id == selectedCategoryId }
    val selectedAccount  = state.accounts.find { it.id == selectedAccountId }

    val amountColor = when (type) {
        "INCOME" -> Forest
        else     -> MaterialTheme.colorScheme.onSurface
    }

    val canSave = amount.toDoubleOrNull() != null && selectedCategoryId != 0L && selectedAccountId != 0L

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 20.dp, end = 12.dp, top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        eyebrowDateFormat.format(Date(selectedDate)).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (isEditing) "Edit entry" else "New entry",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                IconButton(
                    onClick = {
                        val amountVal = amount.toDoubleOrNull() ?: return@IconButton
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
                    enabled = canSave,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (canSave) Forest else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Default.Check, "Save",
                        tint = if (canSave) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ── Segmented type selector ───────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(100.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf("EXPENSE" to "Expense", "INCOME" to "Income").forEach { (k, label) ->
                    val selected = type == k
                    val segBg = when {
                        selected && k == "INCOME"   -> Forest
                        selected && k == "EXPENSE"  -> Terra
                        else                        -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(100.dp))
                            .background(segBg)
                            .clickable { type = k; if (!isEditing) selectedCategoryId = 0L }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = when {
                                selected -> MaterialTheme.colorScheme.onPrimary
                                else     -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // ── Amount display ────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp, horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "AMOUNT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "€",
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp, end = 4.dp)
                    )
                    Text(
                        if (amount.isEmpty()) "0" else amount,
                        fontSize = 64.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-1).sp,
                        color = amountColor,
                        lineHeight = 64.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                // Quick-add chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.weight(1f))
                    listOf("5", "10", "20", "50", "100").forEach { quick ->
                        OutlinedButton(
                            onClick = {
                                val cur = amount.toDoubleOrNull() ?: 0.0
                                amount = (cur + quick.toDouble()).let {
                                    if (it == it.toLong().toDouble()) it.toLong().toString()
                                    else it.toString()
                                }
                            },
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("€$quick", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                }
            }

            // ── Field card ────────────────────────────────────────
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                // Amount field (editable text)
                FieldRow(
                    label = "AMOUNT",
                    value = if (amount.isEmpty()) "Enter amount" else "€ $amount",
                    muted = amount.isEmpty()
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { v ->
                            if (v.matches(Regex("[0-9]*\\.?[0-9]*"))) amount = v
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        label = { Text("Amount (€)") },
                        prefix = { Text("€") }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                // Category
                Box {
                    FieldRowClickable(
                        label = "CATEGORY",
                        value = selectedCategory?.name ?: "Select category",
                        muted = selectedCategory == null,
                        leadingColor = selectedCategory?.color?.let { hex ->
                            runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
                        },
                        onClick = { categoryExpanded = true }
                    )
                    DropdownMenu(
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

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                // Account
                Box {
                    FieldRowClickable(
                        label = "ACCOUNT",
                        value = selectedAccount?.name ?: "Select account",
                        muted = selectedAccount == null,
                        onClick = { accountExpanded = true }
                    )
                    DropdownMenu(
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

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                // Date
                FieldRowClickable(
                    label = "DATE",
                    value = dateFormat.format(Date(selectedDate)),
                    onClick = { showDatePicker = true }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                // Note
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        "NOTE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Add a note…", fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                // Recurring
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "RECURRING",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            if (isRecurring) recurringPeriod.lowercase()
                                .replaceFirstChar { it.titlecase() }
                            else "Does not repeat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isRecurring) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Forest, checkedTrackColor = Forest.copy(alpha = 0.3f))
                    )
                }

                if (isRecurring) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    Box {
                        FieldRowClickable(
                            label = "REPEATS",
                            value = recurringPeriod.lowercase().replaceFirstChar { it.titlecase() },
                            onClick = { periodExpanded = true }
                        )
                        DropdownMenu(
                            expanded = periodExpanded,
                            onDismissRequest = { periodExpanded = false }
                        ) {
                            listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY").forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.lowercase().replaceFirstChar { it.titlecase() }) },
                                    onClick = { recurringPeriod = p; periodExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f).defaultMinSize(minHeight = 24.dp))

            // ── Bottom buttons ────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
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
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                    modifier = Modifier.weight(2f).height(52.dp),
                    enabled = canSave
                ) {
                    Text(
                        if (isEditing) "Update entry" else "Save entry",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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

@Composable
private fun FieldRow(
    label: String,
    value: String,
    muted: Boolean = false,
    content: (@Composable () -> Unit)? = null
) {
    if (content != null) {
        content()
    } else {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(3.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FieldRowClickable(
    label: String,
    value: String,
    muted: Boolean = false,
    leadingColor: Color? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (leadingColor != null) {
                    Box(Modifier.size(8.dp).background(leadingColor, CircleShape))
                }
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Icon(
            Icons.Default.ArrowForwardIos, null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun calculateNextDate(from: Long, period: String): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = from }
    when (period) {
        "DAILY"   -> cal.add(Calendar.DAY_OF_YEAR, 1)
        "WEEKLY"  -> cal.add(Calendar.WEEK_OF_YEAR, 1)
        "MONTHLY" -> cal.add(Calendar.MONTH, 1)
        "YEARLY"  -> cal.add(Calendar.YEAR, 1)
    }
    return cal.timeInMillis
}
