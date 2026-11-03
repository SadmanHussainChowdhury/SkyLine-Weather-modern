package com.example.data.repository

import com.squareup.moshi.JsonClass

enum class AllergyRiskLevel(val label: String, val colorHex: Long) {
    LOW("Low Risk", 0xFF22C55E),          // Green
    MODERATE("Moderate Risk", 0xFFEAB308), // Yellow
    HIGH("High Risk", 0xFFF97316),       // Orange
    VERY_HIGH("Very High", 0xFFEF4444)    // Red
}

@JsonClass(generateAdapter = true)
data class PollenItem(
    val name: String,               // "Tree Pollen", "Grass Pollen", "Weed Pollen", "Mold Spores"
    val countGrainsPerM3: Int,      // e.g. 68 grains/m³
    val maxThreshold: Int = 120,    // Threshold for risk calculation & gauge
    val riskLevel: AllergyRiskLevel,
    val dominantSpecies: String     // e.g. "Birch & Oak", "Timothy & Bermuda", "Ragweed & Sage"
)

@JsonClass(generateAdapter = true)
data class PollenData(
    val city: String,
    val overallRiskLevel: AllergyRiskLevel,
    val overallIndex: Int,          // Scale 0 to 100
    val primaryAllergen: String,    // e.g. "Birch Tree Pollen"
    val treePollen: PollenItem,
    val grassPollen: PollenItem,
    val weedPollen: PollenItem,
    val moldSpores: PollenItem,
    val healthAdvice: String,
    val forecastDate: String = "Today",
    val airQualityIndexPm25: Double = 12.4
)
