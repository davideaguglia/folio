package com.personal.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // CHECKING | SAVINGS | CASH | CREDIT_CARD
    val initialBalance: Double,
    val color: String, // hex, e.g. "#4CAF50"
    val icon: String   // material icon name string
)
