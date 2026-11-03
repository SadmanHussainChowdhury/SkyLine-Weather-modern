package com.example.data.repository

import com.squareup.moshi.JsonClass
import kotlin.math.pow

enum class FrostbiteRiskLevel(val label: String, val colorHex: Long) {
    NONE("No Frostbite Risk", 0xFF10B981),        // Emerald Green
    LOW("Low Chill Impact", 0xFF0EA5E9),          // Sky Blue
    MODERATE("Moderate Risk (30m)", 0xFFF59E0B),  // Amber
    HIGH("High Risk (10-30m)", 0xFFF97316),       // Orange
    SEVERE("Extreme Risk (<10m)", 0xFFEF4444)     // Red
}

@JsonClass(generateAdapter = true)
data class WindChillData(
    val actualTempC: Double,
    val actualTempF: Double,
    val windKph: Double,
    val windMph: Double,
    val windChillC: Double,
    val windChillF: Double,
    val isWindChillActive: Boolean,
    val tempDifferenceC: Double,
    val tempDifferenceF: Double,
    val riskLevel: FrostbiteRiskLevel,
    val frostbiteEstimateTime: String,
    val clothingAdvice: String,
    val windCategoryName: String
) {
    companion object {
        fun calculate(tempC: Double, tempF: Double, windKph: Double, windMph: Double): WindChillData {
            // Standard NWS / Environment Canada Wind Chill Formula:
            // Applicable when Temp <= 50°F (10°C) and Wind > 3 mph (4.8 km/h)
            val isApplicable = tempF <= 50.0 && windMph > 3.0

            val computedChillF = if (isApplicable) {
                val vPow = windMph.pow(0.16)
                35.74 + (0.6215 * tempF) - (35.75 * vPow) + (0.4275 * tempF * vPow)
            } else {
                tempF
            }

            val computedChillC = if (isApplicable) {
                val vPow = windKph.pow(0.16)
                13.12 + (0.6215 * tempC) - (11.37 * vPow) + (0.3965 * tempC * vPow)
            } else {
                tempC
            }

            val diffF = tempF - computedChillF
            val diffC = tempC - computedChillC

            val riskLevel = when {
                computedChillF <= -35.0 -> FrostbiteRiskLevel.SEVERE
                computedChillF <= -18.0 -> FrostbiteRiskLevel.HIGH
                computedChillF <= 0.0 -> FrostbiteRiskLevel.MODERATE
                computedChillF <= 32.0 -> FrostbiteRiskLevel.LOW
                else -> FrostbiteRiskLevel.NONE
            }

            val frostbiteEstimateTime = when (riskLevel) {
                FrostbiteRiskLevel.SEVERE -> "Danger! Frostbite possible in under 10 minutes on exposed skin."
                FrostbiteRiskLevel.HIGH -> "Caution! Frostbite risk within 10 to 30 minutes on exposed skin."
                FrostbiteRiskLevel.MODERATE -> "Cold weather warning. Frostbite possible within 30 minutes."
                FrostbiteRiskLevel.LOW -> "Noticeable wind cooling effect. Low frostbite threat."
                FrostbiteRiskLevel.NONE -> "Comfortable wind condition. No frostbite hazard."
            }

            val clothingAdvice = when {
                computedChillF <= -20.0 -> "Heavy thermal parka, double insulated gloves & full face cover mandatory."
                computedChillF <= 0.0 -> "Insulated winter coat, warm beanie, scarf, and windproof gloves required."
                computedChillF <= 32.0 -> "Layered jacket or heavy fleece with a windbreaker outer shell."
                computedChillF <= 50.0 -> "Light sweater or windproof jacket recommended for outdoor walks."
                else -> "Mild thermal conditions — standard daily clothing is suitable."
            }

            val windCategory = when {
                windKph < 6 -> "Calm Air"
                windKph < 20 -> "Gentle Breeze"
                windKph < 38 -> "Fresh Breeze"
                windKph < 62 -> "Strong Wind"
                else -> "Gale Force Winds"
            }

            return WindChillData(
                actualTempC = tempC,
                actualTempF = tempF,
                windKph = windKph,
                windMph = windMph,
                windChillC = computedChillC,
                windChillF = computedChillF,
                isWindChillActive = isApplicable,
                tempDifferenceC = diffC,
                tempDifferenceF = diffF,
                riskLevel = riskLevel,
                frostbiteEstimateTime = frostbiteEstimateTime,
                clothingAdvice = clothingAdvice,
                windCategoryName = windCategory
            )
        }
    }
}
