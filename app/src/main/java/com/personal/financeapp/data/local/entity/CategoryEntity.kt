package com.personal.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // INCOME | EXPENSE
    val color: String,
    val icon: String,
    val monthlyBudget: Double? = null
)
