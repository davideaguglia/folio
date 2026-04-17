package com.personal.financeapp.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.financeapp.data.local.dao.TransactionWithDetails
import com.personal.financeapp.data.local.entity.CategoryEntity
import com.personal.financeapp.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

data class CategoryExpense(val category: CategoryEntity, val amount: Double)

data class DashboardUiState(
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val netWorth: Double = 0.0,
    val recentTransactions: List<TransactionWithDetails> = emptyList(),
    val categoryExpenses: List<CategoryExpense> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val investmentRepo: InvestmentRepository,
    private val categoryRepo: CategoryRepository
) : ViewModel() {

    private val monthRange: Pair<Long, Long>
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
            val from = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
            return from to cal.timeInMillis
        }

    val uiState: StateFlow<DashboardUiState> = combine(
        combine(
            transactionRepo.getSumByType("INCOME", monthRange.first, monthRange.second),
            transactionRepo.getSumByType("EXPENSE", monthRange.first, monthRange.second),
            transactionRepo.getRecent(5)
        ) { income, expense, recent -> Triple(income, expense, recent) },
        combine(
            transactionRepo.getCategoryTotals(monthRange.first, monthRange.second),
            categoryRepo.getAll()
        ) { totals, categories -> totals to categories }
    ) { (income, expense, recent), (catTotals, categories) ->
        val catMap = categories.associateBy { it.id }
        val catExpenses = catTotals.mapNotNull { ct ->
            catMap[ct.categoryId]?.let { CategoryExpense(it, ct.total) }
        }.sortedByDescending { it.amount }
        DashboardUiState(
            monthlyIncome = income,
            monthlyExpense = expense,
            netWorth = 0.0,
            recentTransactions = recent,
            categoryExpenses = catExpenses
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    // Net worth is derived separately to avoid the combine limit
    val netWorth: StateFlow<Double> = combine(
        accountRepo.getAll(),
        investmentRepo.getTotalPortfolioValue()
    ) { accounts, portfolioValue ->
        // For each account we need to fetch net tx sum - approximate here with flow combination
        portfolioValue // accounts balance added in the screen via repository
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}
