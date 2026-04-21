package com.personal.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val ticker: String = "",
    val type: String, // STOCK | CRYPTO | ETF | REAL_ESTATE | OTHER
    val quantity: Double,
    val purchasePrice: Double, // per unit, EUR
    val currentPrice: Double,  // per unit, EUR — updated manually
    val purchaseDate: Long,
    val notes: String = "",
    val autoFetch: Boolean = false,      // true = refresh price from API automatically
    val currency: String = "EUR",         // native currency returned by the price API
    val lastFetchedAt: Long? = null       // epoch ms of last successful price fetch
)
