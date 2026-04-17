package com.personal.financeapp.data.repository

import com.personal.financeapp.data.local.dao.CategoryTotal
import com.personal.financeapp.data.local.dao.TransactionDao
import com.personal.financeapp.data.local.dao.TransactionWithDetails
import com.personal.financeapp.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(private val dao: TransactionDao) {

    fun getAllWithDetails(): Flow<List<TransactionWithDetails>> = dao.getAllWithDetails()

    fun getByDateRange(from: Long, to: Long): Flow<List<TransactionWithDetails>> =
        dao.getByDateRange(from, to)

    fun getRecent(limit: Int = 5): Flow<List<TransactionWithDetails>> = dao.getRecent(limit)

    fun getSumByType(type: String, from: Long, to: Long): Flow<Double> =
        dao.getSumByType(type, from, to)

    fun getCategoryTotals(from: Long, to: Long): Flow<List<CategoryTotal>> =
        dao.getCategoryTotals(from, to)

    suspend fun getOverdueRecurring(today: Long): List<TransactionEntity> =
        dao.getOverdueRecurring(today)

    suspend fun insert(transaction: TransactionEntity) = dao.insert(transaction)
    suspend fun update(transaction: TransactionEntity) = dao.update(transaction)
    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
