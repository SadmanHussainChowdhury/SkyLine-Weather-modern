package com.example.data.repository

import com.squareup.moshi.JsonClass
import java.util.Calendar
import kotlin.math.abs

@JsonClass(generateAdapter = true)
data class MoonPhaseData(
    val phaseName: String, // e.g. "Waxing Crescent", "Full Moon"
    val illuminationPercentage: Int, // 0 to 100
    val lunarAgeDays: Double, // 0.0 to 29.53 days
    val moonrise: String,
    val moonset: String,
    val stageIndex: Int, // 0 to 7 (8 lunar phases)
    val stageEmoji: String,
    val phaseDescription: String,
    val observationTip: String,
    val nextFullMoonDays: Int,
    val nextNewMoonDays: Int
) {
    companion object {
        private const val LUNAR_CYCLE_DAYS = 29.53058770576

        fun calculateFromWeather(weather: WeatherData): MoonPhaseData {
            // Calculate approximate lunar age based on current date
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Known reference new moon: Jan 11, 2024
            val refCal = Calendar.getInstance().apply {
                set(2024, Calendar.JANUARY, 11, 11, 57)
            }
            val diffMillis = calendar.timeInMillis - refCal.timeInMillis
            val diffDays = diffMillis / (1000.0 * 60 * 60 * 24)
            val currentAge = (diffDays % LUNAR_CYCLE_DAYS + LUNAR_CYCLE_DAYS) % LUNAR_CYCLE_DAYS

            // Illumination percentage from weather if present, otherwise computed
            val calculatedIllum = ((1 - Math.cos(2 * Math.PI * currentAge / LUNAR_CYCLE_DAYS)) / 2 * 100).toInt()
            val finalIllum = if (weather.moonIllumination in 1..100) weather.moonIllumination else calculatedIllum

            val (phaseName, stageIdx, emoji) = when {
                currentAge < 1.8456 -> Triple("New Moon", 0, "🌑")
                currentAge < 5.5369 -> Triple("Waxing Crescent", 1, "🌒")
                currentAge < 9.2283 -> Triple("First Quarter", 2, "🌓")
                currentAge < 12.9196 -> Triple("Waxing Gibbous", 3, "🌔")
                currentAge < 16.6110 -> Triple("Full Moon", 4, "🌕")
                currentAge < 20.3023 -> Triple("Waning Gibbous", 5, "🌖")
                currentAge < 23.9937 -> Triple("Last Quarter", 6, "🌗")
                currentAge < 27.6850 -> Triple("Waning Crescent", 7, "🌘")
                else -> Triple("New Moon", 0, "🌑")
            }

            // Days until next full moon (age ~ 14.76 days)
            val daysToFull = ((14.765 - currentAge + LUNAR_CYCLE_DAYS) % LUNAR_CYCLE_DAYS).toInt().coerceAtLeast(1)
            // Days until next new moon (age ~ 0 or 29.53 days)
            val daysToNew = ((LUNAR_CYCLE_DAYS - currentAge) % LUNAR_CYCLE_DAYS).toInt().coerceAtLeast(1)

            val desc = when (stageIdx) {
                0 -> "The moon is fully aligned between Earth and the Sun. Deep dark night skies."
                1 -> "A slender sliver of light emerges in the western sky after sunset."
                2 -> "Half of the lunar disk is lit as the moon reaches 90 degrees from the Sun."
                3 -> "More than half illuminated and growing brighter each night."
                4 -> "The entire earth-facing lunar hemisphere is illuminated in full glory."
                5 -> "Slightly receding illumination following the peak of the full moon."
                6 -> "Half illuminated on the opposite side as the moon approaches its close."
                7 -> "A delicate waning crescent visible in the early morning twilight."
                else -> "The lunar cycle represents the moon's continuous journey around Earth."
            }

            val tip = when {
                finalIllum < 15 -> "🌟 Ideal night for deep-space stargazing and milky way astrophotography."
                finalIllum < 45 -> "📸 Great night to photograph crisp crater shadows along the lunar terminator."
                finalIllum < 80 -> "🌓 Moderate ambient night light. Craters and maria visible with binoculars."
                else -> "🌕 High night sky brightness. Perfect for direct telescopic lunar viewing."
            }

            return MoonPhaseData(
                phaseName = if (weather.moonPhase.isNotBlank() && weather.moonPhase != "New Moon") weather.moonPhase else phaseName,
                illuminationPercentage = finalIllum,
                lunarAgeDays = Math.round(currentAge * 10) / 10.0,
                moonrise = weather.moonrise,
                moonset = weather.moonset,
                stageIndex = stageIdx,
                stageEmoji = emoji,
                phaseDescription = desc,
                observationTip = tip,
                nextFullMoonDays = daysToFull,
                nextNewMoonDays = daysToNew
            )
        }
    }
}
