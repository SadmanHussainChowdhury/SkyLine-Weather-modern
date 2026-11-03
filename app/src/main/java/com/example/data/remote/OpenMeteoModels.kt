package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoGeocodingResponse(
    val results: List<GeocodingResult>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    val id: Long? = null,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null,
    val timezone: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null,
    val current: OpenMeteoCurrent? = null,
    val hourly: OpenMeteoHourly? = null,
    val daily: OpenMeteoDaily? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
    @Json(name = "temperature_2m") val temperature2m: Double,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int,
    @Json(name = "apparent_temperature") val apparentTemperature: Double,
    @Json(name = "is_day") val isDay: Int,
    val precipitation: Double? = 0.0,
    @Json(name = "weather_code") val weatherCode: Int,
    @Json(name = "surface_pressure") val surfacePressure: Double,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double,
    @Json(name = "wind_direction_10m") val windDirection10m: Int
)

@JsonClass(generateAdapter = true)
data class OpenMeteoHourly(
    val time: List<String>,
    @Json(name = "temperature_2m") val temperature2m: List<Double>,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "is_day") val isDay: List<Int>? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDaily(
    val time: List<String>,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>,
    val sunrise: List<String>? = null,
    val sunset: List<String>? = null,
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>? = null
)
