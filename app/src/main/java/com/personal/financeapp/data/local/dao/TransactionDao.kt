package com.personal.financeapp.data.local.dao

import androidx.room.*
import com.personal.financeapp.data.local.entity.AccountEntity
import com.personal.financeapp.data.local.entity.CategoryEntity
import com.personal.financeapp.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class TransactionWithDetails(
    @Embedded val transaction: TransactionEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity?,
    @Relation(parentColumn = "accountId", entityColumn = "id")
    val account: AccountEntity?
)

data class CategoryTotal(
    val categoryId: Long,
    val total: Double
)

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllWithDetails(): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun getByDateRange(from: Long, to: Long): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<TransactionWithDetails>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = :type AND date BETWEEN :from AND :to")
    fun getSumByType(type: String, from: Long, to: Long): Flow<Double>

    @Query("SELECT categoryId, SUM(amount) as total FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :from AND :to GROUP BY categoryId")
    fun getCategoryTotals(from: Long, to: Long): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM transactions WHERE isRecurring = 1 AND recurringNextDate IS NOT NULL AND recurringNextDate <= :today")
    suspend fun getOverdueRecurring(today: Long): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
