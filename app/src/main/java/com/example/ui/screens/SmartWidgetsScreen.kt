package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HomeCardVisibility
import com.example.ui.components.rememberAppHaptics
import com.example.ui.viewmodel.WeatherViewModel

data class SmartWidgetCardItem(
    val key: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isEnabled: (HomeCardVisibility) -> Boolean
)

@Composable
fun SmartWidgetsScreen(
    viewModel: WeatherViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()
    val visibility by viewModel.cardVisibility.collectAsState()
    val scrollState = rememberScrollState()

    val totalCards = 22
    val activeCards = listOf(
        visibility.showAudioSummary,
        visibility.showSmartInsights,
        visibility.showHourlyForecast,
        visibility.showPrecipitation,
        visibility.showTemperatureTrend,
        visibility.showDailyForecast,
        visibility.showWindDashboard,
        visibility.showWindChill,
        visibility.showThermalComfort,
        visibility.showHumidityGauge,
        visibility.showAirQuality,
        visibility.showPollen,
        visibility.showUvIndex,
        visibility.showMoonPhase,
        visibility.showClimateTrends,
        visibility.showClimateComparison,
        visibility.showNarrativeTimeline,
        visibility.showOfflineMap,
        visibility.showWidgetPreview,
        visibility.showGpsSun,
        visibility.showSunCycle,
        visibility.showStatsGrid
    ).count { it }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("smart_widgets_screen"),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptics.performKeyTap()
                            onBack()
                        },
                        modifier = Modifier.testTag("smart_widgets_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to previous screen",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart Widgets Manager",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Customize active dashboard cards",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Status Bar with Card Count Indicator
            Card(
                modifier = Modifier.fillMaxWidth().testTag("smart_widgets_status_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Widgets,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Home Layout Density",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$activeCards of $totalCards cards visible",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${(activeCards * 100 / totalCards)}% Active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { activeCards.toFloat() / totalCards },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }

            // Quick Preset Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "QUICK PRESETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            haptics.performKeyTap()
                            viewModel.setAllCardsVisibility(true)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preset_all_cards"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Show All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            haptics.performKeyTap()
                            viewModel.applyPresetMinimalist()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preset_minimalist"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Minimalist", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            haptics.performKeyTap()
                            viewModel.setAllCardsVisibility(false)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preset_disable_all"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hide All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Group 1: Core Forecasts
            WidgetGroupSection(
                title = "CORE FORECASTS & ADVICE",
                items = listOf(
                    SmartWidgetCardItem("audio_summary", "Daily Audio Summary", "Voice-synthesized morning/evening weather digest", Icons.Outlined.VolumeUp) { it.showAudioSummary },
                    SmartWidgetCardItem("smart_insights", "Gemini AI Smart Insights", "Contextual AI weather advice & activity suggestions", Icons.Default.AutoAwesome) { it.showSmartInsights },
                    SmartWidgetCardItem("hourly_forecast", "24-Hour Hourly Forecast", "Horizontal hourly temperature & precipitation timeline", Icons.Outlined.Timeline) { it.showHourlyForecast },
                    SmartWidgetCardItem("precipitation", "12-Hour Rain Probability", "Precipitation chance and rain volume breakdown", Icons.Outlined.WaterDrop) { it.showPrecipitation },
                    SmartWidgetCardItem("temp_trend", "Temperature Trend Graph", "Visual curve graph of upcoming 24-hour temperatures", Icons.Outlined.ShowChart) { it.showTemperatureTrend },
                    SmartWidgetCardItem("daily_forecast", "7-Day Extended Forecast", "Multi-day forecast list with high/low range bars", Icons.Outlined.DateRange) { it.showDailyForecast },
                    SmartWidgetCardItem("stats_grid", "Atmospheric Stats Grid", "Compact grid with pressure, visibility & cloud cover", Icons.Outlined.Tune) { it.showStatsGrid }
                ),
                visibility = visibility,
                onToggle = { key, show -> viewModel.setCardVisibility(key, show) },
                haptics = haptics
            )

            // Group 2: Atmospheric & Biometeorology
            WidgetGroupSection(
                title = "ATMOSPHERIC & BIOMETEOROLOGY",
                items = listOf(
                    SmartWidgetCardItem("thermal_comfort", "Thermal Comfort Index", "Apparent heat calculation & clothing layer strategy", Icons.Outlined.Checkroom) { it.showThermalComfort },
                    SmartWidgetCardItem("wind_dashboard", "Wind Speed & Compass", "Animated compass, wind gust speeds & direction", Icons.Outlined.Air) { it.showWindDashboard },
                    SmartWidgetCardItem("wind_chill", "Wind Chill & Frostbite Calculator", "National Weather Service formula & skin safety timer", Icons.Outlined.Thermostat) { it.showWindChill },
                    SmartWidgetCardItem("humidity_gauge", "Humidity & Dew Point Gauge", "Circular relative humidity gauge & comfort rating", Icons.Outlined.Opacity) { it.showHumidityGauge },
                    SmartWidgetCardItem("air_quality", "Air Quality Index (AQI)", "Particulate matter breakdown & respiratory health rating", Icons.Outlined.CloudSync) { it.showAirQuality },
                    SmartWidgetCardItem("pollen", "Pollen & Allergy Index", "Grass, tree & weed pollen count forecast", Icons.Outlined.Grass) { it.showPollen },
                    SmartWidgetCardItem("uv_index", "UV Radiation Dashboard", "Ultraviolet exposure index & sun protection advice", Icons.Outlined.WbSunny) { it.showUvIndex }
                ),
                visibility = visibility,
                onToggle = { key, show -> viewModel.setCardVisibility(key, show) },
                haptics = haptics
            )

            // Group 3: Astronomy, Radar & History
            WidgetGroupSection(
                title = "ASTRONOMY, RADAR & HISTORICAL CLIMATE",
                items = listOf(
                    SmartWidgetCardItem("moon_phase", "Moon Phase Tracker", "Lunar phase cycle, illumination percentage & moonrise", Icons.Outlined.Nightlight) { it.showMoonPhase },
                    SmartWidgetCardItem("climate_trends", "Monthly Climate Trends", "Room database historical monthly averages", Icons.Outlined.ShowChart) { it.showClimateTrends },
                    SmartWidgetCardItem("climate_comparison", "Historical 5-Year Baseline", "Comparing current temperatures to historical records", Icons.Outlined.DateRange) { it.showClimateComparison },
                    SmartWidgetCardItem("narrative_timeline", "Daily Narrative Timeline", "Story-driven weather narrative log", Icons.Outlined.Timeline) { it.showNarrativeTimeline },
                    SmartWidgetCardItem("offline_map", "Offline Weather Radar Map", "Locally cached radar tile map for offline use", Icons.Outlined.Map) { it.showOfflineMap },
                    SmartWidgetCardItem("widget_preview", "Android Home Widget Config", "Preview & pin city widgets to Android desktop", Icons.Outlined.Widgets) { it.showWidgetPreview },
                    SmartWidgetCardItem("gps_sun", "GPS Sunrise & Sunset Calculator", "Exact solar calculation based on GPS position", Icons.Outlined.WbSunny) { it.showGpsSun },
                    SmartWidgetCardItem("sun_cycle", "Solar Path Visualizer", "Sun trajectory arc across the sky", Icons.Outlined.WbSunny) { it.showSunCycle }
                ),
                visibility = visibility,
                onToggle = { key, show -> viewModel.setCardVisibility(key, show) },
                haptics = haptics
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WidgetGroupSection(
    title: String,
    items: List<SmartWidgetCardItem>,
    visibility: HomeCardVisibility,
    onToggle: (String, Boolean) -> Unit,
    haptics: com.example.ui.components.AppHaptics
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            items.forEach { item ->
                val isChecked = item.isEnabled(visibility)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptics.performToggleMode()
                            onToggle(item.key, !isChecked)
                        }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.description,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isChecked,
                        onCheckedChange = { show ->
                            haptics.performToggleMode()
                            onToggle(item.key, show)
                        },
                        modifier = Modifier.testTag("switch_${item.key}"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}
