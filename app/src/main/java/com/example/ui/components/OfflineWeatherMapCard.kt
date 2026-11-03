package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CompassCalibration
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.OfflinePin
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.SignalCellularConnectedNoInternet4Bar
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WindPower
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.WeatherApplication
import com.example.data.local.CachedWeatherMapEntity
import com.example.data.location.GeocodingService
import com.example.data.repository.WeatherData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OfflineWeatherMapCard(
    weather: WeatherData,
    theme: WeatherTheme,
    isCelsius: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as? WeatherApplication
    val repository = remember { app?.container?.weatherRepository }
    val scope = rememberCoroutineScope()

    // Retrieve lat/lon coordinates for current city
    val coordinates = remember(weather.city) {
        GeocodingService.getCoordinatesFromCity(context, weather.city)
    }
    val cityLat = coordinates.first
    val cityLon = coordinates.second

    // Room DB Flow for Cached Map
    val mapFlow = remember(weather.city, repository) {
        repository?.getCachedWeatherMapFlow(weather.city)
    }
    val cachedMapEntity by (mapFlow?.collectAsState(initial = null) ?: remember { mutableStateOf(null) })

    var isSimulatedOffline by remember { mutableStateOf(false) }
    var selectedLayer by remember { mutableIntStateOf(0) } // 0: Radar, 1: Temp, 2: Wind, 3: Clouds
    var zoomLevel by remember { mutableIntStateOf(10) }
    var isSavingToDb by remember { mutableStateOf(false) }

    // Auto-ensure map cache is populated in Room database
    LaunchedEffect(weather.city, repository) {
        if (repository != null) {
            repository.ensureOrGenerateMapCache(context, weather.city)
        }
    }

    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor

    val isOfflineMode = isSimulatedOffline || (cachedMapEntity != null && isSimulatedOffline)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("offline_weather_map_card"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row with Connectivity Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOfflineMode) Color(0xFFFF9800).copy(alpha = 0.15f)
                                else Color(0xFF0288D1).copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isOfflineMode) Icons.Outlined.SignalCellularConnectedNoInternet4Bar else Icons.Outlined.Map,
                            contentDescription = "Map Radar Status",
                            tint = if (isOfflineMode) Color(0xFFFF9800) else Color(0xFF0288D1),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Local Weather Map",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isOfflineMode) Color(0xFFFF9800).copy(alpha = 0.2f) else Color(0xFF22C55E).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (isOfflineMode) "OFFLINE CACHE" else "LIVE RADAR",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isOfflineMode) Color(0xFFFF9800) else Color(0xFF22C55E),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${weather.city} (${"%.3f".format(Locale.US, cityLat)}°, ${"%.3f".format(Locale.US, cityLon)}°)",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }

                // Network Loss Simulation Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isSimulatedOffline = !isSimulatedOffline }
                        .testTag("simulate_offline_map_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSimulatedOffline) Icons.Outlined.SignalCellularConnectedNoInternet4Bar else Icons.Outlined.Wifi,
                            contentDescription = "Toggle Network Status",
                            tint = if (isSimulatedOffline) Color(0xFFFF9800) else Color(0xFF22C55E),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSimulatedOffline) "Offline Mode" else "Online",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }
                }
            }

            // Layer Selection Tabs
            TabRow(
                selectedTabIndex = selectedLayer,
                containerColor = Color.Transparent,
                contentColor = primaryText,
                divider = { HorizontalDivider(color = cardBorder.copy(alpha = 0.5f)) },
                indicator = { tabPositions ->
                    if (selectedLayer < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedLayer]),
                            height = 3.dp,
                            color = when (selectedLayer) {
                                0 -> Color(0xFF29B6F6) // Precip
                                1 -> Color(0xFFFF7043) // Temp
                                2 -> Color(0xFF26A69A) // Wind
                                else -> Color(0xFFAB47BC) // Cloud
                            }
                        )
                    }
                }
            ) {
                val layers = listOf(
                    Triple("Radar", Icons.Outlined.WaterDrop, Color(0xFF29B6F6)),
                    Triple("Temp", Icons.Outlined.Thermostat, Color(0xFFFF7043)),
                    Triple("Wind", Icons.Outlined.WindPower, Color(0xFF26A69A)),
                    Triple("Clouds", Icons.Outlined.Cloud, Color(0xFFAB47BC))
                )

                layers.forEachIndexed { idx, (title, icon, color) ->
                    Tab(
                        selected = selectedLayer == idx,
                        onClick = { selectedLayer = idx },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (selectedLayer == idx) color else secondaryText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(title, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }
            }

            // Interactive Map Visual Canvas Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .testTag("offline_map_canvas_container")
            ) {
                WeatherMapCanvas(
                    cityLat = cityLat,
                    cityLon = cityLon,
                    cityName = weather.city,
                    layerIndex = selectedLayer,
                    zoomLevel = zoomLevel,
                    isOffline = isOfflineMode,
                    cachedEntity = cachedMapEntity,
                    weatherData = weather
                )

                // Map Overlay Controls (Top Right)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        contentColor = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { if (zoomLevel < 14) zoomLevel++ }
                            .testTag("map_zoom_in_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Add, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        contentColor = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { if (zoomLevel > 6) zoomLevel-- }
                            .testTag("map_zoom_out_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Map Legend / Scale Bar (Bottom Left)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(3.dp)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${100 / zoomLevel} km | Zoom $zoomLevel",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Offline Watermark Indicator (Bottom Right)
                if (isOfflineMode) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF9800).copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.OfflinePin,
                                contentDescription = "Room Offline Data",
                                tint = Color.Black,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ROOM OFFLINE CACHE",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // Room Database Caching Details Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF0F4F8),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("room_map_cache_info_panel")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Storage,
                                contentDescription = "Room Database",
                                tint = Color(0xFF0288D1),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Room Cache: cached_weather_maps",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                        }

                        if (cachedMapEntity != null) {
                            val formattedDate = remember(cachedMapEntity?.cachedAtTimestamp) {
                                val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                                sdf.format(Date(cachedMapEntity?.cachedAtTimestamp ?: System.currentTimeMillis()))
                            }
                            Text(
                                text = "Saved $formattedDate",
                                fontSize = 10.5.sp,
                                color = secondaryText
                            )
                        } else {
                            Text(
                                text = "No Cache Stored",
                                fontSize = 10.5.sp,
                                color = Color(0xFFFF7043)
                            )
                        }
                    }

                    HorizontalDivider(color = cardBorder.copy(alpha = 0.5f))

                    // Bounding Box Coordinates and Grid Cell count
                    val northLat = cachedMapEntity?.boundingBoxNorthLat ?: (cityLat + 0.25)
                    val southLat = cachedMapEntity?.boundingBoxSouthLat ?: (cityLat - 0.25)
                    val eastLon = cachedMapEntity?.boundingBoxEastLon ?: (cityLon + 0.35)
                    val westLon = cachedMapEntity?.boundingBoxWestLon ?: (cityLon - 0.35)

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val pillMod = Modifier.weight(1f)

                        MapMetricPill(
                            label = "Center Coordinates",
                            value = "${"%.3f".format(Locale.US, cityLat)}°, ${"%.3f".format(Locale.US, cityLon)}°",
                            icon = Icons.Outlined.MyLocation,
                            color = Color(0xFF0288D1),
                            theme = theme,
                            modifier = pillMod
                        )

                        MapMetricPill(
                            label = "Bounding Box",
                            value = "${"%.2f".format(Locale.US, southLat)}°-${"%.2f".format(Locale.US, northLat)}° N",
                            icon = Icons.Outlined.CompassCalibration,
                            color = Color(0xFFAB47BC),
                            theme = theme,
                            modifier = pillMod
                        )

                        MapMetricPill(
                            label = "Offline Availability",
                            value = if (cachedMapEntity != null) "READY (36 GRID CELLS)" else "PENDING CACHE",
                            icon = Icons.Outlined.DownloadForOffline,
                            color = if (cachedMapEntity != null) Color(0xFF22C55E) else Color(0xFFFF7043),
                            theme = theme,
                            modifier = pillMod
                        )
                    }

                    // Save / Refresh Offline Map Cache Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0288D1),
                        contentColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                repository?.let { repo ->
                                    scope.launch {
                                        isSavingToDb = true
                                        repo.ensureOrGenerateMapCache(context, weather.city, forceRefresh = true)
                                        isSavingToDb = false
                                    }
                                }
                            }
                            .testTag("save_offline_map_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DownloadForOffline,
                                contentDescription = "Cache Map",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSavingToDb) "Caching Coordinate Grid to Room..." else "Save Map to Room DB for Offline Access",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMapCanvas(
    cityLat: Double,
    cityLon: Double,
    cityName: String,
    layerIndex: Int,
    zoomLevel: Int,
    isOffline: Boolean,
    cachedEntity: CachedWeatherMapEntity?,
    weatherData: WeatherData
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .testTag("weather_map_canvas")
    ) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h / 2f

        // Base Map Dark Background with subtle grid topography
        drawRect(color = Color(0xFF0F172A))

        // Draw Coordinate Grid Mesh (Lat/Lon lines)
        val gridStepX = w / 6f
        val gridStepY = h / 5f

        for (i in 1..5) {
            val x = i * gridStepX
            drawLine(
                color = Color(0xFF334155).copy(alpha = 0.5f),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
            )
        }

        for (j in 1..4) {
            val y = j * gridStepY
            drawLine(
                color = Color(0xFF334155).copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
            )
        }

        // Render Map Overlays (0: Radar, 1: Temp, 2: Wind, 3: Clouds)
        val hash = Math.abs(cityName.hashCode())
        val seed = (hash % 100) / 100.0

        when (layerIndex) {
            0 -> {
                // Precipitation Radar Echo Heatmap
                for (r in 0 until 6) {
                    for (c in 0 until 6) {
                        val cellX = c * gridStepX
                        val cellY = r * gridStepY
                        val intensity = ((hash + r * 17 + c * 11) % 100) / 100.0

                        if (intensity > 0.35) {
                            val radarColor = when {
                                intensity > 0.85 -> Color(0xFFE91E63).copy(alpha = 0.65f) // Extreme Storm
                                intensity > 0.70 -> Color(0xFFF44336).copy(alpha = 0.60f) // Heavy Rain
                                intensity > 0.50 -> Color(0xFFFFEB3B).copy(alpha = 0.55f) // Moderate Rain
                                else -> Color(0xFF4CAF50).copy(alpha = 0.45f) // Light Rain
                            }

                            drawRoundRect(
                                color = radarColor,
                                topLeft = Offset(cellX + 4.dp.toPx(), cellY + 4.dp.toPx()),
                                size = Size(gridStepX - 8.dp.toPx(), gridStepY - 8.dp.toPx()),
                                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                            )
                        }
                    }
                }
            }
            1 -> {
                // Temperature Isobar Lines & Gradient Field
                val tempGrad = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF7043).copy(alpha = 0.5f),
                        Color(0xFFFFB74D).copy(alpha = 0.3f),
                        Color(0xFF26A69A).copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = w * 0.7f
                )
                drawRect(brush = tempGrad)

                // Isobar contour circles
                for (radiusStep in listOf(0.25f, 0.45f, 0.65f)) {
                    drawCircle(
                        color = Color(0xFFFFB74D).copy(alpha = 0.6f),
                        radius = w * radiusStep,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                    )
                }
            }
            2 -> {
                // Wind Speed Vector Field Arrows
                for (r in 0 until 5) {
                    for (c in 0 until 6) {
                        val vx = (c + 0.5f) * gridStepX
                        val vy = (r + 0.5f) * gridStepY
                        val angle = (weatherData.windDegree + (r * 15) - (c * 10)) * (Math.PI / 180.0)
                        val length = 18.dp.toPx()

                        val endX = vx + (length * cos(angle)).toFloat()
                        val endY = vy + (length * sin(angle)).toFloat()

                        drawLine(
                            color = Color(0xFF26A69A).copy(alpha = 0.75f),
                            start = Offset(vx, vy),
                            end = Offset(endX, endY),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        drawCircle(color = Color(0xFF26A69A), radius = 2.5.dp.toPx(), center = Offset(endX, endY))
                    }
                }
            }
            3 -> {
                // Cloud Cover Satellite Overlay
                val cloudPath1 = Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(centerX - 90.dp.toPx(), centerY - 60.dp.toPx(), centerX + 70.dp.toPx(), centerY + 40.dp.toPx()))
                }
                drawPath(path = cloudPath1, color = Color.White.copy(alpha = 0.25f))

                val cloudPath2 = Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(centerX - 140.dp.toPx(), centerY - 20.dp.toPx(), centerX - 10.dp.toPx(), centerY + 70.dp.toPx()))
                }
                drawPath(path = cloudPath2, color = Color.White.copy(alpha = 0.20f))
            }
        }

        // Center Marker for City Coordinates
        // Pulsing radar ripple
        drawCircle(
            color = Color(0xFF0288D1).copy(alpha = (1f - (pulseRadius - 12f) / 16f).coerceIn(0f, 1f)),
            radius = pulseRadius.dp.toPx(),
            center = Offset(centerX, centerY)
        )

        // Center Pin
        drawCircle(color = Color(0xFF0288D1), radius = 7.dp.toPx(), center = Offset(centerX, centerY))
        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(centerX, centerY))

        // Center Pin Crosshairs
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(centerX - 14.dp.toPx(), centerY),
            end = Offset(centerX + 14.dp.toPx(), centerY),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(centerX, centerY - 14.dp.toPx()),
            end = Offset(centerX, centerY + 14.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
private fun MapMetricPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val isDark = theme.primaryTextColor == Color.White
    val bg = if (isDark) Color(0xFF1E293B) else Color.White

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = label, fontSize = 9.5.sp, color = theme.secondaryTextColor)
                Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.primaryTextColor)
            }
        }
    }
}
