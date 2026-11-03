package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.Resource
import com.example.data.repository.WeatherData
import com.example.ui.components.rememberAppHaptics
import com.example.ui.viewmodel.WeatherState
import com.example.ui.viewmodel.WeatherViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

enum class WeatherMapLayer(val label: String, val icon: String) {
    PRECIPITATION("Precipitation Radar", "🌧️"),
    TEMPERATURE("Temperature Heatmap", "🌡️"),
    AIR_QUALITY("Air Quality (AQI)", "🌫️"),
    WIND("Wind Flow", "💨")
}

data class CityLocation(
    val name: String,
    val latLng: LatLng,
    val defaultTempC: Double,
    val defaultCondition: String
)

val POPULAR_CITIES = listOf(
    CityLocation("San Francisco", LatLng(37.7749, -122.4194), 18.0, "Partly Cloudy"),
    CityLocation("New York", LatLng(40.7128, -74.0060), 24.0, "Sunny"),
    CityLocation("London", LatLng(51.5074, -0.1278), 16.0, "Light Rain"),
    CityLocation("Tokyo", LatLng(35.6762, 139.6503), 26.0, "Clear"),
    CityLocation("Paris", LatLng(48.8566, 2.3522), 20.0, "Clouds"),
    CityLocation("Sydney", LatLng(-33.8688, 151.2093), 22.0, "Sunny"),
    CityLocation("Miami", LatLng(25.7617, -80.1918), 31.0, "Thunderstorm"),
    CityLocation("Chicago", LatLng(41.8781, -87.6298), 21.0, "Windy"),
    CityLocation("Seattle", LatLng(47.6062, -122.3321), 17.0, "Showers"),
    CityLocation("Berlin", LatLng(52.5200, 13.4050), 19.0, "Partly Cloudy")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveWeatherMapScreen(
    viewModel: WeatherViewModel,
    onBack: () -> Unit,
    initialCityName: String? = null,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()
    val scope = rememberCoroutineScope()
    val isCelsius by viewModel.isCelsius.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()

    // Determine initial camera focus
    val startCity = initialCityName ?: (weatherState as? WeatherState.Success)?.weather?.city ?: "San Francisco"

    val startLocation = POPULAR_CITIES.firstOrNull { it.name.equals(startCity, ignoreCase = true) }?.latLng
        ?: LatLng(37.7749, -122.4194)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startLocation, 9f)
    }

    // Active map state settings
    var selectedLayer by remember { mutableStateOf(WeatherMapLayer.PRECIPITATION) }
    var selectedMapType by remember { mutableStateOf(MapType.NORMAL) }
    var layerOpacity by remember { mutableFloatStateOf(0.75f) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCityWeather by remember { mutableStateOf<WeatherData?>(null) }
    var isLoadingCityWeather by remember { mutableStateOf(false) }

    // Time Playback Animation State for Radar (-120m to +60m)
    var timelineMinutes by remember { mutableIntStateOf(0) } // 0 = NOW
    var isPlayingRadar by remember { mutableStateOf(false) }

    // Auto animation ticker for radar frames when playing
    LaunchedEffect(isPlayingRadar) {
        while (isPlayingRadar) {
            delay(800)
            timelineMinutes = if (timelineMinutes >= 60) -120 else timelineMinutes + 15
        }
    }

    // Fetch weather details for focused camera position / selected marker
    var activeMarkerCity by remember { mutableStateOf(startCity) }

    val customApiKey by viewModel.customApiKey.collectAsState()

    LaunchedEffect(activeMarkerCity) {
        if (activeMarkerCity.isNotBlank()) {
            isLoadingCityWeather = true
            viewModel.repository.getWeather(activeMarkerCity, forceRefresh = false, userApiKey = customApiKey).collect { res ->
                if (res is Resource.Success) {
                    selectedCityWeather = res.data
                }
                isLoadingCityWeather = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Google Maps View with Custom Heatmap & Weather Overlay Canvas
        GoogleMap(
            modifier = Modifier.fillMaxSize().testTag("google_weather_map"),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = selectedMapType,
                isBuildingEnabled = true,
                isIndoorEnabled = false
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true,
                myLocationButtonEnabled = false
            )
        ) {
            // Render Markers for Popular & Favorite Cities
            POPULAR_CITIES.forEach { city ->
                Marker(
                    state = rememberMarkerState(position = city.latLng),
                    title = city.name,
                    snippet = "${city.defaultTempC.toInt()}°C • ${city.defaultCondition}",
                    onClick = {
                        haptics.performKeyTap()
                        activeMarkerCity = city.name
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(city.latLng, 10f)
                            )
                        }
                        true
                    }
                )
            }
        }

        // 2. Weather Heatmap / Radar Visual Canvas Overlay rendered on top of Map
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("weather_heatmap_overlay_canvas")
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Render dynamic visual weather patterns depending on selected layer & timeline offset
            when (selectedLayer) {
                WeatherMapLayer.PRECIPITATION -> {
                    // Animated Radar Cloud Bands
                    val offsetShift = (timelineMinutes + 120) * 1.5f
                    val pulse = (sin(timelineMinutes / 10.0) * 15).toFloat()

                    // Storm cell 1 (Heavy rain/thunder core)
                    val center1 = Offset(canvasWidth * 0.45f + offsetShift, canvasHeight * 0.40f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xCC9333EA).copy(alpha = layerOpacity), // Extreme Purple
                                Color(0xCCE11D48).copy(alpha = layerOpacity), // Red
                                Color(0xCCF59E0B).copy(alpha = layerOpacity), // Yellow/Orange
                                Color(0xCC10B981).copy(alpha = layerOpacity * 0.7f), // Light Rain Green
                                Color.Transparent
                            ),
                            center = center1,
                            radius = 280f + pulse
                        ),
                        center = center1,
                        radius = 280f + pulse
                    )

                    // Storm cell 2 (Scattered rain front)
                    val center2 = Offset(canvasWidth * 0.70f + (offsetShift * 0.8f), canvasHeight * 0.65f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xCC0284C7).copy(alpha = layerOpacity), // Moderate Rain Cyan
                                Color(0xCC34D399).copy(alpha = layerOpacity * 0.6f), // Light Rain
                                Color.Transparent
                            ),
                            center = center2,
                            radius = 220f
                        ),
                        center = center2,
                        radius = 220f
                    )

                    // Draw precipitation movement vector path lines
                    val path = Path().apply {
                        moveTo(canvasWidth * 0.2f, canvasHeight * 0.45f)
                        quadraticTo(
                            canvasWidth * 0.5f + offsetShift, canvasHeight * 0.35f,
                            canvasWidth * 0.85f + offsetShift, canvasHeight * 0.55f
                        )
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFF38BDF8).copy(alpha = 0.4f * layerOpacity),
                        style = Stroke(width = 4f)
                    )
                }

                WeatherMapLayer.TEMPERATURE -> {
                    // Thermal Heatmap Gradient Field
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xCCD97706).copy(alpha = layerOpacity), // Hot Warm Orange (30°C+)
                                Color(0xCC10B981).copy(alpha = layerOpacity * 0.8f), // Mild Green (20°C)
                                Color(0xCC0284C7).copy(alpha = layerOpacity * 0.8f), // Cool Cyan (10°C)
                                Color(0xCC3B82F6).copy(alpha = layerOpacity * 0.7f), // Cold Blue (<0°C)
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f),
                            radius = canvasWidth * 0.7f
                        )
                    )

                    // Secondary Thermal Pocket
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xCCE11D48).copy(alpha = layerOpacity * 0.9f), // Scorching Red
                                Color(0xCCF59E0B).copy(alpha = layerOpacity * 0.7f),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.8f, canvasHeight * 0.75f),
                            radius = 320f
                        ),
                        center = Offset(canvasWidth * 0.8f, canvasHeight * 0.75f),
                        radius = 320f
                    )
                }

                WeatherMapLayer.AIR_QUALITY -> {
                    // Air Quality Index Contour Zones
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xCCEF4444).copy(alpha = layerOpacity), // Unhealthy Red
                                Color(0xCCF59E0B).copy(alpha = layerOpacity * 0.8f), // Moderate Yellow
                                Color(0xCC10B981).copy(alpha = layerOpacity * 0.6f), // Good Green
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.55f, canvasHeight * 0.50f),
                            radius = 360f
                        ),
                        center = Offset(canvasWidth * 0.55f, canvasHeight * 0.50f),
                        radius = 360f
                    )
                }

                WeatherMapLayer.WIND -> {
                    // Wind Streamlines & Speed Vectors
                    for (i in 0..8) {
                        val startY = canvasHeight * (0.2f + i * 0.08f)
                        val waveX = (sin(i + timelineMinutes / 5.0) * 40).toFloat()
                        drawPath(
                            path = Path().apply {
                                moveTo(0f, startY)
                                cubicTo(
                                    canvasWidth * 0.3f, startY - 30f + waveX,
                                    canvasWidth * 0.7f, startY + 30f - waveX,
                                    canvasWidth, startY
                                )
                            },
                            color = Color(0xFF38BDF8).copy(alpha = 0.6f * layerOpacity),
                            style = Stroke(width = (i % 3 + 2).toFloat() * 1.5f)
                        )
                    }
                }
            }
        }

        // 3. Top Header Bar & Search Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptics.performKeyTap()
                            onBack()
                        },
                        modifier = Modifier.testTag("map_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search location on map...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear search",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("map_search_input")
                    )

                    // Quick Search Submit / Jump Button
                    IconButton(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                haptics.performSuccess()
                                activeMarkerCity = searchQuery.trim()
                                val matchedLocation = POPULAR_CITIES.firstOrNull {
                                    it.name.contains(searchQuery.trim(), ignoreCase = true)
                                }?.latLng ?: startLocation
                                scope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(matchedLocation, 10f)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.testTag("map_search_submit")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Jump to city",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Quick Jump City Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(POPULAR_CITIES.take(6)) { city ->
                    Surface(
                        onClick = {
                            haptics.performKeyTap()
                            activeMarkerCity = city.name
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(city.latLng, 10f)
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (activeMarkerCity.equals(city.name, ignoreCase = true)) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                        },
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = city.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeMarkerCity.equals(city.name, ignoreCase = true)) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 4. Right Action Buttons: Layer Opacity, Map Type Toggle & Re-center
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Map Type Toggle Button (Normal -> Satellite -> Terrain)
            FloatingActionButton(
                onClick = {
                    haptics.performToggleMode()
                    selectedMapType = when (selectedMapType) {
                        MapType.NORMAL -> MapType.SATELLITE
                        MapType.SATELLITE -> MapType.TERRAIN
                        else -> MapType.NORMAL
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(44.dp).testTag("map_type_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Toggle Map Type",
                    modifier = Modifier.size(20.dp)
                )
            }

            // My Location Re-center FAB
            FloatingActionButton(
                onClick = {
                    haptics.performKeyTap()
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(startLocation, 10f)
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(44.dp).testTag("my_location_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 5. Bottom Control Sheet: Weather Layers, Radar Playback & Heatmap Legend
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Focused City Weather Detail Callout Card
            selectedCityWeather?.let { weather ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth().testTag("map_weather_callout_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Thermostat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = weather.city,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${weather.conditionText} • Hum ${weather.humidity}% • Wind ${weather.windKph.toInt()} km/h",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val tempStr = if (isCelsius) "${weather.tempC.toInt()}°C" else "${weather.tempF.toInt()}°F"
                            Text(
                                text = tempStr,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "UV ${weather.uv.toInt()} • AQI ${weather.aqi}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Map Overlay Layer Selector & Timeline Controls Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier.fillMaxWidth().testTag("map_controls_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Weather Overlay Mode Selector Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(WeatherMapLayer.entries) { layer ->
                            FilterChip(
                                selected = selectedLayer == layer,
                                onClick = {
                                    haptics.performToggleMode()
                                    selectedLayer = layer
                                },
                                label = { Text("${layer.icon} ${layer.label}", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                leadingIcon = if (selectedLayer == layer) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("layer_chip_${layer.name.lowercase()}")
                            )
                        }
                    }

                    // Layer Opacity Slider Control
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Overlay Opacity:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = layerOpacity,
                            onValueChange = { layerOpacity = it },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f).height(24.dp).testTag("layer_opacity_slider")
                        )
                        Text(
                            text = "${(layerOpacity * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Heatmap Legend Bar
                    HeatmapLegendBar(selectedLayer = selectedLayer)

                    // Precipitation Radar Animated Timeline Control Bar
                    if (selectedLayer == WeatherMapLayer.PRECIPITATION) {
                        HorizontalDividerBar()

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val timeLabel = when {
                                        timelineMinutes == 0 -> "NOW (LIVE RADAR)"
                                        timelineMinutes < 0 -> "${-timelineMinutes} min ago"
                                        else -> "+$timelineMinutes min forecast"
                                    }
                                    if (timelineMinutes == 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color.Red)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = timeLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (timelineMinutes == 0) Color.Red else MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            haptics.performKeyTap()
                                            timelineMinutes = if (timelineMinutes <= -120) 60 else timelineMinutes - 15
                                        },
                                        modifier = Modifier.size(28.dp).testTag("radar_step_prev")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SkipPrevious,
                                            contentDescription = "Previous frame",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            haptics.performToggleMode()
                                            isPlayingRadar = !isPlayingRadar
                                        },
                                        modifier = Modifier.size(32.dp).testTag("radar_play_pause")
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingRadar) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlayingRadar) "Pause" else "Play Radar Animation",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            haptics.performKeyTap()
                                            timelineMinutes = if (timelineMinutes >= 60) -120 else timelineMinutes + 15
                                        },
                                        modifier = Modifier.size(28.dp).testTag("radar_step_next")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SkipNext,
                                            contentDescription = "Next frame",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Slider(
                                value = timelineMinutes.toFloat(),
                                onValueChange = { timelineMinutes = it.toInt() },
                                valueRange = -120f..60f,
                                steps = 11,
                                modifier = Modifier.fillMaxWidth().height(24.dp).testTag("radar_timeline_slider")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeatmapLegendBar(selectedLayer: WeatherMapLayer) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val (colors, labels) = when (selectedLayer) {
            WeatherMapLayer.PRECIPITATION -> Pair(
                listOf(Color(0xFF10B981), Color(0xFF0284C7), Color(0xFFF59E0B), Color(0xFFE11D48), Color(0xFF9333EA)),
                listOf("Light", "Mod", "Heavy", "Extreme")
            )
            WeatherMapLayer.TEMPERATURE -> Pair(
                listOf(Color(0xFF3B82F6), Color(0xFF0284C7), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFE11D48)),
                listOf("<0°C", "10°C", "20°C", "30°C", "40°C+")
            )
            WeatherMapLayer.AIR_QUALITY -> Pair(
                listOf(Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6)),
                listOf("Good", "Moderate", "Unhealthy", "Hazardous")
            )
            WeatherMapLayer.WIND -> Pair(
                listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF6366F1), Color(0xFF4338CA)),
                listOf("Calm", "Breeze", "Gale", "Storm")
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Brush.horizontalGradient(colors = colors))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun HorizontalDividerBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}
