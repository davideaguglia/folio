package com.personal.financeapp.ui.screens.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.financeapp.data.local.entity.InvestmentEntity
import com.personal.financeapp.data.local.entity.InvestmentPriceEntity
import com.personal.financeapp.data.repository.InvestmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val PREDEFINED_TYPES = setOf("STOCK", "BOND", "GOLD", "OTHER")

data class InvestmentsUiState(
    val investments: List<InvestmentEntity> = emptyList(),
    val totalValue: Double = 0.0,
    val totalCost: Double = 0.0,
    val gainLoss: Double = 0.0,
    val gainLossPct: Double = 0.0,
    val typeBreakdown: List<Pair<String, Double>> = emptyList()
)

@HiltViewModel
class InvestmentsViewModel @Inject constructor(
    private val repository: InvestmentRepository
) : ViewModel() {

    val uiState: StateFlow<InvestmentsUiState> = repository.getAll()
        .map { investments ->
            val totalValue = investments.sumOf { it.currentPrice * it.quantity }
            val totalCost = investments.sumOf { it.purchasePrice * it.quantity }
            val gainLoss = totalValue - totalCost
            val gainLossPct = if (totalCost > 0) gainLoss / totalCost * 100 else 0.0
            val typeBreakdown = investments
                .groupBy { it.type }
                .map { (type, invs) -> type to invs.sumOf { it.currentPrice * it.quantity } }
                .sortedByDescending { it.second }
            InvestmentsUiState(investments, totalValue, totalCost, gainLoss, gainLossPct, typeBreakdown)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InvestmentsUiState())

    val customTypes: StateFlow<List<String>> = repository.getDistinctTypes()
        .map { types -> types.filter { it !in PREDEFINED_TYPES } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getPriceHistory(investmentId: Long): Flow<List<InvestmentPriceEntity>> =
        repository.getPriceHistory(investmentId)

    fun insert(investment: InvestmentEntity) = viewModelScope.launch { repository.insert(investment) }
    fun update(investment: InvestmentEntity) = viewModelScope.launch { repository.update(investment) }
    fun delete(investment: InvestmentEntity) = viewModelScope.launch { repository.delete(investment) }

    fun recordPrice(investmentId: Long, price: Double, date: Long) = viewModelScope.launch {
        repository.recordPrice(investmentId, price, date)
    }
}
