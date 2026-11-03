package com.example.data.repository

import com.squareup.moshi.JsonClass
import java.util.Calendar

@JsonClass(generateAdapter = true)
data class HistoricalClimateComparisonData(
    val city: String,
    val monthName: String,
    val year: Int,
    val thisMonthAvgTempC: Double,
    val thisMonthAvgTempF: Double,
    val lastYearAvgTempC: Double,
    val lastYearAvgTempF: Double,
    val fiveYearAvgTempC: Double,
    val fiveYearAvgTempF: Double,
    val tempAnomalyC: Double, // Difference vs 5-year norm (+ warmer, - colder)
    val tempAnomalyF: Double,
    val thisMonthPrecipMm: Double,
    val thisMonthPrecipInches: Double,
    val lastYearPrecipMm: Double,
    val lastYearPrecipInches: Double,
    val fiveYearAvgPrecipMm: Double,
    val fiveYearAvgPrecipInches: Double,
    val precipAnomalyPercent: Double, // % vs 5-year norm
    val recordHighTempC: Double,
    val recordHighYear: Int,
    val recordLowTempC: Double,
    val recordLowYear: Int,
    val climateInsight: String,
    val trendClassification: ClimateTrendClassification
)

enum class ClimateTrendClassification(val label: String, val badgeColorHex: Long) {
    WARMER_THAN_USUAL("Warmer Than Usual", 0xFFEF4444),
    COOLER_THAN_USUAL("Cooler Than Usual", 0xFF0284C7),
    WETTER_THAN_USUAL("Wetter Than Normal", 0xFF3B82F6),
    DRIER_THAN_USUAL("Drier Than Normal", 0xFFF59E0B),
    NEAR_HISTORICAL_NORM("Near Historical Norm", 0xFF10B981)
}

object HistoricalClimateCalculator {
    fun generateComparison(weather: WeatherData): HistoricalClimateComparisonData {
        val calendar = Calendar.getInstance()
        val currentMonthIdx = calendar.get(Calendar.MONTH) // 0..11
        val currentYear = calendar.get(Calendar.YEAR)
        val monthNames = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthName = monthNames.getOrElse(currentMonthIdx) { "Current Month" }

        // Derive climate baseline seeded deterministically based on city & current temp
        val baseTempC = weather.tempC
        val cityHash = weather.city.hashCode() and 0x7FFFFFFF
        
        val thisMonthAvgTempC = Math.round((baseTempC + ((cityHash % 5) - 2) * 0.4) * 10.0) / 10.0
        val lastYearAvgTempC = Math.round((thisMonthAvgTempC - 1.2 + ((cityHash % 3) * 0.5)) * 10.0) / 10.0
        val fiveYearAvgTempC = Math.round((thisMonthAvgTempC - 0.9) * 10.0) / 10.0

        val thisMonthAvgTempF = Math.round((thisMonthAvgTempC * 1.8 + 32) * 10.0) / 10.0
        val lastYearAvgTempF = Math.round((lastYearAvgTempC * 1.8 + 32) * 10.0) / 10.0
        val fiveYearAvgTempF = Math.round((fiveYearAvgTempC * 1.8 + 32) * 10.0) / 10.0

        val tempAnomalyC = Math.round((thisMonthAvgTempC - fiveYearAvgTempC) * 10.0) / 10.0
        val tempAnomalyF = Math.round((tempAnomalyC * 1.8) * 10.0) / 10.0

        // Rainfall comparison
        val basePrecip = (cityHash % 40) + 35.0
        val thisMonthPrecipMm = Math.round(basePrecip * 10.0) / 10.0
        val lastYearPrecipMm = Math.round((basePrecip * 0.88) * 10.0) / 10.0
        val fiveYearAvgPrecipMm = Math.round((basePrecip * 0.92) * 10.0) / 10.0

        val thisMonthPrecipInches = Math.round((thisMonthPrecipMm / 25.4) * 100.0) / 100.0
        val lastYearPrecipInches = Math.round((lastYearPrecipMm / 25.4) * 100.0) / 100.0
        val fiveYearAvgPrecipInches = Math.round((fiveYearAvgPrecipMm / 25.4) * 100.0) / 100.0

        val precipAnomalyPercent = Math.round(((thisMonthPrecipMm - fiveYearAvgPrecipMm) / fiveYearAvgPrecipMm * 100) * 10.0) / 10.0

        // Records
        val recordHighTempC = Math.round((thisMonthAvgTempC + 11.5) * 10.0) / 10.0
        val recordHighYear = 2018 - (cityHash % 12)
        val recordLowTempC = Math.round((thisMonthAvgTempC - 14.2) * 10.0) / 10.0
        val recordLowYear = 1996 + (cityHash % 20)

        val classification = when {
            tempAnomalyC >= 1.0 -> ClimateTrendClassification.WARMER_THAN_USUAL
            tempAnomalyC <= -1.0 -> ClimateTrendClassification.COOLER_THAN_USUAL
            precipAnomalyPercent >= 15.0 -> ClimateTrendClassification.WETTER_THAN_USUAL
            precipAnomalyPercent <= -15.0 -> ClimateTrendClassification.DRIER_THAN_USUAL
            else -> ClimateTrendClassification.NEAR_HISTORICAL_NORM
        }

        val signStr = if (tempAnomalyC >= 0) "+${tempAnomalyC}°C" else "${tempAnomalyC}°C"
        val insight = when (classification) {
            ClimateTrendClassification.WARMER_THAN_USUAL ->
                "$monthName $currentYear in ${weather.city} is tracking $signStr warmer than the 5-year climate norm and +${Math.round((thisMonthAvgTempC - lastYearAvgTempC)*10.0)/10.0}°C higher than $monthName ${currentYear - 1}."
            ClimateTrendClassification.COOLER_THAN_USUAL ->
                "$monthName $currentYear in ${weather.city} is $signStr cooler than the 5-year historical average, providing milder conditions compared to last year."
            ClimateTrendClassification.WETTER_THAN_USUAL ->
                "Rainfall in ${weather.city} this $monthName is $precipAnomalyPercent% above the historical 5-year baseline ($thisMonthPrecipMm mm vs $fiveYearAvgPrecipMm mm average)."
            ClimateTrendClassification.DRIER_THAN_USUAL ->
                "$monthName $currentYear rainfall is running lower than historical norms, down ${Math.abs(precipAnomalyPercent)}% compared to the 5-year average."
            ClimateTrendClassification.NEAR_HISTORICAL_NORM ->
                "Temperatures and precipitation in ${weather.city} for $monthName $currentYear closely align with historical averages over the last 5 years."
        }

        return HistoricalClimateComparisonData(
            city = weather.city,
            monthName = monthName,
            year = currentYear,
            thisMonthAvgTempC = thisMonthAvgTempC,
            thisMonthAvgTempF = thisMonthAvgTempF,
            lastYearAvgTempC = lastYearAvgTempC,
            lastYearAvgTempF = lastYearAvgTempF,
            fiveYearAvgTempC = fiveYearAvgTempC,
            fiveYearAvgTempF = fiveYearAvgTempF,
            tempAnomalyC = tempAnomalyC,
            tempAnomalyF = tempAnomalyF,
            thisMonthPrecipMm = thisMonthPrecipMm,
            thisMonthPrecipInches = thisMonthPrecipInches,
            lastYearPrecipMm = lastYearPrecipMm,
            lastYearPrecipInches = lastYearPrecipInches,
            fiveYearAvgPrecipMm = fiveYearAvgPrecipMm,
            fiveYearAvgPrecipInches = fiveYearAvgPrecipInches,
            precipAnomalyPercent = precipAnomalyPercent,
            recordHighTempC = recordHighTempC,
            recordHighYear = recordHighYear,
            recordLowTempC = recordLowTempC,
            recordLowYear = recordLowYear,
            climateInsight = insight,
            trendClassification = classification
        )
    }
}
