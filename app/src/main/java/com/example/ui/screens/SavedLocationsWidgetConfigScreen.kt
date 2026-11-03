package com.example.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.Resource
import com.example.data.repository.WeatherData
import com.example.ui.components.rememberAppHaptics
import com.example.ui.viewmodel.WeatherViewModel
import com.example.widget.WeatherGlanceWidgetReceiver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CityFilterTab {
    ALL, FAVORITES, SEARCH_HISTORY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedLocationsWidgetConfigScreen(
    viewModel: WeatherViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = rememberAppHaptics()

    val pinnedWidgetCity by viewModel.pinnedWidgetCity.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val isCelsius by viewModel.isCelsius.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()

    val showUv by viewModel.widgetShowUv.collectAsState()
    val showAqi by viewModel.widgetShowAqi.collectAsState()
    val showHumidity by viewModel.widgetShowHumidity.collectAsState()
    val showWind by viewModel.widgetShowWind.collectAsState()
    val showFeelsLike by viewModel.widgetShowFeelsLike.collectAsState()

    var selectedCity by remember(pinnedWidgetCity) { mutableStateOf(pinnedWidgetCity) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf(CityFilterTab.ALL) }

    // State for Preview Card Weather Data
    var previewWeather by remember { mutableStateOf<WeatherData?>(null) }
    var isLoadingPreview by remember { mutableStateOf(false) }

    // Fetch preview weather whenever selectedCity changes
    LaunchedEffect(selectedCity, customApiKey) {
        if (selectedCity.isNotBlank()) {
            isLoadingPreview = true
            viewModel.repository.getWeather(selectedCity, forceRefresh = false, userApiKey = customApiKey).collect { res ->
                if (res is Resource.Success) {
                    previewWeather = res.data
                    isLoadingPreview = false
                } else if (res is Resource.Error) {
                    isLoadingPreview = false
                }
            }
        }
    }

    // Combine recent searches and favorites into a unified list
    val favoriteNames = remember(favorites) { favorites.map { it.cityName } }
    val searchNames = remember(recentSearches) { recentSearches.map { it.cityName } }

    val combinedCities = remember(favorites, recentSearches, selectedFilterTab, searchQuery) {
        val list = mutableListOf<WidgetCityItem>()
        val seen = mutableSetOf<String>()

        if (selectedFilterTab == CityFilterTab.ALL || selectedFilterTab == CityFilterTab.FAVORITES) {
            favorites.forEach { fav ->
                if (seen.add(fav.cityName.lowercase())) {
                    list.add(
                        WidgetCityItem(
                            cityName = fav.cityName,
                            isFavorite = true,
                            subtext = "Saved Favorite • Lat ${"%.2f".format(fav.lat)}, Lon ${"%.2f".format(fav.lon)}"
                        )
                    )
                }
            }
        }

        if (selectedFilterTab == CityFilterTab.ALL || selectedFilterTab == CityFilterTab.SEARCH_HISTORY) {
            val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            recentSearches.forEach { rec ->
                if (seen.add(rec.cityName.lowercase())) {
                    val isFav = favoriteNames.any { it.equals(rec.cityName, ignoreCase = true) }
                    list.add(
                        WidgetCityItem(
                            cityName = rec.cityName,
                            isFavorite = isFav,
                            subtext = "Searched ${sdf.format(Date(rec.searchedAt))}"
                        )
                    )
                } else {
                    // Update existing item subtext to show search timestamp if present
                }
            }
        }

        if (searchQuery.isNotBlank()) {
            list.filter { it.cityName.contains(searchQuery.trim(), ignoreCase = true) }
        } else {
            list
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("saved_locations_widget_config_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = {
                        haptics.performKeyTap()
                        onBack()
                    },
                    modifier = Modifier.testTag("widget_config_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Navigate Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Icon(
                imageVector = Icons.Outlined.Widgets,
                contentDescription = "Widget Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Saved Locations Widget",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Pin cities from search history to Android Home Screen",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 2. Widget Preview & Active Configuration Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("widget_live_preview_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Matching Slate Dark Glance Widget theme
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.PushPin,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "HOME SCREEN WIDGET PREVIEW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8),
                                    letterSpacing = 1.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedCity.equals(pinnedWidgetCity, ignoreCase = true)) Color(0xFF059669) else Color(0xFF334155)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (selectedCity.equals(pinnedWidgetCity, ignoreCase = true)) Icons.Default.Check else Icons.Default.Pin,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (selectedCity.equals(pinnedWidgetCity, ignoreCase = true)) "PINNED ACTIVE" else "SELECTED PREVIEW",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Widget Content Body Preview
                        val weather = previewWeather
                        val tempDisplay = if (weather != null) {
                            if (isCelsius) "${weather.tempC.toInt()}°C" else "${weather.tempF.toInt()}°F"
                        } else {
                            "70°F"
                        }
                        val condDisplay = weather?.conditionText ?: "Partly Cloudy"
                        val highLowDisplay = if (weather != null) {
                            val maxC = weather.dailyForecast.firstOrNull()?.maxTempC ?: weather.tempC
                            val minC = weather.dailyForecast.firstOrNull()?.minTempC ?: weather.tempC
                            val maxF = weather.dailyForecast.firstOrNull()?.maxTempF ?: weather.tempF
                            val minF = weather.dailyForecast.firstOrNull()?.minTempF ?: weather.tempF
                            if (isCelsius) "H: ${maxC.toInt()}°  L: ${minC.toInt()}°"
                            else "H: ${maxF.toInt()}°  L: ${minF.toInt()}°"
                        } else {
                            "H: 75°  L: 58°"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedCity,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = condDisplay,
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Icon(
                                imageVector = Icons.Outlined.Cloud,
                                contentDescription = condDisplay,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = tempDisplay,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = highLowDisplay,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        // Visible Metrics Badges Preview Strip
                        val previewMetrics = mutableListOf<String>()
                        val uvVal = weather?.uv ?: 4.5
                        val aqiVal = weather?.aqi ?: 1
                        val humVal = weather?.humidity ?: 58
                        val windSpeed = weather?.windKph ?: 14.0
                        val feelsC = weather?.feelsLikeC ?: (weather?.tempC ?: 21.0)
                        val feelsF = weather?.feelsLikeF ?: (weather?.tempF ?: 70.0)

                        if (showUv) {
                            val uvLabel = when {
                                uvVal >= 11 -> "Extreme"
                                uvVal >= 8 -> "Very High"
                                uvVal >= 6 -> "High"
                                uvVal >= 3 -> "Moderate"
                                else -> "Low"
                            }
                            previewMetrics.add("UV ${uvVal.toInt()} ($uvLabel)")
                        }
                        if (showAqi) {
                            val aqiDesc = when (aqiVal) {
                                1 -> "Good"
                                2 -> "Moderate"
                                3 -> "Unhealthy"
                                else -> "Good ($aqiVal)"
                            }
                            previewMetrics.add("AQI: $aqiDesc")
                        }
                        if (showHumidity) {
                            previewMetrics.add("Hum: ${humVal}%")
                        }
                        if (showWind) {
                            val spd = if (isCelsius) "${windSpeed.toInt()} km/h" else "${(windSpeed * 0.621371).toInt()} mph"
                            previewMetrics.add("Wind: $spd")
                        }
                        if (showFeelsLike) {
                            val fl = if (isCelsius) "${feelsC.toInt()}°" else "${feelsF.toInt()}°"
                            previewMetrics.add("Feels: $fl")
                        }

                        if (previewMetrics.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = previewMetrics.take(3).joinToString(" • "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFF334155))

                        // Action Buttons: Pin to Home Screen & Sync Active Widget
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    haptics.performSuccess()
                                    viewModel.setPinnedWidgetCity(context, selectedCity)

                                    // Request pinning widget on Android 8.0+ (API 26)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        val appWidgetManager = AppWidgetManager.getInstance(context)
                                        val provider = ComponentName(context, WeatherGlanceWidgetReceiver::class.java)
                                        if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                            appWidgetManager.requestPinAppWidget(provider, null, null)
                                            Toast.makeText(context, "Requesting Android Home Screen Widget Pin for $selectedCity...", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Widget pinned to $selectedCity! Add Weather widget from Home screen.", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Widget updated to $selectedCity!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0288D1),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("pin_to_home_screen_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pin,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Pin $selectedCity to Home Screen",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (!selectedCity.equals(pinnedWidgetCity, ignoreCase = true)) {
                                OutlinedButton(
                                    onClick = {
                                        haptics.performSuccess()
                                        viewModel.setPinnedWidgetCity(context, selectedCity)
                                        Toast.makeText(context, "Set active widget location to $selectedCity", Toast.LENGTH_SHORT).show()
                                    },
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .testTag("sync_active_widget_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Set as Default Widget Location",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2.5 Configurable Home Screen Weather Metrics
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("widget_metrics_config_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Widgets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Visible Weather Metrics On Home Screen",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Select which weather data points appear on your home screen widget:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Quick Metric Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    haptics.performSuccess()
                                    viewModel.toggleWidgetMetric(context, "uv", true)
                                    viewModel.toggleWidgetMetric(context, "aqi", true)
                                    viewModel.toggleWidgetMetric(context, "humidity", true)
                                    viewModel.toggleWidgetMetric(context, "wind", true)
                                    viewModel.toggleWidgetMetric(context, "feels_like", true)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("preset_all_metrics")
                            ) {
                                Text("All Metrics", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    haptics.performSuccess()
                                    viewModel.toggleWidgetMetric(context, "uv", true)
                                    viewModel.toggleWidgetMetric(context, "aqi", true)
                                    viewModel.toggleWidgetMetric(context, "humidity", false)
                                    viewModel.toggleWidgetMetric(context, "wind", false)
                                    viewModel.toggleWidgetMetric(context, "feels_like", false)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("preset_uv_aqi")
                            ) {
                                Text("UV & AQI", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    haptics.performSuccess()
                                    viewModel.toggleWidgetMetric(context, "uv", false)
                                    viewModel.toggleWidgetMetric(context, "aqi", false)
                                    viewModel.toggleWidgetMetric(context, "humidity", false)
                                    viewModel.toggleWidgetMetric(context, "wind", false)
                                    viewModel.toggleWidgetMetric(context, "feels_like", false)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .testTag("preset_minimal")
                            ) {
                                Text("Minimalist", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Metric Toggles Matrix
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = showUv,
                                    onClick = {
                                        haptics.performToggleMode()
                                        viewModel.toggleWidgetMetric(context, "uv", !showUv)
                                    },
                                    label = { Text("UV Index", fontSize = 12.sp) },
                                    leadingIcon = if (showUv) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("metric_chip_uv")
                                )

                                FilterChip(
                                    selected = showAqi,
                                    onClick = {
                                        haptics.performToggleMode()
                                        viewModel.toggleWidgetMetric(context, "aqi", !showAqi)
                                    },
                                    label = { Text("Air Quality (AQI)", fontSize = 12.sp) },
                                    leadingIcon = if (showAqi) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("metric_chip_aqi")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = showHumidity,
                                    onClick = {
                                        haptics.performToggleMode()
                                        viewModel.toggleWidgetMetric(context, "humidity", !showHumidity)
                                    },
                                    label = { Text("Humidity (%)", fontSize = 12.sp) },
                                    leadingIcon = if (showHumidity) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("metric_chip_humidity")
                                )

                                FilterChip(
                                    selected = showWind,
                                    onClick = {
                                        haptics.performToggleMode()
                                        viewModel.toggleWidgetMetric(context, "wind", !showWind)
                                    },
                                    label = { Text("Wind Speed", fontSize = 12.sp) },
                                    leadingIcon = if (showWind) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("metric_chip_wind")
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth()) {
                                FilterChip(
                                    selected = showFeelsLike,
                                    onClick = {
                                        haptics.performToggleMode()
                                        viewModel.toggleWidgetMetric(context, "feels_like", !showFeelsLike)
                                    },
                                    label = { Text("Feels Like Temperature", fontSize = 12.sp) },
                                    leadingIcon = if (showFeelsLike) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("metric_chip_feels_like")
                                )
                            }
                        }
                    }
                }
            }

            // 3. Selection Search Input & Filter Tabs
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Select Location from Search History",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("widget_city_search_input"),
                        placeholder = { Text("Filter saved or recent cities...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search icon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilterTab == CityFilterTab.ALL,
                            onClick = {
                                haptics.performToggleMode()
                                selectedFilterTab = CityFilterTab.ALL
                            },
                            label = { Text("All Saved (${combinedCities.size})") },
                            leadingIcon = if (selectedFilterTab == CityFilterTab.ALL) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )

                        FilterChip(
                            selected = selectedFilterTab == CityFilterTab.FAVORITES,
                            onClick = {
                                haptics.performToggleMode()
                                selectedFilterTab = CityFilterTab.FAVORITES
                            },
                            label = { Text("Favorites (${favorites.size})") },
                            leadingIcon = { Icon(Icons.Outlined.Star, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )

                        FilterChip(
                            selected = selectedFilterTab == CityFilterTab.SEARCH_HISTORY,
                            onClick = {
                                haptics.performToggleMode()
                                selectedFilterTab = CityFilterTab.SEARCH_HISTORY
                            },
                            label = { Text("Recents (${recentSearches.size})") },
                            leadingIcon = { Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }
            }

            // 4. Saved Cities List
            if (combinedCities.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No Search History Found",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Search for cities on the main Weather tab to build your saved locations history for home screen widget pinning.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(combinedCities, key = { it.cityName }) { cityItem ->
                    val isCurrentSelection = selectedCity.equals(cityItem.cityName, ignoreCase = true)
                    val isCurrentlyPinned = pinnedWidgetCity.equals(cityItem.cityName, ignoreCase = true)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                haptics.performToggleMode()
                                selectedCity = cityItem.cityName
                            }
                            .testTag("widget_city_item_${cityItem.cityName.lowercase().replace(" ", "_")}"),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = if (isCurrentSelection || isCurrentlyPinned) 2.dp else 1.dp,
                            color = if (isCurrentlyPinned) Color(0xFF059669)
                            else if (isCurrentSelection) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentSelection) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (cityItem.isFavorite) Icons.Filled.Star else Icons.Default.History,
                                    contentDescription = null,
                                    tint = if (cityItem.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cityItem.cityName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isCurrentlyPinned) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF059669)
                                            ) {
                                                Text(
                                                    text = "PINNED",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = cityItem.subtext,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    haptics.performToggleMode()
                                    selectedCity = cityItem.cityName
                                    viewModel.setPinnedWidgetCity(context, cityItem.cityName)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCurrentlyPinned) Color(0xFF059669) else MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = if (isCurrentlyPinned) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("pin_button_${cityItem.cityName.lowercase().replace(" ", "_")}")
                            ) {
                                Text(
                                    text = if (isCurrentlyPinned) "Pinned" else "Pin Widget",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 5. Instruction Footer
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Widget Tip",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tip: Long-press any empty area on your Android Home Screen, select 'Widgets', then tap 'Weather' to place your pinned location widget.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

data class WidgetCityItem(
    val cityName: String,
    val isFavorite: Boolean,
    val subtext: String
)
