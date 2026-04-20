package com.personal.financeapp.data.repository

import com.personal.financeapp.data.local.entity.NetWorthSnapshotEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetWorthService @Inject constructor(
    private val investmentRepo: InvestmentRepository,
    private val cashSettingsRepo: CashSettingsRepository,
    private val transactionRepo: TransactionRepository,
    private val netWorthRepo: NetWorthRepository
) {
    suspend fun refreshSnapshot() {
        val portfolio = investmentRepo.getTotalPortfolioValue().first()
        val initialCash = cashSettingsRepo.getSettings().first()?.initialCash ?: 0.0
        val income = transactionRepo.getSumByTypeAllTime("INCOME").first()
        val expense = transactionRepo.getSumByTypeAllTime("EXPENSE").first()
        val cash = initialCash + income - expense
        val netWorth = portfolio + cash
        netWorthRepo.insert(
            NetWorthSnapshotEntity(
                totalAssets = netWorth,
                totalLiabilities = 0.0,
                netWorth = netWorth,
                date = System.currentTimeMillis()
            )
        )
    }
}
