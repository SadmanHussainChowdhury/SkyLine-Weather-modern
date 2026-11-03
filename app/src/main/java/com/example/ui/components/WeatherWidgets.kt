package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.WeatherData

@Composable
fun WeatherAlertBanner(
    alertHeadline: String?,
    alertDesc: String?,
    city: String,
    theme: WeatherTheme,
    onScanGps: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isDismissed by remember(alertHeadline) { mutableStateOf(false) }

    if (isDismissed) return

    val hasAlert = !alertHeadline.isNullOrBlank()

    if (!hasAlert) {
        // Beautiful calm green banner displaying "Severe Weather Status: Clear"
        val isDarkTheme = theme.primaryTextColor == Color.White
        val containerColor = if (isDarkTheme) Color(0xFF2E7D32).copy(alpha = 0.25f) else Color(0xFFE8F5E9).copy(alpha = 0.85f)
        val contentColor = if (isDarkTheme) Color(0xFFA5D6A7) else Color(0xFF1B5E20)
        val borderColor = if (isDarkTheme) Color(0xFF81C784).copy(alpha = 0.4f) else Color(0xFFC8E6C9)

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("weather_alert_clear_banner"),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Safe/Clear Weather Status Icon",
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Weather Alerts: Clear",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                        Text(
                            text = "No severe weather reports in $city.",
                            fontSize = 11.sp,
                            color = contentColor.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                if (onScanGps != null) {
                    IconButton(
                        onClick = onScanGps,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("gps_scan_alerts_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Scan GPS Location Alerts",
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    } else {
        // Active warning/alert exist: Red/Amber pulsing severe weather card
        var isExpanded by rememberSaveable { mutableStateOf(false) }

        // Subtle pulsing animation on the warning icon to grab attention
        val infiniteTransition = rememberInfiniteTransition(label = "pulse_animation")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )

        val lowercaseHeadline = alertHeadline!!.lowercase()
        val isWarning = lowercaseHeadline.contains("warning") || lowercaseHeadline.contains("tornado") || lowercaseHeadline.contains("severe")
        val isWatch = lowercaseHeadline.contains("watch")

        val alertBgColor = when {
            isWarning -> Color(0xFFC62828) // Urgent Red
            isWatch -> Color(0xFFE65100)   // Medium Alert Orange
            else -> Color(0xFFF57C00)      // Amber Advisory Yellow-Orange
        }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("weather_alert_banner")
                .animateContentSize()
                .clickable { isExpanded = !isExpanded },
            colors = CardDefaults.cardColors(
                containerColor = alertBgColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Severe Alert Icon",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(pulseScale)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val badgeText = when {
                            isWarning -> "WARNING"
                            isWatch -> "WATCH"
                            else -> "ADVISORY"
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = alertHeadline,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand or Collapse Alert",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.25f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Alert description
                    if (!alertDesc.isNullOrBlank()) {
                        Text(
                            text = alertDesc,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.92f),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Dynamic recommendations list
                    val recommendations = remember(alertHeadline) {
                        val headline = alertHeadline.lowercase()
                        when {
                            headline.contains("thunderstorm") || headline.contains("tornado") || headline.contains("wind") || headline.contains("storm") -> {
                                listOf(
                                    "⚡ Secure loose outdoor items and move indoors.",
                                    "🔌 Keep away from windows and unplug sensitive electrical devices.",
                                    "🛡️ Seek high-quality interior rooms or basement shelter if sirens sound."
                                )
                            }
                            headline.contains("winter") || headline.contains("snow") || headline.contains("ice") || headline.contains("blizzard") || headline.contains("freeze") -> {
                                listOf(
                                    "🚗 Avoid non-essential vehicular travel on snow/icy roads.",
                                    "🧣 Dress in thick layers and cover all exposed skin if outdoors.",
                                    "🚰 Keep indoor plumbing cabinet doors open and faucet valves trickling."
                                )
                            }
                            headline.contains("heat") || headline.contains("warm") || headline.contains("high temp") -> {
                                listOf(
                                    "💧 Hydrate heavily and rest in shade or cooled environments.",
                                    "☀️ post-pone intensive physical work to morning/evening cooler periods.",
                                    "🧴 Regularly apply sunscreen and monitor high-risk vulnerable groups."
                                )
                            }
                            headline.contains("flood") || headline.contains("rain") || headline.contains("flash") -> {
                                listOf(
                                    "🌊 Avoid walking or driving across rapid standing water streams.",
                                    "🎒 Prepare emergency survival bags with critical documents.",
                                    "🏠 Move critical hardware to higher floors if lower levels are compromised."
                                )
                            }
                            else -> {
                                listOf(
                                    "⚠️ Closely track news announcements and regional broadcasts.",
                                    "📦 Inspect household emergency supplies, water, and food.",
                                    "🛡️ Fully abide by local evacuation instructions when specified."
                                )
                            }
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "SAFETY GUIDANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.75f),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        recommendations.forEach { recommendation ->
                            Text(
                                text = recommendation,
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, alertHeadline)
                                    putExtra(Intent.EXTRA_TEXT, "🚨 $alertHeadline in $city\n\n$alertDesc\n\nStay safe! Shared via Skyline Weather.")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Weather Alert"))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.15f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = ButtonDefaults.ContentPadding,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                isDismissed = true
                                Toast.makeText(context, "Alert banner dismissed", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = alertBgColor
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = ButtonDefaults.ContentPadding,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Acknowledge",
                                tint = alertBgColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dismiss", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherStatsGrid(
    weather: WeatherData,
    useCelsius: Boolean,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val tempUnit = if (useCelsius) "°C" else "°F"
    val feelsLikeStr = if (useCelsius) "${weather.feelsLikeC.toInt()}$tempUnit" else "${weather.feelsLikeF.toInt()}$tempUnit"
    val windSpeedVal = if (useCelsius) weather.windKph else weather.windMph
    val windUnitStr = if (useCelsius) "km/h" else "mph"
    val windStr = "${windSpeedVal.toInt()} $windUnitStr"

    // AQI Status
    val aqiLabel = when (weather.aqi) {
        1 -> "Good"
        2 -> "Moderate"
        3 -> "Sensitive"
        4 -> "Unhealthy"
        5 -> "Very Unhealthy"
        6 -> "Hazardous"
        else -> "Good"
    }
    val aqiColor = when (weather.aqi) {
        1 -> Color(0xFF10B981)
        2 -> Color(0xFFF59E0B)
        3 -> Color(0xFFF97316)
        4 -> Color(0xFFEF4444)
        5 -> Color(0xFFA855F7)
        else -> Color(0xFFEF4444)
    }

    // Humidity Status & Progress
    val humidityProgress = (weather.humidity / 100f).coerceIn(0f, 1f)
    val humidityStatus = when {
        weather.humidity < 30 -> "Dry air"
        weather.humidity in 30..60 -> "Comfortable"
        weather.humidity in 61..80 -> "Humid"
        else -> "High moisture"
    }

    // UV Status & Progress (0 to 12)
    val uvVal = weather.uv
    val uvProgress = (uvVal / 12.0).toFloat().coerceIn(0f, 1f)
    val uvStatus = when {
        uvVal <= 2 -> "Low"
        uvVal <= 5 -> "Moderate"
        uvVal <= 7 -> "High"
        uvVal <= 10 -> "Very High"
        else -> "Extreme"
    }
    val uvAdvice = when {
        uvVal <= 2 -> "Low risk"
        uvVal <= 5 -> "SPF 30 recommended"
        uvVal <= 7 -> "Protection required"
        else -> "Avoid peak sun"
    }

    // Wind Speed Progress (0 to 60 km/h)
    val windMax = if (useCelsius) 60.0 else 38.0
    val windProgress = (windSpeedVal / windMax).toFloat().coerceIn(0f, 1f)

    // Visibility
    val visVal = if (useCelsius) weather.visibilityKm else weather.visibilityMiles
    val visUnit = if (useCelsius) "km" else "mi"
    val visMax = if (useCelsius) 10.0 else 6.2
    val visProgress = (visVal / visMax).toFloat().coerceIn(0f, 1f)
    val visStatus = if (visVal >= visMax * 0.9) "Clear view" else if (visVal >= visMax * 0.5) "Moderate haze" else "Low visibility"

    // Pressure Progress (980 to 1040 hPa)
    val pressureVal = weather.pressureMb
    val pressureProgress = ((pressureVal - 980) / 60.0).toFloat().coerceIn(0f, 1f)
    val pressureStatus = when {
        pressureVal < 1005 -> "Low pressure"
        pressureVal in 1005.0..1020.0 -> "Steady pressure"
        else -> "High pressure"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("weather_metrics_grid_section")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weather Metrics Grid",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = theme.primaryTextColor
            )
            Text(
                text = "Live Gauges",
                fontSize = 12.sp,
                color = theme.secondaryTextColor,
                fontWeight = FontWeight.Medium
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val itemWidthModifier = Modifier.weight(1f)

            // 1. Humidity Card with Gauge
            GaugeMetricCard(
                title = "HUMIDITY",
                value = "${weather.humidity}%",
                subtitle = humidityStatus,
                icon = Icons.Outlined.WaterDrop,
                progress = humidityProgress,
                gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                theme = theme,
                testTag = "stat_humidity",
                modifier = itemWidthModifier
            )

            // 2. UV Index Card with Gauge
            GaugeMetricCard(
                title = "UV INDEX",
                value = "${weather.uv.toInt()} • $uvStatus",
                subtitle = uvAdvice,
                icon = Icons.Outlined.WbSunny,
                progress = uvProgress,
                gradientColors = listOf(Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444)),
                theme = theme,
                testTag = "stat_uv",
                modifier = itemWidthModifier
            )

            // 3. Wind Speed Card with Gauge
            GaugeMetricCard(
                title = "WIND SPEED",
                value = windStr,
                subtitle = "Dir: ${weather.windDir} (${weather.windDegree}°)",
                icon = Icons.Outlined.Air,
                progress = windProgress,
                gradientColors = listOf(Color(0xFF818CF8), Color(0xFF4F46E5)),
                theme = theme,
                testTag = "stat_wind",
                modifier = itemWidthModifier
            )

            // 4. Visibility Card with Gauge
            GaugeMetricCard(
                title = "VISIBILITY",
                value = "${String.format("%.1f", visVal)} $visUnit",
                subtitle = visStatus,
                icon = Icons.Outlined.Visibility,
                progress = visProgress,
                gradientColors = listOf(Color(0xFF34D399), Color(0xFF059669)),
                theme = theme,
                testTag = "stat_visibility",
                modifier = itemWidthModifier
            )

            // 5. Air Quality (AQI) Card with Gauge
            GaugeMetricCard(
                title = "AIR QUALITY",
                value = "$aqiLabel (${weather.aqi})",
                valueColor = aqiColor,
                subtitle = "EPA Index $aqiLabel",
                icon = Icons.Outlined.Eco,
                progress = (weather.aqi / 6f).coerceIn(0.15f, 1f),
                gradientColors = listOf(Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444)),
                theme = theme,
                testTag = "stat_aqi",
                modifier = itemWidthModifier
            )

            // 6. Pressure Card with Gauge
            GaugeMetricCard(
                title = "PRESSURE",
                value = "${weather.pressureMb.toInt()} hPa",
                subtitle = pressureStatus,
                icon = Icons.Outlined.Compress,
                progress = pressureProgress,
                gradientColors = listOf(Color(0xFFF472B6), Color(0xFFDB2777)),
                theme = theme,
                testTag = "stat_pressure",
                modifier = itemWidthModifier
            )

            // 7. Feels Like Card with Gauge
            GaugeMetricCard(
                title = "FEELS LIKE",
                value = feelsLikeStr,
                subtitle = if (weather.feelsLikeC > weather.tempC) "Warmer than actual" else "Cooler than actual",
                icon = Icons.Outlined.Thermostat,
                progress = ((weather.feelsLikeC + 10) / 50.0).toFloat().coerceIn(0.1f, 1f),
                gradientColors = listOf(Color(0xFFFBBF24), Color(0xFFEA580C)),
                theme = theme,
                testTag = "stat_feels_like",
                modifier = itemWidthModifier
            )

            // 8. Sunrise Card with Gauge
            GaugeMetricCard(
                title = "SUNRISE",
                value = weather.sunrise,
                subtitle = "Sunset: ${weather.sunset}",
                icon = Icons.Outlined.LightMode,
                progress = 0.45f,
                gradientColors = listOf(Color(0xFFFDE047), Color(0xFFF97316)),
                theme = theme,
                testTag = "stat_sunrise",
                modifier = itemWidthModifier
            )
        }
    }
}

@Composable
fun GaugeMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    progress: Float,
    gradientColors: List<Color>,
    theme: WeatherTheme,
    testTag: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Unspecified
) {
    val actualValueColor = if (valueColor != Color.Unspecified) valueColor else theme.primaryTextColor
    val iconTint = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495)

    val glassColor = if (theme.primaryTextColor == Color.White) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.55f)
    }
    val glassBorderColor = if (theme.primaryTextColor == Color.White) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.35f)
    }

    Surface(
        modifier = modifier.testTag(testTag),
        color = glassColor,
        border = BorderStroke(1.dp, glassBorderColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "$title Icon",
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title.uppercase(),
                        fontSize = 11.sp,
                        color = theme.secondaryTextColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 17.sp,
                color = actualValueColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = theme.secondaryTextColor,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Gauge Meter Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (theme.primaryTextColor == Color.White) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress.coerceIn(0.04f, 1.0f))
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(gradientColors))
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    theme: WeatherTheme,
    testTag: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    GaugeMetricCard(
        title = title,
        value = value,
        subtitle = title,
        icon = icon,
        progress = 0.5f,
        gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
        theme = theme,
        testTag = testTag,
        modifier = modifier,
        valueColor = valueColor
    )
}
