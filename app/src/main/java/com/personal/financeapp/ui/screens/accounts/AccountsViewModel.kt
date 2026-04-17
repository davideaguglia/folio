package com.personal.financeapp.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.financeapp.data.local.entity.AccountEntity
import com.personal.financeapp.data.repository.AccountRepository
import com.personal.financeapp.data.repository.AccountWithBalance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountsUiState(
    val accountsWithBalance: List<AccountWithBalance> = emptyList(),
    val totalBalance: Double = 0.0
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val repository: AccountRepository
) : ViewModel() {

    val uiState: StateFlow<AccountsUiState> = repository.getAll()
        .map { accounts ->
            val withBalances = repository.getAllWithBalances(accounts)
            AccountsUiState(
                accountsWithBalance = withBalances,
                totalBalance = withBalances.sumOf { it.currentBalance }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccountsUiState())

    fun insert(account: AccountEntity) = viewModelScope.launch { repository.insert(account) }
    fun update(account: AccountEntity) = viewModelScope.launch { repository.update(account) }
    fun delete(account: AccountEntity) = viewModelScope.launch { repository.delete(account) }
}
