package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrentPollen(
    @Json(name = "alder_pollen") val alderPollen: Double? = 0.0,
    @Json(name = "birch_pollen") val birchPollen: Double? = 0.0,
    @Json(name = "grass_pollen") val grassPollen: Double? = 0.0,
    @Json(name = "mugwort_pollen") val mugwortPollen: Double? = 0.0,
    @Json(name = "olive_pollen") val olivePollen: Double? = 0.0,
    @Json(name = "ragweed_pollen") val ragweedPollen: Double? = 0.0,
    @Json(name = "pm10") val pm10: Double? = 0.0,
    @Json(name = "pm2_5") val pm25: Double? = 0.0
)

@JsonClass(generateAdapter = true)
data class OpenMeteoPollenResponse(
    @Json(name = "latitude") val latitude: Double? = 0.0,
    @Json(name = "longitude") val longitude: Double? = 0.0,
    @Json(name = "current") val current: OpenMeteoCurrentPollen? = null
)

interface PollenApiService {
    @GET("v1/air-quality")
    suspend fun getPollenData(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "alder_pollen,birch_pollen,grass_pollen,mugwort_pollen,olive_pollen,ragweed_pollen,pm10,pm2_5"
    ): OpenMeteoPollenResponse
}
