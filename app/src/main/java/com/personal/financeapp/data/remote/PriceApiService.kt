package com.personal.financeapp.data.remote

import com.personal.financeapp.data.remote.dto.YahooQuoteResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PriceApiService {
    @GET("v7/finance/quote")
    suspend fun getQuote(
        @Query("symbols") symbols: String,
        @Query("fields") fields: String = "regularMarketPrice,currency,longName,quoteType"
    ): YahooQuoteResponse
}
