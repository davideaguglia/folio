package com.personal.financeapp.ui.screens.accounts

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.financeapp.data.local.entity.AccountEntity
import com.personal.financeapp.data.repository.AccountWithBalance
import com.personal.financeapp.ui.theme.Crimson
import com.personal.financeapp.ui.theme.Forest
import com.personal.financeapp.util.CurrencyFormatter

@Composable
fun AccountsScreen(viewModel: AccountsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<AccountEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editTarget = null; showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) { Icon(Icons.Default.Add, "Add account") }
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
                        .statusBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)
                ) {
                    Text(
                        "${state.accountsWithBalance.size} ACCOUNTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("Accounts", style = MaterialTheme.typography.headlineLarge)
                }
            }

            // ── Total balance hero ────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "LIQUID POSITION",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                CurrencyFormatter.format(state.totalBalance),
                                style = MaterialTheme.typography.displaySmall,
                                color = if (state.totalBalance >= 0) MaterialTheme.colorScheme.onSurface else Crimson
                            )
                        }
                    }
                }
            }

            // ── Account cards ─────────────────────────────────────
            items(state.accountsWithBalance, key = { it.account.id }) { item ->
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    AccountCard(
                        item = item,
                        onEdit = { editTarget = item.account; showDialog = true },
                        onDelete = { viewModel.delete(item.account) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AccountDialog(
            account = editTarget,
            onDismiss = { showDialog = false },
            onSave = { account ->
                if (editTarget == null) viewModel.insert(account) else viewModel.update(account)
                showDialog = false
            }
        )
    }
}

@Composable
private fun AccountCard(item: AccountWithBalance, onEdit: () -> Unit, onDelete: () -> Unit) {
    val acc = item.account
    val accentColor = runCatching { Color(android.graphics.Color.parseColor(acc.color)) }
        .getOrDefault(Forest)
    val isNegative = item.currentBalance < 0

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = accountTypeIcon(acc.type),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        acc.type.replace('_', ' ').lowercase().replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(acc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "CURRENT BALANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    CurrencyFormatter.format(item.currentBalance),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isNegative) Crimson else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun accountTypeIcon(type: String) = when (type) {
    "CHECKING"    -> Icons.Default.AccountBalance
    "SAVINGS"     -> Icons.Default.Savings
    "CREDIT_CARD" -> Icons.Default.CreditCard
    else          -> Icons.Default.AccountBalanceWallet
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDialog(account: AccountEntity?, onDismiss: () -> Unit, onSave: (AccountEntity) -> Unit) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    var type by remember { mutableStateOf(account?.type ?: "CHECKING") }
    var initialBalance by remember { mutableStateOf(account?.initialBalance?.toString() ?: "0") }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (account == null) "New Account" else "Edit Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Account name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = type.replace('_', ' ').replaceFirstChar { it.titlecase() },
                        onValueChange = {}, readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        listOf("CHECKING", "SAVINGS", "CASH", "CREDIT_CARD").forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.replace('_', ' ').replaceFirstChar { it.titlecase() }) },
                                onClick = { type = t; typeExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = initialBalance, onValueChange = { initialBalance = it },
                    label = { Text("Initial balance (€)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(), prefix = { Text("€") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(AccountEntity(
                        id = account?.id ?: 0L,
                        name = name, type = type,
                        initialBalance = initialBalance.toDoubleOrNull() ?: 0.0,
                        color = account?.color ?: "#2D5A3F",
                        icon = account?.icon ?: "account_balance"
                    ))
                },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
