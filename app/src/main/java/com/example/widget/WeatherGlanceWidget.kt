package com.example.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.datastore.preferences.core.intPreferencesKey
import com.example.data.local.dataStore
import kotlinx.coroutines.flow.firstOrNull

class WeatherGlanceWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<Preferences> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WeatherWidgetContent()
            }
        }
    }

    @Composable
    private fun WeatherWidgetContent() {
        val prefs = currentState<Preferences>()
        val cityName = prefs[CITY_NAME_KEY] ?: "San Francisco"
        val tempC = prefs[TEMP_C_KEY] ?: 21.5
        val tempF = prefs[TEMP_F_KEY] ?: 70.7
        val conditionText = prefs[CONDITION_KEY] ?: "Partly Cloudy"
        val isCelsius = prefs[IS_CELSIUS_KEY] ?: true
        val highC = prefs[HIGH_C_KEY] ?: 24.0
        val lowC = prefs[LOW_C_KEY] ?: 14.0

        val showUv = prefs[SHOW_UV_KEY] ?: true
        val showAqi = prefs[SHOW_AQI_KEY] ?: true
        val showHumidity = prefs[SHOW_HUMIDITY_KEY] ?: true
        val showWind = prefs[SHOW_WIND_KEY] ?: true
        val showFeelsLike = prefs[SHOW_FEELS_LIKE_KEY] ?: true

        val uvVal = prefs[UV_VAL_KEY] ?: 4.5
        val aqiVal = prefs[AQI_VAL_KEY] ?: 38
        val humidityVal = prefs[HUMIDITY_VAL_KEY] ?: 58
        val windKph = prefs[WIND_KPH_KEY] ?: 14.0
        val feelsLikeC = prefs[FEELS_LIKE_C_KEY] ?: 21.0
        val feelsLikeF = prefs[FEELS_LIKE_F_KEY] ?: 69.8

        val displayTemp = if (isCelsius) "${tempC.toInt()}°C" else "${tempF.toInt()}°F"
        val highLowText = if (isCelsius) {
            "H: ${highC.toInt()}° L: ${lowC.toInt()}°"
        } else {
            val highF = (highC * 9 / 5) + 32
            val lowF = (lowC * 9 / 5) + 32
            "H: ${highF.toInt()}° L: ${lowF.toInt()}°"
        }

        val iconRes = when {
            conditionText.contains("Rain", true) || conditionText.contains("Shower", true) || conditionText.contains("Drizzle", true) -> R.drawable.ic_widget_rainy
            conditionText.contains("Snow", true) || conditionText.contains("Ice", true) || conditionText.contains("Flurry", true) -> R.drawable.ic_widget_snowy
            conditionText.contains("Cloud", true) || conditionText.contains("Overcast", true) || conditionText.contains("Mist", true) -> R.drawable.ic_widget_cloudy
            else -> R.drawable.ic_widget_sunny
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(day = androidx.compose.ui.graphics.Color(0xFF0F172A), night = androidx.compose.ui.graphics.Color(0xFF0F172A)))
                .cornerRadius(20.dp)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                // Location & Weather Condition Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = cityName,
                            style = TextStyle(
                                color = ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color.White),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = conditionText,
                            style = TextStyle(
                                color = ColorProvider(day = androidx.compose.ui.graphics.Color(0xFF94A3B8), night = androidx.compose.ui.graphics.Color(0xFF94A3B8)),
                                fontSize = 11.sp
                            )
                        )
                    }

                    Image(
                        provider = ImageProvider(iconRes),
                        contentDescription = conditionText,
                        modifier = GlanceModifier.size(36.dp)
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Current Temperature & High/Low Range
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = displayTemp,
                        style = TextStyle(
                            color = ColorProvider(day = androidx.compose.ui.graphics.Color(0xFF0288D1), night = androidx.compose.ui.graphics.Color(0xFF29B6F6)),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    Text(
                        text = highLowText,
                        style = TextStyle(
                            color = ColorProvider(day = androidx.compose.ui.graphics.Color(0xFFCBD5E1), night = androidx.compose.ui.graphics.Color(0xFFCBD5E1)),
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Configurable Weather Metrics Strip
                val activeMetrics = mutableListOf<String>()
                if (showUv) {
                    val uvLabel = when {
                        uvVal >= 11 -> "Extreme"
                        uvVal >= 8 -> "Very High"
                        uvVal >= 6 -> "High"
                        uvVal >= 3 -> "Moderate"
                        else -> "Low"
                    }
                    activeMetrics.add("UV ${uvVal.toInt()} ($uvLabel)")
                }
                if (showAqi) {
                    val aqiDesc = when (aqiVal) {
                        1 -> "Good"
                        2 -> "Moderate"
                        3 -> "Unhealthy"
                        else -> "Good ($aqiVal)"
                    }
                    activeMetrics.add("AQI: $aqiDesc")
                }
                if (showHumidity) {
                    activeMetrics.add("Hum: ${humidityVal}%")
                }
                if (showWind) {
                    val speed = if (isCelsius) "${windKph.toInt()} km/h" else "${(windKph * 0.621371).toInt()} mph"
                    activeMetrics.add("Wind: $speed")
                }
                if (showFeelsLike) {
                    val feels = if (isCelsius) "${feelsLikeC.toInt()}°" else "${feelsLikeF.toInt()}°"
                    activeMetrics.add("Feels: $feels")
                }

                if (activeMetrics.isNotEmpty()) {
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(ColorProvider(day = androidx.compose.ui.graphics.Color(0xFF1E293B), night = androidx.compose.ui.graphics.Color(0xFF1E293B)))
                            .cornerRadius(10.dp)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayText = activeMetrics.take(3).joinToString(" • ")
                        Text(
                            text = displayText,
                            style = TextStyle(
                                color = ColorProvider(day = androidx.compose.ui.graphics.Color(0xFF38BDF8), night = androidx.compose.ui.graphics.Color(0xFF38BDF8)),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }

    companion object {
        val CITY_NAME_KEY = stringPreferencesKey("city_name")
        val TEMP_C_KEY = doublePreferencesKey("temp_c")
        val TEMP_F_KEY = doublePreferencesKey("temp_f")
        val CONDITION_KEY = stringPreferencesKey("condition")
        val IS_CELSIUS_KEY = booleanPreferencesKey("is_celsius")
        val HIGH_C_KEY = doublePreferencesKey("high_c")
        val LOW_C_KEY = doublePreferencesKey("low_c")

        val SHOW_UV_KEY = booleanPreferencesKey("show_uv")
        val SHOW_AQI_KEY = booleanPreferencesKey("show_aqi")
        val SHOW_HUMIDITY_KEY = booleanPreferencesKey("show_humidity")
        val SHOW_WIND_KEY = booleanPreferencesKey("show_wind")
        val SHOW_FEELS_LIKE_KEY = booleanPreferencesKey("show_feels_like")

        val UV_VAL_KEY = doublePreferencesKey("uv_val")
        val AQI_VAL_KEY = intPreferencesKey("aqi_val")
        val HUMIDITY_VAL_KEY = intPreferencesKey("humidity_val")
        val WIND_KPH_KEY = doublePreferencesKey("wind_kph")
        val FEELS_LIKE_C_KEY = doublePreferencesKey("feels_like_c")
        val FEELS_LIKE_F_KEY = doublePreferencesKey("feels_like_f")

        suspend fun updateAllWidgets(
            context: Context,
            cityName: String,
            tempC: Double,
            tempF: Double,
            condition: String,
            isCelsius: Boolean,
            highC: Double,
            lowC: Double,
            uv: Double = 4.5,
            aqi: Int = 1,
            humidity: Int = 58,
            windKph: Double = 14.0,
            feelsLikeC: Double = tempC,
            feelsLikeF: Double = tempF
        ) {
            withContext(Dispatchers.IO) {
                try {
                    val settingsDataStore = com.example.data.local.SettingsDataStore(context)
                    val showUv = settingsDataStore.widgetShowUvFlow.firstOrNull() ?: true
                    val showAqi = settingsDataStore.widgetShowAqiFlow.firstOrNull() ?: true
                    val showHumidity = settingsDataStore.widgetShowHumidityFlow.firstOrNull() ?: true
                    val showWind = settingsDataStore.widgetShowWindFlow.firstOrNull() ?: true
                    val showFeelsLike = settingsDataStore.widgetShowFeelsLikeFlow.firstOrNull() ?: true

                    val manager = GlanceAppWidgetManager(context)
                    val glanceIds = manager.getGlanceIds(WeatherGlanceWidget::class.java)
                    for (glanceId in glanceIds) {
                        updateAppWidgetState(context, glanceId) { prefs ->
                            prefs[CITY_NAME_KEY] = cityName
                            prefs[TEMP_C_KEY] = tempC
                            prefs[TEMP_F_KEY] = tempF
                            prefs[CONDITION_KEY] = condition
                            prefs[IS_CELSIUS_KEY] = isCelsius
                            prefs[HIGH_C_KEY] = highC
                            prefs[LOW_C_KEY] = lowC

                            prefs[SHOW_UV_KEY] = showUv
                            prefs[SHOW_AQI_KEY] = showAqi
                            prefs[SHOW_HUMIDITY_KEY] = showHumidity
                            prefs[SHOW_WIND_KEY] = showWind
                            prefs[SHOW_FEELS_LIKE_KEY] = showFeelsLike

                            prefs[UV_VAL_KEY] = uv
                            prefs[AQI_VAL_KEY] = aqi
                            prefs[HUMIDITY_VAL_KEY] = humidity
                            prefs[WIND_KPH_KEY] = windKph
                            prefs[FEELS_LIKE_C_KEY] = feelsLikeC
                            prefs[FEELS_LIKE_F_KEY] = feelsLikeF
                        }
                        WeatherGlanceWidget().update(context, glanceId)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("WeatherGlanceWidget", "Error updating glance widget state: ${e.message}", e)
                }
            }
        }
    }
}

class WeatherGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherGlanceWidget()
}
