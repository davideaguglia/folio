package com.personal.financeapp.data.repository

import com.personal.financeapp.data.local.dao.CashSettingsDao
import com.personal.financeapp.data.local.entity.CashSettingsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CashSettingsRepository @Inject constructor(private val dao: CashSettingsDao) {
    fun getSettings(): Flow<CashSettingsEntity?> = dao.getSettings()
    suspend fun setInitialCash(amount: Double) = dao.upsert(CashSettingsEntity(initialCash = amount))
}
