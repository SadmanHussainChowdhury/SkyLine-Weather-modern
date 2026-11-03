package com.example.data.repository

import com.squareup.moshi.JsonClass
import kotlin.math.abs
import java.util.Calendar

@JsonClass(generateAdapter = true)
data class HourlyPrecipitationItem(
    val time: String,
    val chancePercentage: Int, // 0 - 100
    val intensityMmPerHour: Double, // e.g. 0.0 to 15.0
    val intensityLabel: String, // "No Rain", "Light Drizzle", "Moderate Rain", "Heavy Rain", "Downpour"
    val conditionText: String
)

@JsonClass(generateAdapter = true)
data class PrecipitationForecastData(
    val city: String,
    val overallPeakChance: Int,
    val peakTime: String,
    val totalRainfallMm: Double,
    val summaryHeadline: String,
    val hourlyList: List<HourlyPrecipitationItem>
) {
    companion object {
        fun generateFromWeather(weather: WeatherData): PrecipitationForecastData {
            val citySeed = abs(weather.city.lowercase().hashCode())
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

            val condition = weather.conditionText.lowercase()
            val baseChance = when {
                condition.contains("thunder") || condition.contains("heavy rain") -> 85
                condition.contains("rain") || condition.contains("shower") || condition.contains("drizzle") -> 65
                condition.contains("overcast") || condition.contains("cloud") -> 35
                condition.contains("snow") || condition.contains("sleet") -> 55
                else -> 18
            }

            val hourlyItems = mutableListOf<HourlyPrecipitationItem>()
            var maxChance = 0
            var peakHourTime = "Now"
            var sumRainfall = 0.0

            for (i in 0 until 12) {
                val hourOfDay = (currentHour + i) % 24
                val timeLabel = if (i == 0) "Now" else {
                    val h12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                    val ampm = if (hourOfDay >= 12) "PM" else "AM"
                    "$h12 $ampm"
                }

                // Deterministic variance per hour based on seed and hour index
                val variation = ((citySeed + i * 17 + hourOfDay * 31) % 41) - 20
                val chance = (baseChance + variation).coerceIn(5, 95)

                if (chance > maxChance) {
                    maxChance = chance
                    peakHourTime = timeLabel
                }

                val intensityMm = when {
                    chance < 20 -> 0.0
                    chance < 40 -> 0.2 + ((citySeed + i) % 8) / 10.0
                    chance < 70 -> 1.0 + ((citySeed + i) % 20) / 10.0
                    chance < 85 -> 3.0 + ((citySeed + i) % 30) / 10.0
                    else -> 6.0 + ((citySeed + i) % 50) / 10.0
                }

                val intensityLabel = when {
                    intensityMm == 0.0 -> "No Rain"
                    intensityMm < 1.0 -> "Light Drizzle"
                    intensityMm < 3.0 -> "Moderate Rain"
                    intensityMm < 6.0 -> "Heavy Rain"
                    else -> "Downpour"
                }

                val condText = when {
                    intensityMm == 0.0 -> if (chance > 30) "Cloudy / Dry" else "Clear"
                    intensityMm < 1.0 -> "Passing Drizzle"
                    intensityMm < 3.0 -> "Steady Rain"
                    intensityMm < 6.0 -> "Heavy Rain Showers"
                    else -> "Severe Rain & Gusts"
                }

                sumRainfall += intensityMm * 0.4 // expected accumulation

                hourlyItems.add(
                    HourlyPrecipitationItem(
                        time = timeLabel,
                        chancePercentage = chance,
                        intensityMmPerHour = Math.round(intensityMm * 10) / 10.0,
                        intensityLabel = intensityLabel,
                        conditionText = condText
                    )
                )
            }

            val roundedTotalRain = Math.round(sumRainfall * 10) / 10.0
            val summaryHeadline = when {
                maxChance >= 75 -> "High rain risk around $peakHourTime ($maxChance% peak)"
                maxChance >= 45 -> "Scattered showers expected around $peakHourTime ($maxChance% peak)"
                else -> "Low precipitation probability over the next 12 hours ($maxChance% max)"
            }

            return PrecipitationForecastData(
                city = weather.city,
                overallPeakChance = maxChance,
                peakTime = peakHourTime,
                totalRainfallMm = roundedTotalRain,
                summaryHeadline = summaryHeadline,
                hourlyList = hourlyItems
            )
        }
    }
}
