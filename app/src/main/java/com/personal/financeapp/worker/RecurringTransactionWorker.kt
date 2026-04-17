package com.personal.financeapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.personal.financeapp.data.local.entity.TransactionEntity
import com.personal.financeapp.data.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepository: TransactionRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val today = System.currentTimeMillis()
        val overdue = transactionRepository.getOverdueRecurring(today)
        overdue.forEach { template ->
            val nextDate = template.recurringNextDate ?: return@forEach
            // Create concrete transaction for this occurrence
            transactionRepository.insert(
                TransactionEntity(
                    amount = template.amount,
                    type = template.type,
                    categoryId = template.categoryId,
                    accountId = template.accountId,
                    date = nextDate,
                    description = template.description,
                    isRecurring = false
                )
            )
            // Advance the template's next due date
            val advanced = calculateNextDate(nextDate, template.recurringPeriod ?: "MONTHLY")
            transactionRepository.update(template.copy(recurringNextDate = advanced))
        }
        return Result.success()
    }

    private fun calculateNextDate(from: Long, period: String): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = from }
        when (period) {
            "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
            "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            "MONTHLY" -> cal.add(Calendar.MONTH, 1)
            "YEARLY" -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }

    companion object {
        const val WORK_NAME = "RecurringTransactionWork"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<RecurringTransactionWorker>()
                .setConstraints(Constraints.Builder().build())
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
