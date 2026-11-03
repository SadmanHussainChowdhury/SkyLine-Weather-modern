package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.SevereCold
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.WeatherData
import kotlin.math.abs
import kotlin.math.roundToInt

enum class ActivityLevel(
    val title: String,
    val tempOffsetC: Double,
    val icon: ImageVector,
    val description: String
) {
    RESTING("Resting / Standing", 0.0, Icons.Default.SelfImprovement, "Normal baseline body heat production"),
    WALKING("Casual Walking", 2.5, Icons.Default.DirectionsWalk, "Light movement increases perceived temp by ~2.5°C"),
    RUNNING("Active Running / Sports", 6.0, Icons.Default.DirectionsRun, "Intense exertion adds ~6.0°C metabolic warmth")
}

data class ThermalComfortData(
    val actualTemp: Double,
    val feelsLikeTemp: Double,
    val delta: Double, // feelsLike - actual
    val statusTitle: String,
    val statusDescription: String,
    val statusColor: Color,
    val baseLayer: String,
    val midLayer: String,
    val outerLayer: String,
    val accessories: String,
    val icon: ImageVector
)

@Composable
fun ThermalComfortCard(
    weather: WeatherData,
    theme: WeatherTheme,
    isCelsius: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()

    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor

    var selectedActivity by remember { mutableStateOf(ActivityLevel.RESTING) }
    var isSimulatorExpanded by remember { mutableStateOf(false) }

    val baseTemp = if (isCelsius) weather.tempC else weather.tempF
    val baseFeelsLike = if (isCelsius) weather.feelsLikeC else weather.feelsLikeF

    var customTemp by remember(baseTemp) { mutableDoubleStateOf(baseTemp) }
    var customFeelsLike by remember(baseFeelsLike) { mutableDoubleStateOf(baseFeelsLike) }

    val activeTemp = if (isSimulatorExpanded) customTemp else baseTemp
    val activeFeelsLikeRaw = if (isSimulatorExpanded) customFeelsLike else baseFeelsLike

    // Apply activity level metabolic offset to feels-like calculation
    val metabolicOffset = if (isCelsius) selectedActivity.tempOffsetC else selectedActivity.tempOffsetC * 1.8
    val activeFeelsLike = activeFeelsLikeRaw + metabolicOffset

    val delta = activeFeelsLike - activeTemp

    val comfortData = calculateThermalComfort(
        actualTemp = activeTemp,
        feelsLikeTemp = activeFeelsLike,
        delta = delta,
        isCelsius = isCelsius
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("thermal_comfort_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Title Row
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
                            .background(comfortData.statusColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Checkroom,
                            contentDescription = "Thermal Comfort Icon",
                            tint = comfortData.statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Thermal Comfort Index",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "Apparent Heat & Clothing Layer Guide",
                            fontSize = 12.sp,
                            color = secondaryText
                        )
                    }
                }

                // Delta Badge Tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = comfortData.statusColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, comfortData.statusColor.copy(alpha = 0.35f))
                ) {
                    val deltaString = if (delta >= 0) {
                        "+${String.format("%.1f", delta)}°${if (isCelsius) "C" else "F"}"
                    } else {
                        "${String.format("%.1f", delta)}°${if (isCelsius) "C" else "F"}"
                    }
                    Text(
                        text = "Offset: $deltaString",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = comfortData.statusColor,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("thermal_delta_badge")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Temperature Comparison Hero Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ambient Temperature Metric
                        Column {
                            Text(
                                text = "Ambient Temp",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${String.format("%.1f", activeTemp)}°${if (isCelsius) "C" else "F"}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText,
                                modifier = Modifier.testTag("ambient_temp_text")
                            )
                        }

                        // Status Icon Indicator
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(comfortData.statusColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = comfortData.icon,
                                contentDescription = null,
                                tint = comfortData.statusColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Feels Like Temperature Metric
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Perceived 'Feels Like'",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${String.format("%.1f", activeFeelsLike)}°${if (isCelsius) "C" else "F"}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = comfortData.statusColor,
                                modifier = Modifier.testTag("feels_like_temp_text")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Visual Thermal Comparison Gradient Meter Bar
                    val progressFraction = remember(delta) {
                        // Clamp delta between -10 and +10 for visual meter bar
                        val clamped = delta.coerceIn(-10.0, 10.0)
                        ((clamped + 10.0) / 20.0).toFloat()
                    }

                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF38BDF8), // Cold Wind Chill
                                            Color(0xFF2DD4BF), // Mild Cool
                                            Color(0xFF34D399), // Neutral Balanced
                                            Color(0xFFFBBF24), // Warm Humidity
                                            Color(0xFFF87171)  // High Heat Trap
                                        )
                                    )
                                )
                        ) {
                            // Indicator needle thumb
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.CenterStart)
                                    .padding(start = (progressFraction * 280).dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, comfortData.statusColor, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Wind Chill Cold", fontSize = 10.sp, color = Color(0xFF38BDF8))
                            Text("Neutral Match", fontSize = 10.sp, color = Color(0xFF34D399))
                            Text("Humid Heat Trap", fontSize = 10.sp, color = Color(0xFFF87171))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = comfortData.statusTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = comfortData.statusColor
                    )
                    Text(
                        text = comfortData.statusDescription,
                        fontSize = 12.sp,
                        color = secondaryText,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activity Level Metabolic Heat Switcher
            Text(
                text = "Metabolic Heat Activity Filter",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = secondaryText,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActivityLevel.values().forEach { activity ->
                    val isSelected = selectedActivity == activity
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                haptics.performKeyTap()
                                selectedActivity = activity
                            }
                            .testTag("activity_level_${activity.name.lowercase()}"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) comfortData.statusColor.copy(alpha = 0.2f) else (if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) comfortData.statusColor else (if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = activity.icon,
                                contentDescription = activity.title,
                                tint = if (isSelected) comfortData.statusColor else secondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activity.title.split(" ").first(),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) primaryText else secondaryText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Recommended Clothing Layers Section
            Text(
                text = "SUGGESTED APPAREL STRATEGY",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = comfortData.statusColor
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ClothingLayerRow(
                    label = "Base Layer",
                    detail = comfortData.baseLayer,
                    icon = Icons.Outlined.Checkroom,
                    isDark = isDark,
                    primaryText = primaryText,
                    testTag = "layer_base"
                )

                ClothingLayerRow(
                    label = "Mid Layer",
                    detail = comfortData.midLayer,
                    icon = Icons.Outlined.Thermostat,
                    isDark = isDark,
                    primaryText = primaryText,
                    testTag = "layer_mid"
                )

                ClothingLayerRow(
                    label = "Outer Shell",
                    detail = comfortData.outerLayer,
                    icon = Icons.Outlined.Air,
                    isDark = isDark,
                    primaryText = primaryText,
                    testTag = "layer_outer"
                )

                ClothingLayerRow(
                    label = "Accessories",
                    detail = comfortData.accessories,
                    icon = Icons.Outlined.WbSunny,
                    isDark = isDark,
                    primaryText = primaryText,
                    testTag = "layer_accessories"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = cardBorder.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(12.dp))

            // Expandable Custom Thermal Simulator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        haptics.performKeyTap()
                        isSimulatorExpanded = !isSimulatorExpanded
                    }
                    .padding(vertical = 4.dp)
                    .testTag("thermal_simulator_toggle"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = comfortData.statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Interactive Thermal Comfort Simulator",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                }

                Icon(
                    imageVector = if (isSimulatorExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = "Expand simulator",
                    tint = secondaryText
                )
            }

            AnimatedVisibility(
                visible = isSimulatorExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Adjust Temperature & Feels-Like Sliders:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Ambient Temp: ${String.format("%.1f", customTemp)}°${if (isCelsius) "C" else "F"}",
                                fontSize = 12.sp,
                                color = secondaryText
                            )
                            Slider(
                                value = customTemp.toFloat(),
                                onValueChange = { customTemp = it.toDouble() },
                                valueRange = if (isCelsius) -15f..45f else 5f..115f,
                                colors = SliderDefaults.colors(thumbColor = comfortData.statusColor, activeTrackColor = comfortData.statusColor),
                                modifier = Modifier.testTag("simulator_temp_slider")
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Perceived 'Feels Like': ${String.format("%.1f", customFeelsLike)}°${if (isCelsius) "C" else "F"}",
                                fontSize = 12.sp,
                                color = secondaryText
                            )
                            Slider(
                                value = customFeelsLike.toFloat(),
                                onValueChange = { customFeelsLike = it.toDouble() },
                                valueRange = if (isCelsius) -20f..50f else -4f..122f,
                                colors = SliderDefaults.colors(thumbColor = comfortData.statusColor, activeTrackColor = comfortData.statusColor),
                                modifier = Modifier.testTag("simulator_feels_like_slider")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = comfortData.statusColor.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            haptics.performKeyTap()
                                            customTemp = baseTemp
                                            customFeelsLike = baseFeelsLike
                                        }
                                ) {
                                    Text(
                                        text = "Reset to Live Weather",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = comfortData.statusColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClothingLayerRow(
    label: String,
    detail: String,
    icon: ImageVector,
    isDark: Boolean,
    primaryText: Color,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.6f) else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryText,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = detail,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryText
                )
            }
        }
    }
}

private fun calculateThermalComfort(
    actualTemp: Double,
    feelsLikeTemp: Double,
    delta: Double,
    isCelsius: Boolean
): ThermalComfortData {
    // Convert thresholds if in Fahrenheit
    val deltaC = if (isCelsius) delta else delta / 1.8
    val tempC = if (isCelsius) actualTemp else (actualTemp - 32) * 5 / 9

    return when {
        // High Wind Chill Drift (Feels much colder)
        deltaC < -3.0 -> ThermalComfortData(
            actualTemp = actualTemp,
            feelsLikeTemp = feelsLikeTemp,
            delta = delta,
            statusTitle = "High Wind Chill & Cold Air Drift",
            statusDescription = "Significant wind velocity or moisture makes it feel colder than ambient air. Ensure wind protection.",
            statusColor = Color(0xFF38BDF8), // Cyan
            baseLayer = "Long-sleeve thermal top & leggings",
            midLayer = "Insulating fleece pullover or sweater",
            outerLayer = "Windproof shell jacket or heavy coat",
            accessories = "Beanie cap, scarf & wind-resistant gloves",
            icon = Icons.Outlined.SevereCold
        )

        // Moderate Wind Chill Drift
        deltaC <= -1.0 -> ThermalComfortData(
            actualTemp = actualTemp,
            feelsLikeTemp = feelsLikeTemp,
            delta = delta,
            statusTitle = "Mild Wind Chill Effect",
            statusDescription = "Cool breezes reduce skin surface heat slightly below the thermometer reading.",
            statusColor = Color(0xFF2DD4BF), // Teal
            baseLayer = "Comfortable cotton t-shirt or long sleeve",
            midLayer = "Light hoodie or cardigan",
            outerLayer = "Light windbreaker (optional for open areas)",
            accessories = "Light scarf or covered footwear",
            icon = Icons.Outlined.Air
        )

        // Optimal Neutral Balance
        deltaC in -0.9..0.9 -> ThermalComfortData(
            actualTemp = actualTemp,
            feelsLikeTemp = feelsLikeTemp,
            delta = delta,
            statusTitle = "Optimal Thermal Neutrality",
            statusDescription = "Perceived temperature closely matches thermometer reading with minimal wind or humidity drift.",
            statusColor = Color(0xFF34D399), // Emerald Green
            baseLayer = if (tempC > 20) "Breathable short-sleeve t-shirt" else "Standard comfortable cotton shirt",
            midLayer = if (tempC < 18) "Light sweater or denim jacket" else "None required",
            outerLayer = if (tempC < 14) "Light casual jacket" else "None needed",
            accessories = if (tempC > 22) "Sunglasses & UV hat" else "Standard accessories",
            icon = Icons.Outlined.Thermostat
        )

        // Slight Humidity Warmth
        deltaC <= 3.0 -> ThermalComfortData(
            actualTemp = actualTemp,
            feelsLikeTemp = feelsLikeTemp,
            delta = delta,
            statusTitle = "Mild Humidity & Solar Warmth",
            statusDescription = "Atmospheric moisture reduces natural sweat cooling, making it feel slightly warmer.",
            statusColor = Color(0xFFFBBF24), // Amber
            baseLayer = "Moisture-wicking, breathable loose top",
            midLayer = "Not recommended (stay ventilated)",
            outerLayer = "Light breathable layer (or none)",
            accessories = "Sunglasses, cap & water bottle",
            icon = Icons.Outlined.WaterDrop
        )

        // Heavy Heat & Humidity Trap
        else -> ThermalComfortData(
            actualTemp = actualTemp,
            feelsLikeTemp = feelsLikeTemp,
            delta = delta,
            statusTitle = "High Apparent Heat & Humidity Trap",
            statusDescription = "Heavy air humidity hinders sweat evaporation. Apparent heat index feels elevated.",
            statusColor = Color(0xFFF87171), // Coral Red
            baseLayer = "Ultra-lightweight linen or technical mesh top",
            midLayer = "None (avoid heavy layers)",
            outerLayer = "None (UV sun-protection sleeve if outdoors)",
            accessories = "UV-blocking sunglasses, wide hat & cold hydration",
            icon = Icons.Outlined.WbSunny
        )
    }
}
