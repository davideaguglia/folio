package com.personal.financeapp.data.local.dao

import androidx.room.*
import com.personal.financeapp.data.local.entity.InvestmentPriceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentPriceDao {
    @Query("SELECT * FROM investment_prices WHERE investmentId = :investmentId ORDER BY date ASC")
    fun getByInvestment(investmentId: Long): Flow<List<InvestmentPriceEntity>>

    @Query("SELECT * FROM investment_prices WHERE investmentId = :investmentId ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(investmentId: Long): InvestmentPriceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(price: InvestmentPriceEntity): Long

    @Delete
    suspend fun delete(price: InvestmentPriceEntity)
}
