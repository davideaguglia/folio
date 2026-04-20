package com.personal.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cash_settings")
data class CashSettingsEntity(
    @PrimaryKey val id: Long = 1L,
    val initialCash: Double = 0.0
)
