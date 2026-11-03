package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey val city: String,
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
    val aqi: Int, // US EPA Index (1-6) or general AQI value
    val alertHeadline: String?,
    val alertDesc: String?,
    val hourlyJson: String, // Serialized list of hourly forecast items
    val dailyJson: String,  // Serialized list of daily forecast items
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_cities")
data class FavoriteCityEntity(
    @PrimaryKey val cityName: String,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val cityName: String,
    val searchedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "monthly_climate_trends",
    primaryKeys = ["city", "month"]
)
data class MonthlyClimateEntity(
    val city: String,
    val month: Int, // 1 = Jan, 2 = Feb, ..., 12 = Dec
    val monthName: String, // "Jan", "Feb", "Mar", etc.
    val avgHighC: Double,
    val avgLowC: Double,
    val avgHighF: Double,
    val avgLowF: Double,
    val precipMm: Double,
    val precipInches: Double,
    val rainyDays: Int,
    val sunshineHours: Int,
    val historicalMinC: Double = avgLowC - 4.5,
    val historicalMaxC: Double = avgHighC + 4.5
)

@Entity(tableName = "cached_weather_maps")
data class CachedWeatherMapEntity(
    @PrimaryKey val city: String,
    val lat: Double,
    val lon: Double,
    val zoomLevel: Int = 10,
    val boundingBoxNorthLat: Double,
    val boundingBoxSouthLat: Double,
    val boundingBoxEastLon: Double,
    val boundingBoxWestLon: Double,
    val primaryLayerType: String = "PRECIPITATION_RADAR",
    val radarGridDataJson: String,
    val isOfflineAvailable: Boolean = true,
    val tileCountCached: Int = 16,
    val mapWidthPx: Int = 600,
    val mapHeightPx: Int = 400,
    val cachedAtTimestamp: Long = System.currentTimeMillis()
)

