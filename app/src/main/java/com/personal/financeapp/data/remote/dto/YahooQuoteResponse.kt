package com.personal.financeapp.data.remote.dto

data class YahooQuoteResponse(
    val quoteResponse: QuoteResponse
)

data class QuoteResponse(
    val result: List<QuoteResult>?,
    val error: Any?
)

data class QuoteResult(
    val symbol: String,
    val longName: String?,
    val shortName: String?,
    val regularMarketPrice: Double?,
    val currency: String?,
    val quoteType: String?
)
