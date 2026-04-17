package com.personal.financeapp.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val formatter = NumberFormat.getCurrencyInstance(Locale.ITALY).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    fun format(amount: Double): String = formatter.format(amount)

    fun formatWithSign(amount: Double, isIncome: Boolean): String {
        val sign = if (isIncome) "+" else "-"
        return "$sign${formatter.format(kotlin.math.abs(amount))}"
    }
}
