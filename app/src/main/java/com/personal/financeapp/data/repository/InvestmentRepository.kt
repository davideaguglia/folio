package com.personal.financeapp.data.repository

import com.personal.financeapp.data.local.dao.InvestmentDao
import com.personal.financeapp.data.local.dao.InvestmentPriceDao
import com.personal.financeapp.data.local.entity.InvestmentEntity
import com.personal.financeapp.data.local.entity.InvestmentPriceEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvestmentRepository @Inject constructor(
    private val investmentDao: InvestmentDao,
    private val priceDao: InvestmentPriceDao
) {
    fun getAll(): Flow<List<InvestmentEntity>> = investmentDao.getAll()
    fun getTotalPortfolioValue(): Flow<Double> = investmentDao.getTotalPortfolioValue()
    fun getDistinctTypes(): Flow<List<String>> = investmentDao.getDistinctTypes()
    suspend fun getById(id: Long): InvestmentEntity? = investmentDao.getById(id)

    fun getPriceHistory(investmentId: Long): Flow<List<InvestmentPriceEntity>> =
        priceDao.getByInvestment(investmentId)

    suspend fun insert(investment: InvestmentEntity): Long = investmentDao.insert(investment)
    suspend fun update(investment: InvestmentEntity) = investmentDao.update(investment)
    suspend fun delete(investment: InvestmentEntity) = investmentDao.delete(investment)

    suspend fun recordPrice(investmentId: Long, price: Double, date: Long) {
        priceDao.insert(InvestmentPriceEntity(investmentId = investmentId, price = price, date = date))
        val investment = investmentDao.getById(investmentId) ?: return
        investmentDao.update(investment.copy(currentPrice = price))
    }
}
