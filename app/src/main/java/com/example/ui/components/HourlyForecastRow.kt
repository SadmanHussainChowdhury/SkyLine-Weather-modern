package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.HourlyForecastItem

enum class HourlyViewMode {
    SUMMARY,
    DETAILED_LIST
}

@Composable
fun HourlyForecastRow(
    hourlyList: List<HourlyForecastItem>,
    useCelsius: Boolean,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    if (hourlyList.isEmpty()) return

    val haptics = rememberAppHaptics()
    var viewMode by remember { mutableStateOf(HourlyViewMode.SUMMARY) }

    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor
    val accentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    // Calculate 24-Hour min/max temperatures for range progress bars
    val maxTempC = hourlyList.maxOfOrNull { it.tempC } ?: 20.0
    val minTempC = hourlyList.minOfOrNull { it.tempC } ?: 10.0
    val maxTempF = hourlyList.maxOfOrNull { it.tempF } ?: 68.0
    val minTempF = hourlyList.minOfOrNull { it.tempF } ?: 50.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("hourly_forecast_card"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row with Toggle Button
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
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = "24 Hour Forecast Icon",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "24-Hour Forecast",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = if (viewMode == HourlyViewMode.SUMMARY) "24-Hour Summary View" else "Detailed Hourly List",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }

                // Toggle Control Segment Buttons
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Toggle 1: 24h Summary
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (viewMode == HourlyViewMode.SUMMARY) accentColor else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (viewMode != HourlyViewMode.SUMMARY) {
                                        haptics.performToggleMode()
                                        viewMode = HourlyViewMode.SUMMARY
                                    }
                                }
                                .testTag("toggle_hourly_summary")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.BarChart,
                                    contentDescription = "24-Hour Summary Mode",
                                    tint = if (viewMode == HourlyViewMode.SUMMARY) Color.White else secondaryText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Summary",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (viewMode == HourlyViewMode.SUMMARY) Color.White else secondaryText
                                )
                            }
                        }

                        // Toggle 2: Detailed List
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (viewMode == HourlyViewMode.DETAILED_LIST) accentColor else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (viewMode != HourlyViewMode.DETAILED_LIST) {
                                        haptics.performToggleMode()
                                        viewMode = HourlyViewMode.DETAILED_LIST
                                    }
                                }
                                .testTag("toggle_hourly_detailed_list")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ListAlt,
                                    contentDescription = "Detailed List Mode",
                                    tint = if (viewMode == HourlyViewMode.DETAILED_LIST) Color.White else secondaryText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "List",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (viewMode == HourlyViewMode.DETAILED_LIST) Color.White else secondaryText
                                )
                            }
                        }
                    }
                }
            }

            // Mode 1: 24-Hour Summary View
            AnimatedVisibility(
                visible = viewMode == HourlyViewMode.SUMMARY,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Quick Stats Summary Pill Banner
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 24h Max Temp Stat
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "24H MAX",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = secondaryText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (useCelsius) "${Math.round(maxTempC)}°C" else "${Math.round(maxTempF)}°F",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(1.dp)
                                    .background(cardBorder)
                            )

                            // 24h Min Temp Stat
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "24H MIN",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = secondaryText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (useCelsius) "${Math.round(minTempC)}°C" else "${Math.round(minTempF)}°F",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3B82F6)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(1.dp)
                                    .background(cardBorder)
                            )

                            // Total Hours Stat
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SLOTS",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = secondaryText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${hourlyList.size} Hours",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryText
                                )
                            }
                        }
                    }

                    // Horizontal Scrolling Carousel
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hourly_forecast_row"),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        itemsIndexed(hourlyList) { index, hourItem ->
                            val tempStr = if (useCelsius) "${Math.round(hourItem.tempC)}°" else "${Math.round(hourItem.tempF)}°"
                            val isCurrentHour = index == 0

                            val itemBg = if (isCurrentHour) {
                                accentColor.copy(alpha = 0.2f)
                            } else {
                                if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
                            }

                            val itemBorder = if (isCurrentHour) {
                                BorderStroke(1.5.dp, accentColor)
                            } else {
                                BorderStroke(1.dp, cardBorder)
                            }

                            Surface(
                                color = itemBg,
                                border = itemBorder,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .width(86.dp)
                                    .clickable {
                                        haptics.performKeyTap()
                                    }
                                    .testTag("hourly_forecast_item_$index")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (isCurrentHour) "Now" else hourItem.time,
                                        fontSize = 12.sp,
                                        color = if (isCurrentHour) accentColor else primaryText,
                                        fontWeight = if (isCurrentHour) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.testTag("hourly_time_text_$index")
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    // Weather Condition Icon
                                    Icon(
                                        imageVector = getWeatherIcon(hourItem.conditionCode),
                                        contentDescription = hourItem.conditionText,
                                        tint = if (isCurrentHour) accentColor else (if (hourItem.isDay) Color(0xFFF59E0B) else Color(0xFF818CF8)),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    // Temperature
                                    Text(
                                        text = tempStr,
                                        fontSize = 16.sp,
                                        color = primaryText,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.testTag("hourly_temp_text_$index")
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    
                                    // Condition Short Text
                                    Text(
                                        text = hourItem.conditionText,
                                        fontSize = 10.sp,
                                        color = secondaryText,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Mode 2: Detailed Hour-by-Hour Forecast List View
            AnimatedVisibility(
                visible = viewMode == HourlyViewMode.DETAILED_LIST,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("hourly_detailed_list_container")
                ) {
                    hourlyList.forEachIndexed { index, hourItem ->
                        val tempVal = if (useCelsius) hourItem.tempC else hourItem.tempF
                        val minVal = if (useCelsius) minTempC else minTempF
                        val maxVal = if (useCelsius) maxTempC else maxTempF
                        val tempStr = if (useCelsius) "${Math.round(hourItem.tempC)}°C" else "${Math.round(hourItem.tempF)}°F"

                        // Progress fraction within range
                        val range = (maxVal - minVal).coerceAtLeast(1.0)
                        val progress = ((tempVal - minVal) / range).toFloat().coerceIn(0f, 1f)

                        val isCurrentHour = index == 0

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCurrentHour) accentColor.copy(alpha = 0.15f) else (if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, if (isCurrentHour) accentColor.copy(alpha = 0.5f) else cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Time & Day/Night Icon
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    Icon(
                                        imageVector = if (hourItem.isDay) Icons.Outlined.WbSunny else Icons.Outlined.NightsStay,
                                        contentDescription = if (hourItem.isDay) "Day" else "Night",
                                        tint = if (hourItem.isDay) Color(0xFFF59E0B) else Color(0xFF818CF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isCurrentHour) "Now" else hourItem.time,
                                        fontSize = 13.sp,
                                        fontWeight = if (isCurrentHour) FontWeight.Bold else FontWeight.Medium,
                                        color = primaryText
                                    )
                                }

                                // Condition Icon & Text
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = getWeatherIcon(hourItem.conditionCode),
                                        contentDescription = hourItem.conditionText,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = hourItem.conditionText,
                                        fontSize = 12.sp,
                                        color = secondaryText,
                                        maxLines = 1
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Relative Temperature Bar + Value
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.width(110.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(6.dp)
                                            .clip(CircleShape),
                                        color = if (tempVal > (minVal + maxVal)/2) Color(0xFFF97316) else Color(0xFF38BDF8),
                                        trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = tempStr,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryText
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
