package com.personal.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_DEFAULT
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.SET_DEFAULT
        )
    ],
    indices = [Index("categoryId"), Index("accountId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: String,         // INCOME | EXPENSE
    val categoryId: Long = 1,
    val accountId: Long = 1,
    val date: Long,           // epoch millis
    val description: String = "",
    val isRecurring: Boolean = false,
    val recurringPeriod: String? = null, // DAILY | WEEKLY | MONTHLY | YEARLY
    val recurringNextDate: Long? = null
)
