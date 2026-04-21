package com.personal.financeapp.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.financeapp.data.local.dao.TransactionWithDetails
import com.personal.financeapp.data.local.entity.AccountEntity
import com.personal.financeapp.data.local.entity.CategoryEntity
import com.personal.financeapp.data.local.entity.TransactionEntity
import com.personal.financeapp.data.repository.AccountRepository
import com.personal.financeapp.data.repository.CategoryRepository
import com.personal.financeapp.data.repository.NetWorthService
import com.personal.financeapp.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionUiState(
    val transactions: List<TransactionWithDetails> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val filterType: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository,
    private val accountRepo: AccountRepository,
    private val netWorthService: NetWorthService
) : ViewModel() {

    private val filterType = MutableStateFlow<String?>(null)
    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<TransactionUiState> = combine(
        transactionRepo.getAllWithDetails(),
        categoryRepo.getAll(),
        accountRepo.getAll(),
        filterType,
        searchQuery
    ) { transactions, categories, accounts, type, query ->
        val filtered = transactions
            .filter { type == null || it.transaction.type == type }
            .filter { query.isBlank() || it.transaction.description.contains(query, ignoreCase = true) ||
                    it.category?.name?.contains(query, ignoreCase = true) == true }
        TransactionUiState(filtered, categories, accounts, type, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionUiState())

    fun setFilter(type: String?) { filterType.value = type }
    fun setSearch(query: String) { searchQuery.value = query }

    fun insert(transaction: TransactionEntity) = viewModelScope.launch {
        transactionRepo.insert(transaction)
        netWorthService.refreshSnapshot()
    }

    fun update(transaction: TransactionEntity) = viewModelScope.launch {
        transactionRepo.update(transaction)
        netWorthService.refreshSnapshot()
    }

    fun delete(transaction: TransactionEntity) = viewModelScope.launch {
        transactionRepo.delete(transaction)
        netWorthService.refreshSnapshot()
    }

    fun insertCategory(name: String, type: String) = viewModelScope.launch {
        val colors = listOf("#F44336","#2196F3","#FF9800","#4CAF50","#9C27B0","#00BCD4","#FF5722","#3F51B5")
        categoryRepo.insert(CategoryEntity(name = name, type = type, color = colors.random(), icon = "label", monthlyBudget = null))
    }

    fun insertAccount(name: String, type: String) = viewModelScope.launch {
        val color = when (type) {
            "CREDIT_CARD" -> "#5C6BC0"
            "SAVINGS"     -> "#009688"
            "CASH"        -> "#4CAF50"
            else          -> "#2D5A3F"
        }
        accountRepo.insert(AccountEntity(name = name, type = type, initialBalance = 0.0, color = color, icon = "account_balance"))
    }
}
