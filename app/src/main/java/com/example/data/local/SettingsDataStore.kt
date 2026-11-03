package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_settings")

data class HomeCardVisibility(
    val showAudioSummary: Boolean = true,
    val showSmartInsights: Boolean = true,
    val showHourlyForecast: Boolean = true,
    val showPrecipitation: Boolean = true,
    val showTemperatureTrend: Boolean = true,
    val showDailyForecast: Boolean = true,
    val showWindDashboard: Boolean = true,
    val showWindChill: Boolean = true,
    val showThermalComfort: Boolean = true,
    val showHumidityGauge: Boolean = true,
    val showAirQuality: Boolean = true,
    val showPollen: Boolean = true,
    val showUvIndex: Boolean = true,
    val showMoonPhase: Boolean = true,
    val showClimateTrends: Boolean = true,
    val showClimateComparison: Boolean = true,
    val showNarrativeTimeline: Boolean = true,
    val showOfflineMap: Boolean = true,
    val showWidgetPreview: Boolean = true,
    val showGpsSun: Boolean = true,
    val showSunCycle: Boolean = true,
    val showStatsGrid: Boolean = true
)

class SettingsDataStore(private val context: Context) {

    companion object {
        val KEY_USE_CELSIUS = booleanPreferencesKey("use_celsius")
        val KEY_LAST_CITY = stringPreferencesKey("last_city")
        val KEY_CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_AESTHETIC_THEME = stringPreferencesKey("aesthetic_theme")
        val KEY_PINNED_WIDGET_CITY = stringPreferencesKey("pinned_widget_city")
        val KEY_WIDGET_SHOW_UV = booleanPreferencesKey("widget_show_uv")
        val KEY_WIDGET_SHOW_AQI = booleanPreferencesKey("widget_show_aqi")
        val KEY_WIDGET_SHOW_HUMIDITY = booleanPreferencesKey("widget_show_humidity")
        val KEY_WIDGET_SHOW_WIND = booleanPreferencesKey("widget_show_wind")
        val KEY_WIDGET_SHOW_FEELS_LIKE = booleanPreferencesKey("widget_show_feels_like")

        // Real-Time Auto Sync Preferences
        val KEY_REALTIME_SYNC_ENABLED = booleanPreferencesKey("realtime_sync_enabled")
        val KEY_REALTIME_SYNC_INTERVAL_SEC = intPreferencesKey("realtime_sync_interval_sec")

        // Home Screen Card Visibility Preferences
        val KEY_CARD_AUDIO_SUMMARY = booleanPreferencesKey("card_audio_summary")
        val KEY_CARD_SMART_INSIGHTS = booleanPreferencesKey("card_smart_insights")
        val KEY_CARD_HOURLY_FORECAST = booleanPreferencesKey("card_hourly_forecast")
        val KEY_CARD_PRECIPITATION = booleanPreferencesKey("card_precipitation")
        val KEY_CARD_TEMP_TREND = booleanPreferencesKey("card_temp_trend")
        val KEY_CARD_DAILY_FORECAST = booleanPreferencesKey("card_daily_forecast")
        val KEY_CARD_WIND_DASHBOARD = booleanPreferencesKey("card_wind_dashboard")
        val KEY_CARD_WIND_CHILL = booleanPreferencesKey("card_wind_chill")
        val KEY_CARD_THERMAL_COMFORT = booleanPreferencesKey("card_thermal_comfort")
        val KEY_CARD_HUMIDITY_GAUGE = booleanPreferencesKey("card_humidity_gauge")
        val KEY_CARD_AIR_QUALITY = booleanPreferencesKey("card_air_quality")
        val KEY_CARD_POLLEN = booleanPreferencesKey("card_pollen")
        val KEY_CARD_UV_INDEX = booleanPreferencesKey("card_uv_index")
        val KEY_CARD_MOON_PHASE = booleanPreferencesKey("card_moon_phase")
        val KEY_CARD_CLIMATE_TRENDS = booleanPreferencesKey("card_climate_trends")
        val KEY_CARD_CLIMATE_COMPARISON = booleanPreferencesKey("card_climate_comparison")
        val KEY_CARD_NARRATIVE_TIMELINE = booleanPreferencesKey("card_narrative_timeline")
        val KEY_CARD_OFFLINE_MAP = booleanPreferencesKey("card_offline_map")
        val KEY_CARD_WIDGET_PREVIEW = booleanPreferencesKey("card_widget_preview")
        val KEY_CARD_GPS_SUN = booleanPreferencesKey("card_gps_sun")
        val KEY_CARD_SUN_CYCLE = booleanPreferencesKey("card_sun_cycle")
        val KEY_CARD_STATS_GRID = booleanPreferencesKey("card_stats_grid")
    }

    val homeCardVisibilityFlow: Flow<HomeCardVisibility> = context.dataStore.data
        .map { prefs ->
            HomeCardVisibility(
                showAudioSummary = prefs[KEY_CARD_AUDIO_SUMMARY] ?: true,
                showSmartInsights = prefs[KEY_CARD_SMART_INSIGHTS] ?: true,
                showHourlyForecast = prefs[KEY_CARD_HOURLY_FORECAST] ?: true,
                showPrecipitation = prefs[KEY_CARD_PRECIPITATION] ?: true,
                showTemperatureTrend = prefs[KEY_CARD_TEMP_TREND] ?: true,
                showDailyForecast = prefs[KEY_CARD_DAILY_FORECAST] ?: true,
                showWindDashboard = prefs[KEY_CARD_WIND_DASHBOARD] ?: true,
                showWindChill = prefs[KEY_CARD_WIND_CHILL] ?: true,
                showThermalComfort = prefs[KEY_CARD_THERMAL_COMFORT] ?: true,
                showHumidityGauge = prefs[KEY_CARD_HUMIDITY_GAUGE] ?: true,
                showAirQuality = prefs[KEY_CARD_AIR_QUALITY] ?: true,
                showPollen = prefs[KEY_CARD_POLLEN] ?: true,
                showUvIndex = prefs[KEY_CARD_UV_INDEX] ?: true,
                showMoonPhase = prefs[KEY_CARD_MOON_PHASE] ?: true,
                showClimateTrends = prefs[KEY_CARD_CLIMATE_TRENDS] ?: true,
                showClimateComparison = prefs[KEY_CARD_CLIMATE_COMPARISON] ?: true,
                showNarrativeTimeline = prefs[KEY_CARD_NARRATIVE_TIMELINE] ?: true,
                showOfflineMap = prefs[KEY_CARD_OFFLINE_MAP] ?: true,
                showWidgetPreview = prefs[KEY_CARD_WIDGET_PREVIEW] ?: true,
                showGpsSun = prefs[KEY_CARD_GPS_SUN] ?: true,
                showSunCycle = prefs[KEY_CARD_SUN_CYCLE] ?: true,
                showStatsGrid = prefs[KEY_CARD_STATS_GRID] ?: true
            )
        }

    val widgetShowUvFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_WIDGET_SHOW_UV] ?: true }

    val widgetShowAqiFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_WIDGET_SHOW_AQI] ?: true }

    val widgetShowHumidityFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_WIDGET_SHOW_HUMIDITY] ?: true }

    val widgetShowWindFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_WIDGET_SHOW_WIND] ?: true }

    val widgetShowFeelsLikeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_WIDGET_SHOW_FEELS_LIKE] ?: true }

    val pinnedWidgetCityFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_PINNED_WIDGET_CITY] ?: preferences[KEY_LAST_CITY] ?: "New York"
        }

    val useCelsiusFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_USE_CELSIUS] ?: true // Celsius is the default
        }

    val lastCityFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_LAST_CITY] ?: "New York" // Default fallback city
        }

    val customApiKeyFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_CUSTOM_API_KEY] ?: ""
        }

    val themeModeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_THEME_MODE] ?: "system" // "system" (auto switch based on system), "light", "dark"
        }

    val aestheticThemeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_AESTHETIC_THEME] ?: "midnight_blue"
        }

    val realtimeSyncEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_REALTIME_SYNC_ENABLED] ?: true
        }

    val realtimeSyncIntervalSecFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_REALTIME_SYNC_INTERVAL_SEC] ?: 60 // default 60 seconds (1 min) for live updates
        }

    suspend fun setRealtimeSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REALTIME_SYNC_ENABLED] = enabled
        }
    }

    suspend fun setRealtimeSyncIntervalSec(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REALTIME_SYNC_INTERVAL_SEC] = seconds
        }
    }

    suspend fun setAestheticTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AESTHETIC_THEME] = theme
        }
    }

    suspend fun setThemeMode(themeMode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = themeMode
        }
    }

    suspend fun setUseCelsius(useCelsius: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USE_CELSIUS] = useCelsius
        }
    }

    suspend fun setLastCity(city: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_CITY] = city
        }
    }

    suspend fun setPinnedWidgetCity(city: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PINNED_WIDGET_CITY] = city
        }
    }

    suspend fun setCustomApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CUSTOM_API_KEY] = apiKey
        }
    }

    suspend fun setWidgetShowMetric(metricKey: String, show: Boolean) {
        context.dataStore.edit { preferences ->
            when (metricKey) {
                "uv" -> preferences[KEY_WIDGET_SHOW_UV] = show
                "aqi" -> preferences[KEY_WIDGET_SHOW_AQI] = show
                "humidity" -> preferences[KEY_WIDGET_SHOW_HUMIDITY] = show
                "wind" -> preferences[KEY_WIDGET_SHOW_WIND] = show
                "feels_like" -> preferences[KEY_WIDGET_SHOW_FEELS_LIKE] = show
            }
        }
    }

    suspend fun setCardVisibility(cardKey: String, show: Boolean) {
        context.dataStore.edit { prefs ->
            when (cardKey) {
                "audio_summary" -> prefs[KEY_CARD_AUDIO_SUMMARY] = show
                "smart_insights" -> prefs[KEY_CARD_SMART_INSIGHTS] = show
                "hourly_forecast" -> prefs[KEY_CARD_HOURLY_FORECAST] = show
                "precipitation" -> prefs[KEY_CARD_PRECIPITATION] = show
                "temp_trend" -> prefs[KEY_CARD_TEMP_TREND] = show
                "daily_forecast" -> prefs[KEY_CARD_DAILY_FORECAST] = show
                "wind_dashboard" -> prefs[KEY_CARD_WIND_DASHBOARD] = show
                "wind_chill" -> prefs[KEY_CARD_WIND_CHILL] = show
                "thermal_comfort" -> prefs[KEY_CARD_THERMAL_COMFORT] = show
                "humidity_gauge" -> prefs[KEY_CARD_HUMIDITY_GAUGE] = show
                "air_quality" -> prefs[KEY_CARD_AIR_QUALITY] = show
                "pollen" -> prefs[KEY_CARD_POLLEN] = show
                "uv_index" -> prefs[KEY_CARD_UV_INDEX] = show
                "moon_phase" -> prefs[KEY_CARD_MOON_PHASE] = show
                "climate_trends" -> prefs[KEY_CARD_CLIMATE_TRENDS] = show
                "climate_comparison" -> prefs[KEY_CARD_CLIMATE_COMPARISON] = show
                "narrative_timeline" -> prefs[KEY_CARD_NARRATIVE_TIMELINE] = show
                "offline_map" -> prefs[KEY_CARD_OFFLINE_MAP] = show
                "widget_preview" -> prefs[KEY_CARD_WIDGET_PREVIEW] = show
                "gps_sun" -> prefs[KEY_CARD_GPS_SUN] = show
                "sun_cycle" -> prefs[KEY_CARD_SUN_CYCLE] = show
                "stats_grid" -> prefs[KEY_CARD_STATS_GRID] = show
            }
        }
    }

    suspend fun setAllCardsVisibility(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CARD_AUDIO_SUMMARY] = show
            prefs[KEY_CARD_SMART_INSIGHTS] = show
            prefs[KEY_CARD_HOURLY_FORECAST] = show
            prefs[KEY_CARD_PRECIPITATION] = show
            prefs[KEY_CARD_TEMP_TREND] = show
            prefs[KEY_CARD_DAILY_FORECAST] = show
            prefs[KEY_CARD_WIND_DASHBOARD] = show
            prefs[KEY_CARD_WIND_CHILL] = show
            prefs[KEY_CARD_THERMAL_COMFORT] = show
            prefs[KEY_CARD_HUMIDITY_GAUGE] = show
            prefs[KEY_CARD_AIR_QUALITY] = show
            prefs[KEY_CARD_POLLEN] = show
            prefs[KEY_CARD_UV_INDEX] = show
            prefs[KEY_CARD_MOON_PHASE] = show
            prefs[KEY_CARD_CLIMATE_TRENDS] = show
            prefs[KEY_CARD_CLIMATE_COMPARISON] = show
            prefs[KEY_CARD_NARRATIVE_TIMELINE] = show
            prefs[KEY_CARD_OFFLINE_MAP] = show
            prefs[KEY_CARD_WIDGET_PREVIEW] = show
            prefs[KEY_CARD_GPS_SUN] = show
            prefs[KEY_CARD_SUN_CYCLE] = show
            prefs[KEY_CARD_STATS_GRID] = show
        }
    }

    suspend fun applyPresetMinimalist() {
        context.dataStore.edit { prefs ->
            prefs[KEY_CARD_AUDIO_SUMMARY] = false
            prefs[KEY_CARD_SMART_INSIGHTS] = true
            prefs[KEY_CARD_HOURLY_FORECAST] = true
            prefs[KEY_CARD_PRECIPITATION] = true
            prefs[KEY_CARD_TEMP_TREND] = false
            prefs[KEY_CARD_DAILY_FORECAST] = true
            prefs[KEY_CARD_WIND_DASHBOARD] = false
            prefs[KEY_CARD_WIND_CHILL] = false
            prefs[KEY_CARD_THERMAL_COMFORT] = true
            prefs[KEY_CARD_HUMIDITY_GAUGE] = false
            prefs[KEY_CARD_AIR_QUALITY] = true
            prefs[KEY_CARD_POLLEN] = false
            prefs[KEY_CARD_UV_INDEX] = true
            prefs[KEY_CARD_MOON_PHASE] = false
            prefs[KEY_CARD_CLIMATE_TRENDS] = false
            prefs[KEY_CARD_CLIMATE_COMPARISON] = false
            prefs[KEY_CARD_NARRATIVE_TIMELINE] = false
            prefs[KEY_CARD_OFFLINE_MAP] = false
            prefs[KEY_CARD_WIDGET_PREVIEW] = false
            prefs[KEY_CARD_GPS_SUN] = false
            prefs[KEY_CARD_SUN_CYCLE] = false
            prefs[KEY_CARD_STATS_GRID] = true
        }
    }
}
