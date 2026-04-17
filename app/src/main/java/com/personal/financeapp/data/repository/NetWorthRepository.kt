package com.personal.financeapp.data.repository

import com.personal.financeapp.data.local.dao.NetWorthDao
import com.personal.financeapp.data.local.entity.NetWorthSnapshotEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetWorthRepository @Inject constructor(private val dao: NetWorthDao) {
    fun getAll(): Flow<List<NetWorthSnapshotEntity>> = dao.getAll()
    suspend fun getLatest(): NetWorthSnapshotEntity? = dao.getLatest()
    suspend fun insert(snapshot: NetWorthSnapshotEntity) = dao.insert(snapshot)
    suspend fun delete(snapshot: NetWorthSnapshotEntity) = dao.delete(snapshot)
}
