package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("days") days: Int = 7,
        @Query("aqi") aqi: String = "yes",
        @Query("alerts") alerts: String = "yes"
    ): WeatherResponse
}
