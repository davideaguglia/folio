package com.personal.financeapp.data.remote

import com.personal.financeapp.data.local.entity.InvestmentEntity
import com.personal.financeapp.data.repository.InvestmentRepository
import com.personal.financeapp.data.repository.NetWorthService
import javax.inject.Inject
import javax.inject.Singleton

sealed class TickerResult {
    data class Found(
        val name: String,
        val price: Double,
        val currency: String,
        val type: String
    ) : TickerResult()
    object NotFound : TickerResult()
    data class Error(val message: String) : TickerResult()
}

@Singleton
class PriceFetchService @Inject constructor(
    private val api: PriceApiService,
    private val investmentRepo: InvestmentRepository,
    private val netWorthService: NetWorthService
) {
    suspend fun lookup(ticker: String): TickerResult {
        return try {
            val response = api.getQuote(ticker.trim().uppercase())
            val result = response.quoteResponse.result?.firstOrNull()
            if (result == null || result.regularMarketPrice == null) {
                TickerResult.NotFound
            } else {
                val type = when (result.quoteType?.uppercase()) {
                    "EQUITY"         -> "STOCK"
                    "ETF"            -> "ETF"
                    "CRYPTOCURRENCY" -> "CRYPTO"
                    "MUTUALFUND"     -> "ETF"
                    else             -> "OTHER"
                }
                TickerResult.Found(
                    name = result.longName ?: result.shortName ?: ticker,
                    price = result.regularMarketPrice,
                    currency = result.currency ?: "USD",
                    type = type
                )
            }
        } catch (e: Exception) {
            TickerResult.Error(e.message ?: "Network error")
        }
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
