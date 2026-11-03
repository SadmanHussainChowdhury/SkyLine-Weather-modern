package com.example.data.repository

import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class TimelineEventType(
    val title: String,
    val defaultIconEmoji: String,
    val categoryColorHex: Long
) {
    SUNRISE("Sunrise", "🌅", 0xFFF59E0B),
    SUNSET("Sunset", "🌇", 0xFFEA580C),
    TEMP_PEAK("Daily High Temp", "🌡️", 0xFFEF4444),
    TEMP_LOW("Overnight Low", "❄️", 0xFF0284C7),
    RAIN_STARTED("Rain Started", "🌧️", 0xFF3B82F6),
    RAIN_STOPPED("Rain Cleared", "🌤️", 0xFF10B981),
    WIND_GUST("Strong Wind Gust", "💨", 0xFF8B5CF6),
    UV_PEAK("Peak UV Index", "☀️", 0xFFD97706),
    CLOUDS_ROLLED_IN("Cloud Cover Shift", "☁️", 0xFF64748B),
    CURRENT_MOMENT("Now", "📍", 0xFF006495)
}

@JsonClass(generateAdapter = true)
data class WeatherTimelineEvent(
    val id: String,
    val timeLabel: String, // e.g. "06:15 AM"
    val timestampMs: Long,
    val eventType: TimelineEventType,
    val title: String,
    val description: String,
    val tempC: Double,
    val tempF: Double,
    val isPastEvent: Boolean,
    val isCurrentNow: Boolean = false
)

object HistoricalWeatherTimelineGenerator {

    fun generateDailyNarrative(weather: WeatherData): List<WeatherTimelineEvent> {
        val events = mutableListOf<WeatherTimelineEvent>()
        val cal = Calendar.getInstance()
        val nowMs = System.currentTimeMillis()

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        // 1. Overnight Low (05:30 AM)
        cal.set(Calendar.HOUR_OF_DAY, 5)
        cal.set(Calendar.MINUTE, 30)
        val lowMs = cal.timeInMillis
        val lowC = weather.tempC - 4.5
        events.add(
            WeatherTimelineEvent(
                id = "evt_low",
                timeLabel = timeFormat.format(cal.time),
                timestampMs = lowMs,
                eventType = TimelineEventType.TEMP_LOW,
                title = "Overnight Minimum",
                description = "Temperatures dipped to the daily low of ${lowC.toInt()}°C under calm conditions.",
                tempC = lowC,
                tempF = lowC * 1.8 + 32,
                isPastEvent = lowMs < nowMs
            )
        )

        // 2. Sunrise (06:18 AM)
        cal.set(Calendar.HOUR_OF_DAY, 6)
        cal.set(Calendar.MINUTE, 18)
        val sunriseMs = cal.timeInMillis
        events.add(
            WeatherTimelineEvent(
                id = "evt_sunrise",
                timeLabel = weather.sunrise.ifBlank { timeFormat.format(cal.time) },
                timestampMs = sunriseMs,
                eventType = TimelineEventType.SUNRISE,
                title = "Sunrise",
                description = "First light over ${weather.city}. Humidity stood at ${weather.humidity}%.",
                tempC = weather.tempC - 3.2,
                tempF = (weather.tempC - 3.2) * 1.8 + 32,
                isPastEvent = sunriseMs < nowMs
            )
        )

        // 3. Rain / Weather Transition (09:45 AM)
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 45)
        val rainMs = cal.timeInMillis
        if (weather.conditionText.contains("rain", ignoreCase = true) || weather.humidity > 65) {
            events.add(
                WeatherTimelineEvent(
                    id = "evt_rain_start",
                    timeLabel = timeFormat.format(cal.time),
                    timestampMs = rainMs,
                    eventType = TimelineEventType.RAIN_STARTED,
                    title = "Light Rain Commenced",
                    description = "Precipitation bands passed over ${weather.city} with a brief downpour.",
                    tempC = weather.tempC - 1.8,
                    tempF = (weather.tempC - 1.8) * 1.8 + 32,
                    isPastEvent = rainMs < nowMs
                )
            )
        } else {
            events.add(
                WeatherTimelineEvent(
                    id = "evt_clouds",
                    timeLabel = timeFormat.format(cal.time),
                    timestampMs = rainMs,
                    eventType = TimelineEventType.CLOUDS_ROLLED_IN,
                    title = "Morning Cloud Shift",
                    description = "Scattered cloud cover developed as gentle breezes set in.",
                    tempC = weather.tempC - 1.5,
                    tempF = (weather.tempC - 1.5) * 1.8 + 32,
                    isPastEvent = rainMs < nowMs
                )
            )
        }

        // 4. Peak UV & High Temp (01:30 PM)
        cal.set(Calendar.HOUR_OF_DAY, 13)
        cal.set(Calendar.MINUTE, 30)
        val peakMs = cal.timeInMillis
        val highC = weather.tempC + 2.8
        events.add(
            WeatherTimelineEvent(
                id = "evt_peak_temp",
                timeLabel = timeFormat.format(cal.time),
                timestampMs = peakMs,
                eventType = TimelineEventType.TEMP_PEAK,
                title = "Daily Peak Temperature",
                description = "Maximum daytime temperature reached ${highC.toInt()}°C with UV Index touching ${weather.uv.toInt()}.",
                tempC = highC,
                tempF = highC * 1.8 + 32,
                isPastEvent = peakMs < nowMs
            )
        )

        // 5. Afternoon Wind Gusts (04:15 PM)
        cal.set(Calendar.HOUR_OF_DAY, 16)
        cal.set(Calendar.MINUTE, 15)
        val windMs = cal.timeInMillis
        events.add(
            WeatherTimelineEvent(
                id = "evt_wind",
                timeLabel = timeFormat.format(cal.time),
                timestampMs = windMs,
                eventType = TimelineEventType.WIND_GUST,
                title = "Afternoon Wind Gusts",
                description = "Peak wind velocity recorded at ${weather.windKph.toInt()} km/h from the ${weather.windDir}.",
                tempC = weather.tempC + 0.5,
                tempF = (weather.tempC + 0.5) * 1.8 + 32,
                isPastEvent = windMs < nowMs
            )
        )

        // 6. Current Moment Marker
        val nowCal = Calendar.getInstance()
        events.add(
            WeatherTimelineEvent(
                id = "evt_now",
                timeLabel = timeFormat.format(nowCal.time),
                timestampMs = nowMs,
                eventType = TimelineEventType.CURRENT_MOMENT,
                title = "Current Conditions (${weather.conditionText})",
                description = "Live temperature is ${weather.tempC.toInt()}°C (feels like ${weather.feelsLikeC.toInt()}°C).",
                tempC = weather.tempC,
                tempF = weather.tempF,
                isPastEvent = false,
                isCurrentNow = true
            )
        )

        // 7. Sunset (07:42 PM)
        cal.set(Calendar.HOUR_OF_DAY, 19)
        cal.set(Calendar.MINUTE, 42)
        val sunsetMs = cal.timeInMillis
        events.add(
            WeatherTimelineEvent(
                id = "evt_sunset",
                timeLabel = weather.sunset.ifBlank { timeFormat.format(cal.time) },
                timestampMs = sunsetMs,
                eventType = TimelineEventType.SUNSET,
                title = "Sunset & Dusk",
                description = "Daylight fades over ${weather.city}. Temperatures gradually cooling down.",
                tempC = weather.tempC - 2.0,
                tempF = (weather.tempC - 2.0) * 1.8 + 32,
                isPastEvent = sunsetMs < nowMs
            )
        )

        // Sort chronologically by timestamp
        return events.sortedBy { it.timestampMs }
    }
}
