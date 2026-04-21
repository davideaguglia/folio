package com.personal.financeapp.ui.screens.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.financeapp.data.local.entity.InvestmentEntity
import com.personal.financeapp.data.local.entity.InvestmentPriceEntity
import com.personal.financeapp.data.remote.PriceFetchService
import com.personal.financeapp.data.remote.TickerResult
import com.personal.financeapp.data.repository.CashSettingsRepository
import com.personal.financeapp.data.repository.InvestmentRepository
import com.personal.financeapp.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val PREDEFINED_TYPES = setOf("STOCK", "BOND", "GOLD", "OTHER")

sealed class TickerLookupState {
    object Idle : TickerLookupState()
    object Loading : TickerLookupState()
    data class Found(val name: String, val price: Double, val currency: String, val type: String) : TickerLookupState()
    object NotFound : TickerLookupState()
    data class Error(val message: String) : TickerLookupState()
}

data class InvestmentsUiState(
    val investments: List<InvestmentEntity> = emptyList(),
    val totalValue: Double = 0.0,
    val totalCost: Double = 0.0,
    val gainLoss: Double = 0.0,
    val gainLossPct: Double = 0.0,
    val typeBreakdown: List<Pair<String, Double>> = emptyList(),
    val cashAvailable: Double = 0.0,
    val initialCash: Double = 0.0,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class InvestmentsViewModel @Inject constructor(
    private val repository: InvestmentRepository,
    private val cashSettingsRepo: CashSettingsRepository,
    private val transactionRepo: TransactionRepository,
    private val priceFetchService: PriceFetchService
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    private val _tickerLookup = MutableStateFlow<TickerLookupState>(TickerLookupState.Idle)
    val tickerLookup: StateFlow<TickerLookupState> = _tickerLookup.asStateFlow()

    val uiState: StateFlow<InvestmentsUiState> = combine(
        repository.getAll(),
        cashSettingsRepo.getSettings(),
        transactionRepo.getSumByTypeAllTime("INCOME"),
        transactionRepo.getSumByTypeAllTime("EXPENSE"),
        _isRefreshing
    ) { investments, cashSettings, totalIncome, totalExpense, refreshing ->
        val totalValue = investments.sumOf { it.currentPrice * it.quantity }
        val totalCost = investments.sumOf { it.purchasePrice * it.quantity }
        val gainLoss = totalValue - totalCost
        val gainLossPct = if (totalCost > 0) gainLoss / totalCost * 100 else 0.0
        val typeBreakdown = investments
            .groupBy { it.type }
            .map { (type, invs) -> type to invs.sumOf { it.currentPrice * it.quantity } }
            .sortedByDescending { it.second }
        val initialCash = cashSettings?.initialCash ?: 0.0
        val cashAvailable = initialCash + totalIncome - totalExpense
        InvestmentsUiState(investments, totalValue, totalCost, gainLoss, gainLossPct,
            typeBreakdown, cashAvailable, initialCash, refreshing)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InvestmentsUiState())

    val customTypes: StateFlow<List<String>> = repository.getDistinctTypes()
        .map { types -> types.filter { it !in PREDEFINED_TYPES } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getPriceHistory(investmentId: Long): Flow<List<InvestmentPriceEntity>> =
        repository.getPriceHistory(investmentId)

    fun setInitialCash(amount: Double) = viewModelScope.launch {
        cashSettingsRepo.setInitialCash(amount)
    }

    fun lookupTicker(ticker: String) = viewModelScope.launch {
        _tickerLookup.value = TickerLookupState.Loading
        _tickerLookup.value = when (val r = priceFetchService.lookup(ticker)) {
            is TickerResult.Found   -> TickerLookupState.Found(r.name, r.price, r.currency, r.type)
            is TickerResult.NotFound -> TickerLookupState.NotFound
            is TickerResult.Error   -> TickerLookupState.Error(r.message)
        }
    }

    fun resetTickerLookup() { _tickerLookup.value = TickerLookupState.Idle }

    fun refreshAllPrices() = viewModelScope.launch {
        _isRefreshing.value = true
        uiState.value.investments
            .filter { it.autoFetch && it.ticker.isNotBlank() }
            .forEach { priceFetchService.fetchAndRecord(it) }
        _isRefreshing.value = false
    }

    fun insert(investment: InvestmentEntity) = viewModelScope.launch { repository.insert(investment) }

    fun insertAndFetch(investment: InvestmentEntity) = viewModelScope.launch {
        val id = repository.insert(investment)
        if (investment.autoFetch && investment.ticker.isNotBlank()) {
            priceFetchService.fetchAndRecord(investment.copy(id = id))
        }
    }

    fun update(investment: InvestmentEntity) = viewModelScope.launch { repository.update(investment) }
    fun delete(investment: InvestmentEntity) = viewModelScope.launch { repository.delete(investment) }

    fun recordPrice(investmentId: Long, price: Double, date: Long) = viewModelScope.launch {
        repository.recordPrice(investmentId, price, date)
    }
}
