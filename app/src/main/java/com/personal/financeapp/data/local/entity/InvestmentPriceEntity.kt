package com.personal.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "investment_prices",
    foreignKeys = [
        ForeignKey(
            entity = InvestmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["investmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("investmentId")]
)
data class InvestmentPriceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val investmentId: Long,
    val price: Double,
    val date: Long // epoch millis
)
