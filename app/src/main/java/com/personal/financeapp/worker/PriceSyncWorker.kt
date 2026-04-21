package com.personal.financeapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.personal.financeapp.data.remote.PriceFetchService
import com.personal.financeapp.data.repository.InvestmentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class PriceSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val priceFetchService: PriceFetchService,
    private val investmentRepo: InvestmentRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            investmentRepo.getAll().first()
                .filter { it.autoFetch && it.ticker.isNotBlank() }
                .forEach { priceFetchService.fetchAndRecord(it) }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "folio_price_sync_daily"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<PriceSyncWorker>(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
