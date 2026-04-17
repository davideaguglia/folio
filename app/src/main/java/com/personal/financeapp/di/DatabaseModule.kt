package com.personal.financeapp.di

import android.content.Context
import com.personal.financeapp.data.local.AppDatabase
import com.personal.financeapp.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideInvestmentDao(db: AppDatabase): InvestmentDao = db.investmentDao()
    @Provides fun provideInvestmentPriceDao(db: AppDatabase): InvestmentPriceDao = db.investmentPriceDao()
    @Provides fun provideNetWorthDao(db: AppDatabase): NetWorthDao = db.netWorthDao()
}
