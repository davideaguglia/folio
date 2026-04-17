package com.personal.financeapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.personal.financeapp.data.local.dao.*
import com.personal.financeapp.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        InvestmentEntity::class,
        InvestmentPriceEntity::class,
        NetWorthSnapshotEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun investmentPriceDao(): InvestmentPriceDao
    abstract fun netWorthDao(): NetWorthDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "finance_db")
                    .addCallback(SeedCallback())
                    .build()
                    .also { INSTANCE = it }
            }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                seedCategories(db)
                seedAccount(db)
            }
        }

        private fun seedCategories(db: SupportSQLiteDatabase) {
            val expenseCategories = listOf(
                Triple("Food & Dining", "#F44336", "restaurant"),
                Triple("Transportation", "#2196F3", "directions_car"),
                Triple("Housing", "#FF9800", "home"),
                Triple("Healthcare", "#E91E63", "favorite"),
                Triple("Entertainment", "#9C27B0", "movie"),
                Triple("Shopping", "#00BCD4", "shopping_cart"),
                Triple("Education", "#009688", "school"),
                Triple("Travel", "#3F51B5", "flight"),
                Triple("Personal Care", "#FF5722", "person"),
                Triple("Other", "#607D8B", "more_horiz")
            )
            val incomeCategories = listOf(
                Triple("Salary", "#4CAF50", "work"),
                Triple("Freelance", "#8BC34A", "laptop"),
                Triple("Investment Returns", "#CDDC39", "trending_up"),
                Triple("Gift", "#FFC107", "card_giftcard"),
                Triple("Other Income", "#795548", "add_circle")
            )
            expenseCategories.forEach { (name, color, icon) ->
                db.execSQL(
                    "INSERT INTO categories (name, type, color, icon, monthlyBudget) VALUES (?, 'EXPENSE', ?, ?, NULL)",
                    arrayOf(name, color, icon)
                )
            }
            incomeCategories.forEach { (name, color, icon) ->
                db.execSQL(
                    "INSERT INTO categories (name, type, color, icon, monthlyBudget) VALUES (?, 'INCOME', ?, ?, NULL)",
                    arrayOf(name, color, icon)
                )
            }
        }

        private fun seedAccount(db: SupportSQLiteDatabase) {
            db.execSQL(
                "INSERT INTO accounts (name, type, initialBalance, color, icon) VALUES (?, 'CASH', 0.0, ?, ?)",
                arrayOf("Cash", "#4CAF50", "account_balance_wallet")
            )
        }
    }
}
