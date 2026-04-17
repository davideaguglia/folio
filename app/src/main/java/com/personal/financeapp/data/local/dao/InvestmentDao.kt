package com.personal.financeapp.data.local.dao

import androidx.room.*
import com.personal.financeapp.data.local.entity.InvestmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments ORDER BY name ASC")
    fun getAll(): Flow<List<InvestmentEntity>>

    @Query("SELECT * FROM investments WHERE id = :id")
    suspend fun getById(id: Long): InvestmentEntity?

    @Query("SELECT COALESCE(SUM(currentPrice * quantity), 0.0) FROM investments")
    fun getTotalPortfolioValue(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(investment: InvestmentEntity): Long

    @Update
    suspend fun update(investment: InvestmentEntity)

    @Delete
    suspend fun delete(investment: InvestmentEntity)
}
