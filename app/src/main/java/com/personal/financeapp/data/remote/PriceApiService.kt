package com.personal.financeapp.data.remote

import com.personal.financeapp.data.remote.dto.YahooChartResponse
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
}
