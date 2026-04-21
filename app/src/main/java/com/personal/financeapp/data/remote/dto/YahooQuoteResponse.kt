package com.personal.financeapp.data.remote.dto

data class YahooChartResponse(
    val chart: ChartResponse
)

data class ChartResponse(
    val result: List<ChartResult>?,
    val error: ChartError?
)

data class ChartResult(
    val meta: ChartMeta
)

data class ChartMeta(
    val symbol: String?,
    val currency: String?,
    val regularMarketPrice: Double?,
    val instrumentType: String?,
    val longName: String?,
    val shortName: String?
)

data class ChartError(
    val code: String?,
    val description: String?
)
