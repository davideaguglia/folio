package com.personal.financeapp.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.financeapp.data.local.entity.CategoryEntity
import com.personal.financeapp.data.local.entity.NetWorthSnapshotEntity
import com.personal.financeapp.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class MonthlyData(val label: String, val income: Double, val expense: Double)
data class CategoryBreakdown(val category: CategoryEntity, val amount: Double, val percent: Float)

data class ReportsUiState(
    val monthlyData: List<MonthlyData> = emptyList(),
    val categoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val netWorthHistory: List<NetWorthSnapshotEntity> = emptyList(),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository,
    private val netWorthRepo: NetWorthRepository,
    private val accountRepo: AccountRepository,
    private val investmentRepo: InvestmentRepository
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    val uiState: StateFlow<ReportsUiState> = combine(
        netWorthRepo.getAll(),
        categoryRepo.getAll(),
        selectedMonth,
        selectedYear,
        transactionRepo.getAllWithDetails()
    ) { snapshots, categories, month, year, allTx ->
        // Monthly bar chart: last 12 months
        val monthlyData = buildMonthlyData(allTx, year, month)

        // Category breakdown for selected month
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        val from = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        val to = cal.timeInMillis

        val monthTx = allTx.filter { it.transaction.date in from..to && it.transaction.type == "EXPENSE" }
        val catMap = categories.associateBy { it.id }
        val catSpend = monthTx.groupBy { it.transaction.categoryId }
            .mapValues { (_, txs) -> txs.sumOf { it.transaction.amount } }
        val total = catSpend.values.sum().coerceAtLeast(0.01)
        val breakdown = catSpend.mapNotNull { (catId, amount) ->
            catMap[catId]?.let { CategoryBreakdown(it, amount, (amount / total).toFloat()) }
        }.sortedByDescending { it.amount }

        ReportsUiState(monthlyData, breakdown, snapshots, month, year)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState())

    fun selectMonth(month: Int, year: Int) {
        selectedMonth.value = month
        selectedYear.value = year
    }

    fun recordNetWorthSnapshot(totalAssets: Double, totalLiabilities: Double) = viewModelScope.launch {
        netWorthRepo.insert(
            NetWorthSnapshotEntity(
                totalAssets = totalAssets,
                totalLiabilities = totalLiabilities,
                netWorth = totalAssets - totalLiabilities,
                date = System.currentTimeMillis()
            )
        )
    }

    private fun buildMonthlyData(
        allTx: List<com.personal.financeapp.data.local.dao.TransactionWithDetails>,
        currentYear: Int,
        currentMonth: Int
    ): List<MonthlyData> {
        val months = mutableListOf<MonthlyData>()
        val cal = Calendar.getInstance()
        cal.set(currentYear, currentMonth, 1)
        for (i in 11 downTo 0) {
            val c = Calendar.getInstance()
            c.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0)
            c.add(Calendar.MONTH, -i)
            val from = c.timeInMillis
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59)
            val to = c.timeInMillis
            val label = "${c.get(Calendar.MONTH) + 1}/${c.get(Calendar.YEAR) % 100}"
            val income = allTx.filter { it.transaction.date in from..to && it.transaction.type == "INCOME" }
                .sumOf { it.transaction.amount }
            val expense = allTx.filter { it.transaction.date in from..to && it.transaction.type == "EXPENSE" }
                .sumOf { it.transaction.amount }
            months.add(MonthlyData(label, income, expense))
        }
        return months
    }
}
