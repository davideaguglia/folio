package com.personal.financeapp.data.remote

import com.personal.financeapp.data.remote.dto.YahooChartResponse
import com.personal.financeapp.data.remote.dto.YahooSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PriceApiService {
    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol") symbol: String,
        @Query("interval") interval: String = "1d",
        @Query("range") range: String = "1d"
    ): YahooChartResponse

    @GET("v1/finance/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("quotesCount") quotesCount: Int = 5,
        @Query("newsCount") newsCount: Int = 0,
        @Query("enableFuzzyQuery") fuzzy: Boolean = false
    ): YahooSearchResponse
}
