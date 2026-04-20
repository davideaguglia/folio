package com.personal.financeapp.data.local.dao

import androidx.room.*
import com.personal.financeapp.data.local.entity.CashSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashSettingsDao {
    @Query("SELECT * FROM cash_settings WHERE id = 1")
    fun getSettings(): Flow<CashSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CashSettingsEntity)
}
