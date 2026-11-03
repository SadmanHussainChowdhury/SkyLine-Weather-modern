package com.example.data.repository

import com.squareup.moshi.JsonClass
import kotlin.math.ln
import kotlin.math.roundToInt

enum class HumidityComfortLevel(val label: String, val colorHex: Long) {
    VERY_DRY("Very Dry (<30%)", 0xFFEAB308),       // Amber
    IDEAL("Comfortable (30-50%)", 0xFF10B981),     // Emerald Green
    HUMID("Humid (50-70%)", 0xFF0EA5E9),           // Sky Blue
    VERY_HUMID("Muggy (>70%)", 0xFF8B5CF6)         // Purple
}

enum class MoldRiskLevel(val label: String, val colorHex: Long) {
    LOW("Low Mold Risk", 0xFF10B981),
    MODERATE("Moderate Risk", 0xFFF59E0B),
    HIGH("High Risk", 0xFFEF4444)
}

@JsonClass(generateAdapter = true)
data class HumidityData(
    val humidityPercentage: Int,
    val tempC: Double,
    val tempF: Double,
    val dewPointC: Double,
    val dewPointF: Double,
    val comfortLevel: HumidityComfortLevel,
    val moldRisk: MoldRiskLevel,
    val statusSummary: String,
    val indoorAdvice: String
) {
    companion object {
        fun calculate(humidity: Int, tempC: Double, tempF: Double): HumidityData {
            val clampedHumidity = humidity.coerceIn(0, 100)

            // Dew Point Calculation using Magnus-Tetens Formula
            val a = 17.27
            val b = 237.7
            val alpha = ((a * tempC) / (b + tempC)) + ln(clampedHumidity.toDouble().coerceAtLeast(1.0) / 100.0)
            val computedDewPointC = (b * alpha) / (a - alpha)
            val computedDewPointF = (computedDewPointC * 9 / 5) + 32

            val comfortLevel = when {
                clampedHumidity < 30 -> HumidityComfortLevel.VERY_DRY
                clampedHumidity <= 50 -> HumidityComfortLevel.IDEAL
                clampedHumidity <= 70 -> HumidityComfortLevel.HUMID
                else -> HumidityComfortLevel.VERY_HUMID
            }

            val moldRisk = when {
                clampedHumidity >= 70 -> MoldRiskLevel.HIGH
                clampedHumidity >= 55 -> MoldRiskLevel.MODERATE
                else -> MoldRiskLevel.LOW
            }

            val statusSummary = when (comfortLevel) {
                HumidityComfortLevel.VERY_DRY -> "Air is dry. Dry skin and respiratory irritation may occur."
                HumidityComfortLevel.IDEAL -> "Optimal humidity balance. Perfect for indoor and outdoor activities."
                HumidityComfortLevel.HUMID -> "Noticeable moisture in the air. Warm conditions may feel stickier."
                HumidityComfortLevel.VERY_HUMID -> "Muggy atmosphere. Sweat evaporation is slowed, increasing perceived heat."
            }

            val indoorAdvice = when (comfortLevel) {
                HumidityComfortLevel.VERY_DRY -> "Run a cool-mist humidifier indoors and stay hydrated."
                HumidityComfortLevel.IDEAL -> "Maintain current air ventilation. No adjustments needed."
                HumidityComfortLevel.HUMID -> "Consider using an air conditioner or dehumidifier in closed rooms."
                HumidityComfortLevel.VERY_HUMID -> "Run dehumidifiers to prevent indoor condensation and mold spores."
            }

            return HumidityData(
                humidityPercentage = clampedHumidity,
                tempC = tempC,
                tempF = tempF,
                dewPointC = computedDewPointC,
                dewPointF = computedDewPointF,
                comfortLevel = comfortLevel,
                moldRisk = moldRisk,
                statusSummary = statusSummary,
                indoorAdvice = indoorAdvice
            )
        }
    }
}
