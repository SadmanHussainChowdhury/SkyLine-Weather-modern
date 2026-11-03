package com.example.data.repository

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherData(
    val city: String,
    val tempC: Double,
    val tempF: Double,
    val conditionText: String,
    val conditionCode: Int,
    val humidity: Int,
    val windKph: Double,
    val windMph: Double,
    val windDegree: Int = 180,
    val windDir: String = "S",
    val pressureMb: Double,
    val uv: Double,
    val sunrise: String,
    val sunset: String,
    val moonPhase: String = "New Moon",
    val moonIllumination: Int = 0,
    val moonrise: String = "08:00 PM",
    val moonset: String = "06:00 AM",
    val feelsLikeC: Double,
    val feelsLikeF: Double,
    val visibilityKm: Double = 10.0,
    val visibilityMiles: Double = 6.2,
    val aqi: Int, // EPA index: 1=Good, 2=Moderate, 3=Unhealthy for sensitive, 4=Unhealthy, 5=Very Unhealthy, 6=Hazardous
    val alertHeadline: String?,
    val alertDesc: String?,
    val hourlyForecast: List<HourlyForecastItem>,
    val dailyForecast: List<DailyForecastItem>,
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class HourlyForecastItem(
    val time: String, // e.g. "08:00 AM" or "20:00"
    val tempC: Double,
    val tempF: Double,
    val conditionText: String,
    val conditionCode: Int,
    val isDay: Boolean,
    val epoch: Long
)

@JsonClass(generateAdapter = true)
data class DailyForecastItem(
    val date: String, // e.g. "Monday" or "07/05"
    val maxTempC: Double,
    val maxTempF: Double,
    val minTempC: Double,
    val minTempF: Double,
    val conditionText: String,
    val conditionCode: Int,
    val uv: Double
)
