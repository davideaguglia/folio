package com.personal.financeapp.data.repository

import com.personal.financeapp.data.local.dao.AccountDao
import com.personal.financeapp.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

data class AccountWithBalance(
    val account: AccountEntity,
    val currentBalance: Double
)

@Singleton
class AccountRepository @Inject constructor(private val dao: AccountDao) {

    fun getAll(): Flow<List<AccountEntity>> = dao.getAll()

    suspend fun getWithBalance(account: AccountEntity): AccountWithBalance {
        val netTx = dao.getNetTransactionSum(account.id)
        return AccountWithBalance(account, account.initialBalance + netTx)
    }

    suspend fun getAllWithBalances(accounts: List<AccountEntity>): List<AccountWithBalance> =
        accounts.map { getWithBalance(it) }

    suspend fun insert(account: AccountEntity) = dao.insert(account)
    suspend fun update(account: AccountEntity) = dao.update(account)
    suspend fun delete(account: AccountEntity) = dao.delete(account)
}
