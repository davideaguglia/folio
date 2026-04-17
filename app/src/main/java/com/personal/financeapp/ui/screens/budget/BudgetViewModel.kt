package com.personal.financeapp.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.financeapp.data.local.entity.CategoryEntity
import com.personal.financeapp.data.repository.CategoryRepository
import com.personal.financeapp.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CategoryBudgetProgress(
    val category: CategoryEntity,
    val spent: Double,
    val budget: Double,
    val progress: Float // 0.0 - 1.0+
)

data class BudgetUiState(
    val items: List<CategoryBudgetProgress> = emptyList(),
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val categoryRepo: CategoryRepository,
    private val transactionRepo: TransactionRepository
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

    val uiState: StateFlow<BudgetUiState> = combine(
        categoryRepo.getByType("EXPENSE"),
        transactionRepo.getCategoryTotals(monthRange.first, monthRange.second)
    ) { categories, totals ->
        val spentMap = totals.associate { it.categoryId to it.total }
        val items = categories
            .filter { it.monthlyBudget != null && it.monthlyBudget > 0 }
            .map { cat ->
                val spent = spentMap[cat.id] ?: 0.0
                val budget = cat.monthlyBudget!!
                CategoryBudgetProgress(cat, spent, budget, (spent / budget).toFloat())
            }
            .sortedByDescending { it.progress }
        BudgetUiState(
            items = items,
            totalBudget = items.sumOf { it.budget },
            totalSpent = items.sumOf { it.spent }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetUiState())

    val allExpenseCategories: StateFlow<List<CategoryEntity>> = categoryRepo.getByType("EXPENSE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateBudget(category: CategoryEntity, budget: Double?) = viewModelScope.launch {
        categoryRepo.update(category.copy(monthlyBudget = budget))
    }
}
