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
        NetWorthSnapshotEntity::class,
        CashSettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun investmentPriceDao(): InvestmentPriceDao
    abstract fun netWorthDao(): NetWorthDao
    abstract fun cashSettingsDao(): CashSettingsDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "finance_db")
                    .fallbackToDestructiveMigration()
                    .addCallback(SeedCallback())
                    .build()
                    .also { INSTANCE = it }
            }
    }

    private class SeedCallback : Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            CoroutineScope(Dispatchers.IO).launch {
                val cursor = db.query("SELECT COUNT(*) FROM categories")
                cursor.moveToFirst()
                val count = cursor.getInt(0)
                cursor.close()
                if (count == 0) {
                    seedCategories(db)
                    seedAccount(db)
                } else {
                    syncCategoryColors(db)
                }
            }
        }

        // Keeps seeded category colors in sync across installs/updates
        private fun syncCategoryColors(db: SupportSQLiteDatabase) {
            val colors = mapOf(
                "Food & Dining"      to "#C4683A",
                "Transportation"     to "#4A6FA5",
                "Housing"            to "#B08A3E",
                "Healthcare"         to "#A94436",
                "Entertainment"      to "#7B5EA7",
                "Shopping"           to "#3E8A87",
                "Education"          to "#2D5A3F",
                "Travel"             to "#4A5F8A",
                "Personal Care"      to "#9B6B4F",
                "Other"              to "#7A7D6E",
                "Salary"             to "#2D5A3F",
                "Freelance"          to "#4D8060",
                "Investment Returns" to "#B08A3E",
                "Gift"               to "#B8864A",
                "Other Income"       to "#7A6B52"
            )
            colors.forEach { (name, hex) ->
                db.execSQL("UPDATE categories SET color = ? WHERE name = ?", arrayOf(hex, name))
            }
        }

        private fun seedCategories(db: SupportSQLiteDatabase) {
            val expenseCategories = listOf(
                Triple("Food & Dining",  "#C4683A", "restaurant"),
                Triple("Transportation", "#4A6FA5", "directions_car"),
                Triple("Housing",        "#B08A3E", "home"),
                Triple("Healthcare",     "#A94436", "favorite"),
                Triple("Entertainment",  "#7B5EA7", "movie"),
                Triple("Shopping",       "#3E8A87", "shopping_cart"),
                Triple("Education",      "#2D5A3F", "school"),
                Triple("Travel",         "#4A5F8A", "flight"),
                Triple("Personal Care",  "#9B6B4F", "person"),
                Triple("Other",          "#7A7D6E", "more_horiz")
            )
            val incomeCategories = listOf(
                Triple("Salary",             "#2D5A3F", "work"),
                Triple("Freelance",          "#4D8060", "laptop"),
                Triple("Investment Returns", "#B08A3E", "trending_up"),
                Triple("Gift",               "#B8864A", "card_giftcard"),
                Triple("Other Income",       "#7A6B52", "add_circle")
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
                "INSERT INTO accounts (name, type, initialBalance, color, icon) VALUES (?, 'CREDIT_CARD', 0.0, ?, ?)",
                arrayOf("Card", "#5C6BC0", "credit_card")
            )
            db.execSQL(
                "INSERT INTO accounts (name, type, initialBalance, color, icon) VALUES (?, 'CASH', 0.0, ?, ?)",
                arrayOf("Cash", "#4CAF50", "account_balance_wallet")
            )
        }
    }
}
