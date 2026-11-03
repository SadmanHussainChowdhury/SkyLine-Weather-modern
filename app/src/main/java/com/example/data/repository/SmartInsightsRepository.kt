package com.example.data.repository

import com.example.BuildConfig
import com.example.data.remote.GeminiApiService
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmartInsightsRepository(
    private val geminiApiService: GeminiApiService
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(SmartInsightData::class.java)

    suspend fun fetchSmartInsights(
        weather: WeatherData,
        isCelsius: Boolean
    ): SmartInsightData = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "null") {
            return@withContext SmartInsightData.generateFallback(weather, isCelsius)
        }

        val tempStr = if (isCelsius) "${Math.round(weather.tempC)}°C" else "${Math.round(weather.tempF)}°F"
        val feelsLikeStr = if (isCelsius) "${Math.round(weather.feelsLikeC)}°C" else "${Math.round(weather.feelsLikeF)}°F"

        val prompt = """
            You are an expert meteorological personal assistant and outfit stylist.
            Weather context for ${weather.city}:
            - Temperature: $tempStr (Feels like $feelsLikeStr)
            - Weather Condition: ${weather.conditionText}
            - Humidity: ${weather.humidity}%
            - Wind Speed: ${weather.windKph} km/h
            - UV Index: ${weather.uv}
            
            Task: Provide personalized daily weather advice for the user (e.g. 'Dress in layers today').
            
            Return your response ONLY as a JSON object with these exact keys:
            {
              "mainHeadline": "Short action advice like 'Dress in layers today' or 'Carry an umbrella & jacket'",
              "outfitAdvice": "Specific guidance on clothes, layers, footwear or accessories to wear today.",
              "activityTip": "Recommendation for outdoor activity timing, commuting, or exercise.",
              "sunAndRainAdvice": "Note on UV exposure, rain precautions, or wind chill.",
              "keyTakeaways": ["Short tip 1", "Short tip 2", "Short tip 3"]
            }
            Do NOT include markdown formatting or triple backticks. Return raw JSON string only.
        """.trimIndent()

        try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                )
            )

            val response = geminiApiService.generateContent(apiKey = apiKey, request = request)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            val cleanJson = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val parsed = adapter.fromJson(cleanJson)
            if (parsed != null && parsed.mainHeadline.isNotBlank()) {
                parsed.copy(isAiGenerated = true)
            } else {
                SmartInsightData.generateFallback(weather, isCelsius)
            }
        } catch (e: Exception) {
            SmartInsightData.generateFallback(weather, isCelsius)
        }
    }
}
