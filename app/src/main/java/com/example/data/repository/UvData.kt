package com.example.data.repository

import com.squareup.moshi.JsonClass

enum class UvRiskLevel(val label: String, val category: String, val colorHex: Long) {
    LOW("Low Exposure", "Low", 0xFF10B981),              // Emerald Green
    MODERATE("Moderate Risk", "Moderate", 0xFFF59E0B),    // Amber / Yellow
    HIGH("High Risk", "High", 0xFFF97316),                // Orange
    VERY_HIGH("Very High Danger", "Very High", 0xFFEF4444),// Red
    EXTREME("Extreme Hazard", "Extreme", 0xFFA855F7)      // Purple
}

@JsonClass(generateAdapter = true)
data class UvData(
    val uvIndex: Double,
    val riskLevel: UvRiskLevel,
    val recommendedSpf: String,
    val maxUnprotectedTimeMinutes: String,
    val protectionAdvice: String,
    val requiredGear: List<String>,
    val peakHoursNote: String = "Peak UV intensity occurs between 10:00 AM and 4:00 PM."
) {
    companion object {
        fun calculate(uvIndex: Double): UvData {
            val clampedUv = uvIndex.coerceAtLeast(0.0)

            val riskLevel = when {
                clampedUv < 3.0 -> UvRiskLevel.LOW
                clampedUv < 6.0 -> UvRiskLevel.MODERATE
                clampedUv < 8.0 -> UvRiskLevel.HIGH
                clampedUv < 11.0 -> UvRiskLevel.VERY_HIGH
                else -> UvRiskLevel.EXTREME
            }

            val recommendedSpf = when (riskLevel) {
                UvRiskLevel.LOW -> "SPF 15+"
                UvRiskLevel.MODERATE -> "SPF 30+"
                UvRiskLevel.HIGH -> "SPF 30+ to 50+"
                UvRiskLevel.VERY_HIGH -> "SPF 50+ Broad Spectrum"
                UvRiskLevel.EXTREME -> "SPF 50+ Broad Spectrum Water Resistant"
            }

            val maxUnprotectedTimeMinutes = when (riskLevel) {
                UvRiskLevel.LOW -> "60+ minutes"
                UvRiskLevel.MODERATE -> "30 - 45 mins"
                UvRiskLevel.HIGH -> "15 - 25 mins"
                UvRiskLevel.VERY_HIGH -> "10 - 15 mins"
                UvRiskLevel.EXTREME -> "< 10 mins"
            }

            val protectionAdvice = when (riskLevel) {
                UvRiskLevel.LOW -> "Minimal sun hazard. Wear UV-blocking sunglasses on bright days."
                UvRiskLevel.MODERATE -> "Take precautions. Apply sunscreen, wear a wide-brim hat, and seek shade during midday."
                UvRiskLevel.HIGH -> "Protection essential. Cover up with long sleeves, wear sunglasses, and reapply sunscreen every 2 hours."
                UvRiskLevel.VERY_HIGH -> "Extra protection required. Avoid direct mid-day sun. Broad-spectrum SPF and protective clothing are mandatory."
                UvRiskLevel.EXTREME -> "Avoid outdoor sun exposure during peak hours. Unprotected skin and eyes can burn in minutes."
            }

            val requiredGear = when (riskLevel) {
                UvRiskLevel.LOW -> listOf("Sunglasses")
                UvRiskLevel.MODERATE -> listOf("SPF 30 Sunscreen", "Sunglasses", "Wide-Brim Hat")
                UvRiskLevel.HIGH -> listOf("SPF 30+ Sunscreen", "UV Sunglasses", "Hat & Long Sleeves", "Seek Shade")
                UvRiskLevel.VERY_HIGH -> listOf("SPF 50+ Sunscreen", "UV400 Sunglasses", "UPF Clothing", "Wide Hat")
                UvRiskLevel.EXTREME -> listOf("SPF 50+ Sunscreen", "UPF50+ Full Cover", "UV400 Sunglasses", "Avoid Peak Sun")
            }

            return UvData(
                uvIndex = clampedUv,
                riskLevel = riskLevel,
                recommendedSpf = recommendedSpf,
                maxUnprotectedTimeMinutes = maxUnprotectedTimeMinutes,
                protectionAdvice = protectionAdvice,
                requiredGear = requiredGear
            )
        }
    }
}
