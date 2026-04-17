package com.personal.financeapp.util

import android.content.Context
import com.personal.financeapp.data.local.dao.TransactionWithDetails
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    fun export(context: Context, transactions: List<TransactionWithDetails>): String {
        val sb = StringBuilder()
        sb.appendLine("Date,Type,Amount,Category,Account,Description,Recurring")
        transactions.forEach { item ->
            val tx = item.transaction
            sb.appendLine(
                "${dateFormat.format(Date(tx.date))}," +
                "${tx.type}," +
                "${tx.amount}," +
                "\"${item.category?.name ?: ""}\"," +
                "\"${item.account?.name ?: ""}\"," +
                "\"${tx.description.replace("\"", "\"\"")}\"," +
                "${tx.isRecurring}"
            )
        }

        val dir = context.getExternalFilesDir("exports") ?: context.filesDir
        dir.mkdirs()
        val file = File(dir, "finance_export_${fileNameFormat.format(Date())}.csv")
        file.writeText(sb.toString())
        return file.absolutePath
    }
}
