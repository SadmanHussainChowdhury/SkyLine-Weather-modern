package com.example.data.repository

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SmartInsightData(
    val mainHeadline: String,
    val outfitAdvice: String,
    val activityTip: String,
    val sunAndRainAdvice: String,
    val keyTakeaways: List<String>,
    val isAiGenerated: Boolean = true
) {
    companion object {
        fun generateFallback(weather: WeatherData, isCelsius: Boolean): SmartInsightData {
            val temp = if (isCelsius) weather.tempC else (weather.tempF - 32) * 5 / 9
            val condition = weather.conditionText.lowercase()
            val uv = weather.uv
            val humidity = weather.humidity
            val wind = weather.windKph

            val isRainy = condition.contains("rain") || condition.contains("drizzle") || condition.contains("shower") || condition.contains("thunder")
            val isSnowy = condition.contains("snow") || condition.contains("blizzard") || condition.contains("sleet")
            val isSunny = condition.contains("sun") || condition.contains("clear")

            val headline = when {
                isRainy -> "Carry an umbrella & stay dry"
                isSnowy -> "Bundle up in warm winter gear"
                temp < 10 -> "Dress in warm insulating layers"
                temp in 10.0..18.0 -> "Dress in layers today"
                temp in 18.1..26.0 -> "Mild & comfortable weather"
                else -> "Wear light breathable fabrics"
            }

            val outfit = when {
                temp < 5 -> "Heavy insulated coat, fleece inner layer, scarf, and warm gloves."
                temp < 15 -> "Opt for a mid-weight jacket or cardigan over a t-shirt for versatile layering throughout the day."
                temp < 25 -> "Light shirt, breathable pants or jeans, and comfortable walking shoes."
                else -> "Moisture-wicking t-shirt, linen shorts, and sunglasses."
            }

            val activity = when {
                isRainy -> "Great day for indoor activities, gym workouts, or visiting local cafes."
                isSnowy -> "Take care on icy roads and walkways; limit outdoor exposure."
                wind > 30 -> "Windy conditions outdoors; secure loose items and hold onto hats."
                isSunny && temp in 15.0..26.0 -> "Ideal conditions for outdoor walks, park visits, or outdoor dining."
                else -> "Pleasant conditions for outdoor commutes and light exercise."
            }

            val precautions = mutableListOf<String>()
            if (uv >= 6) precautions.add("UV index is high: apply SPF 30+ sunscreen.")
            if (isRainy) precautions.add("Rain gear & waterproof shoes recommended.")
            if (humidity > 75 && temp > 22) precautions.add("High humidity: stay hydrated.")
            if (wind > 25) precautions.add("Breezy conditions make it feel cooler.")

            if (precautions.isEmpty()) {
                precautions.add("Moderate weather conditions; standard daily wear works great.")
            }

            val sunRain = precautions.joinToString(" ")

            val takeaways = listOf(
                if (temp < 15) "Layering allows easy temperature adjustment." else "Hydration is key during warm outdoor hours.",
                if (isRainy) "Keep a compact travel umbrella in your bag." else if (uv >= 5) "Peak UV intensity occurs between 10 AM and 4 PM." else "Moderate humidity across the city.",
                "Custom weather recommendation for ${weather.city}."
            )

            return SmartInsightData(
                mainHeadline = headline,
                outfitAdvice = outfit,
                activityTip = activity,
                sunAndRainAdvice = sunRain,
                keyTakeaways = takeaways,
                isAiGenerated = false
            )
        }
    }
}
