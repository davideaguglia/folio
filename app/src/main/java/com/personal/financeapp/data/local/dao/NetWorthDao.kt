package com.personal.financeapp.data.local.dao

import androidx.room.*
import com.personal.financeapp.data.local.entity.NetWorthSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NetWorthDao {
    @Query("SELECT * FROM net_worth_snapshots ORDER BY date ASC")
    fun getAll(): Flow<List<NetWorthSnapshotEntity>>

    @Query("SELECT * FROM net_worth_snapshots ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): NetWorthSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: NetWorthSnapshotEntity): Long

    @Delete
    suspend fun delete(snapshot: NetWorthSnapshotEntity)
}
