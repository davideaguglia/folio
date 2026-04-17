package com.personal.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "net_worth_snapshots")
data class NetWorthSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val totalAssets: Double,
    val totalLiabilities: Double,
    val netWorth: Double,
    val date: Long // epoch millis
)
