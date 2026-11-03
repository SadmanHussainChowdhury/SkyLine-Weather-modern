package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val location: LocationModel,
    val current: CurrentModel,
    val forecast: ForecastModel,
    val alerts: AlertContainerModel? = null
)

@JsonClass(generateAdapter = true)
data class LocationModel(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val localtime: String
)

@JsonClass(generateAdapter = true)
data class CurrentModel(
    @Json(name = "temp_c") val tempC: Double,
    @Json(name = "temp_f") val tempF: Double,
    @Json(name = "is_day") val isDay: Int,
    val condition: ConditionModel,
    @Json(name = "wind_mph") val windMph: Double,
    @Json(name = "wind_kph") val windKph: Double,
    @Json(name = "wind_degree") val windDegree: Int? = null,
    @Json(name = "wind_dir") val windDir: String? = null,
    @Json(name = "pressure_mb") val pressureMb: Double,
    val humidity: Int,
    @Json(name = "feelslike_c") val feelsLikeC: Double,
    @Json(name = "feelslike_f") val feelsLikeF: Double,
    val uv: Double,
    @Json(name = "air_quality") val airQuality: AirQualityModel? = null
)

@JsonClass(generateAdapter = true)
data class ConditionModel(
    val text: String,
    val icon: String,
    val code: Int
)

@JsonClass(generateAdapter = true)
data class AirQualityModel(
    @Json(name = "us-epa-index") val epaIndex: Int? = null
)

@JsonClass(generateAdapter = true)
data class ForecastModel(
    @Json(name = "forecastday") val forecastDay: List<ForecastDayModel>
)

@JsonClass(generateAdapter = true)
data class ForecastDayModel(
    val date: String,
    val day: DayModel,
    val astro: AstroModel,
    val hour: List<HourModel>
)

@JsonClass(generateAdapter = true)
data class DayModel(
    @Json(name = "maxtemp_c") val maxTempC: Double,
    @Json(name = "maxtemp_f") val maxTempF: Double,
    @Json(name = "mintemp_c") val minTempC: Double,
    @Json(name = "mintemp_f") val minTempF: Double,
    val condition: ConditionModel,
    val uv: Double
)

@JsonClass(generateAdapter = true)
data class AstroModel(
    val sunrise: String,
    val sunset: String,
    @Json(name = "moonrise") val moonrise: String? = null,
    @Json(name = "moonset") val moonset: String? = null,
    @Json(name = "moon_phase") val moonPhase: String? = null,
    @Json(name = "moon_illumination") val moonIllumination: String? = null
)

@JsonClass(generateAdapter = true)
data class HourModel(
    @Json(name = "time_epoch") val timeEpoch: Long,
    val time: String,
    @Json(name = "temp_c") val tempC: Double,
    @Json(name = "temp_f") val tempF: Double,
    val condition: ConditionModel,
    @Json(name = "is_day") val isDay: Int
)

@JsonClass(generateAdapter = true)
data class AlertContainerModel(
    val alert: List<AlertModel>? = null
)

@JsonClass(generateAdapter = true)
data class AlertModel(
    val headline: String?,
    val desc: String?,
    val category: String? = null,
    val event: String? = null
)
