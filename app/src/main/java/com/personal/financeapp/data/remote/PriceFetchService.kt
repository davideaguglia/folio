package com.personal.financeapp.data.remote

import com.personal.financeapp.data.local.entity.InvestmentEntity
import com.personal.financeapp.data.repository.InvestmentRepository
import com.personal.financeapp.data.repository.NetWorthService
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

sealed class TickerResult {
    data class Found(
        val name: String,
        val price: Double,
        val currency: String,
        val type: String,
        val ticker: String  // resolved symbol (may differ from input when ISIN was given)
    ) : TickerResult()
    object NotFound : TickerResult()
    data class Error(val message: String) : TickerResult()
}

private val ISIN_REGEX = Regex("^[A-Z]{2}[A-Z0-9]{10}$")

@Singleton
class PriceFetchService @Inject constructor(
    private val api: PriceApiService,
    private val investmentRepo: InvestmentRepository,
    private val netWorthService: NetWorthService
) {
    suspend fun lookup(input: String): TickerResult {
        return try {
            val query = input.trim().uppercase()
            // If input looks like an ISIN, resolve it to a ticker first
            val ticker = if (ISIN_REGEX.matches(query)) resolveIsin(query) ?: return TickerResult.NotFound
                         else query
            val response = api.getChart(ticker)
            val meta = response.chart.result?.firstOrNull()?.meta
            if (meta == null || meta.regularMarketPrice == null) {
                TickerResult.NotFound
            } else {
                val type = when (meta.instrumentType?.uppercase()) {
                    "EQUITY"         -> "STOCK"
                    "ETF"            -> "ETF"
                    "CRYPTOCURRENCY" -> "CRYPTO"
                    "MUTUALFUND"     -> "ETF"
                    else             -> "OTHER"
                }
                TickerResult.Found(
                    name     = meta.longName ?: meta.shortName ?: ticker,
                    price    = meta.regularMarketPrice,
                    currency = meta.currency ?: "USD",
                    type     = type,
                    ticker   = meta.symbol ?: ticker
                )
            }
        } catch (e: HttpException) {
            if (e.code() == 404) TickerResult.NotFound
            else TickerResult.Error("HTTP ${e.code()} — ${e.message()}")
        } catch (e: Exception) {
            TickerResult.Error(e.message ?: e.javaClass.simpleName)
        }
    }

    private suspend fun resolveIsin(isin: String): String? {
        return try {
            api.search(isin).quotes
                ?.firstOrNull { !it.symbol.isNullOrBlank() }
                ?.symbol
        } catch (_: Exception) { null }
    }

    suspend fun fetchAndRecord(investment: InvestmentEntity) {
        if (!investment.autoFetch || investment.ticker.isBlank()) return
        try {
            val result = lookup(investment.ticker)
            if (result is TickerResult.Found) {
                val now = System.currentTimeMillis()
                investmentRepo.recordPriceWithMeta(
                    investmentId = investment.id,
                    price = result.price,
                    currency = result.currency,
                    date = now
                )
                netWorthService.refreshSnapshot()
            }
        } catch (_: Exception) { /* silent fail — no crash on background price refresh */ }
    }
}
