package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.DailyForecastItem

@Composable
fun DailyForecastList(
    dailyList: List<DailyForecastItem>,
    useCelsius: Boolean,
    theme: WeatherTheme,
    modifier: Modifier = Modifier,
    currentTemp: Double? = null
) {
    if (dailyList.isEmpty()) return

    // Calculate weekly temperature boundaries for visual range representation
    val globalMin = remember(dailyList, useCelsius) {
        val mins = dailyList.map { if (useCelsius) it.minTempC else it.minTempF }
        mins.minOrNull() ?: 0.0
    }
    val globalMax = remember(dailyList, useCelsius) {
        val maxes = dailyList.map { if (useCelsius) it.maxTempC else it.maxTempF }
        maxes.maxOrNull() ?: 40.0
    }
    val globalRange = remember(globalMin, globalMax) {
        (globalMax - globalMin).coerceAtLeast(1.0)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("daily_forecast_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "7-DAY EXTENDED FORECAST",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = theme.secondaryTextColor,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "Tap day for details",
                fontSize = 11.sp,
                color = theme.secondaryTextColor.copy(alpha = 0.8f)
            )
        }

        val glassColor = if (theme.primaryTextColor == Color.White) {
            Color.White.copy(alpha = 0.08f)
        } else {
            Color.White.copy(alpha = 0.45f)
        }
        val glassBorderColor = if (theme.primaryTextColor == Color.White) {
            Color.White.copy(alpha = 0.15f)
        } else {
            Color.White.copy(alpha = 0.35f)
        }

        Surface(
            color = glassColor,
            border = BorderStroke(1.dp, glassBorderColor),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                dailyList.forEachIndexed { index, dayItem ->
                    DailyForecastRowItem(
                        dayItem = dayItem,
                        useCelsius = useCelsius,
                        theme = theme,
                        globalMin = globalMin,
                        globalMax = globalMax,
                        globalRange = globalRange,
                        isToday = index == 0,
                        currentTemp = if (index == 0) currentTemp else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("daily_forecast_item_$index")
                    )
                }
            }
        }
    }
}

@Composable
fun DailyForecastRowItem(
    dayItem: DailyForecastItem,
    useCelsius: Boolean,
    theme: WeatherTheme,
    globalMin: Double,
    globalMax: Double,
    globalRange: Double,
    isToday: Boolean,
    currentTemp: Double?,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val haptics = rememberAppHaptics()

    val maxTemp = if (useCelsius) dayItem.maxTempC else dayItem.maxTempF
    val minTemp = if (useCelsius) dayItem.minTempC else dayItem.minTempF

    val maxTempStr = "${Math.round(maxTemp)}°"
    val minTempStr = "${Math.round(minTemp)}°"
    val tempUnit = if (useCelsius) "°C" else "°F"

    // Calculate left/right offsets for visual temperature bar
    val leftPct = remember(minTemp, globalMin, globalRange) {
        ((minTemp - globalMin) / globalRange).coerceIn(0.0, 1.0)
    }
    val rightPct = remember(maxTemp, globalMin, globalRange) {
        ((maxTemp - globalMin) / globalRange).coerceIn(0.0, 1.0)
    }

    val isDark = theme.primaryTextColor == Color.White
    val chipBg = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                haptics.performToggleMode()
                isExpanded = !isExpanded
            }
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Day Identifier
            Text(
                text = dayItem.date,
                fontSize = 14.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
                color = theme.primaryTextColor,
                modifier = Modifier.width(76.dp)
            )

            // 2. Dynamic Weather Condition Summary Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(chipBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getWeatherIcon(dayItem.conditionCode),
                    contentDescription = dayItem.conditionText,
                    tint = theme.primaryTextColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 3. Low Temperature Label
            Text(
                text = minTempStr,
                fontSize = 14.sp,
                color = theme.secondaryTextColor,
                textAlign = TextAlign.End,
                modifier = Modifier.width(32.dp)
            )

            // 4. Custom visual range slider timeline
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .height(6.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val radius = height / 2f

                    // Render the background bar representing the full weekly range
                    drawRoundRect(
                        color = theme.primaryTextColor.copy(alpha = 0.12f),
                        size = Size(width, height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
                    )

                    // Render the colored range segment for this specific day
                    val startX = (leftPct * width).toFloat()
                    val endX = (rightPct * width).toFloat()
                    val activeWidth = (endX - startX).coerceAtLeast(height)

                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF60A5FA), // Cool blue for lower bound
                                Color(0xFFFBBF24)  // Warm yellow/orange for higher bound
                            )
                        ),
                        topLeft = Offset(startX, 0f),
                        size = Size(activeWidth, height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
                    )

                    // If this represents "Today" and we have access to current temp, overlay indicator dot
                    if (isToday && currentTemp != null) {
                        val currentPct = ((currentTemp - globalMin) / globalRange).coerceIn(0.0, 1.0)
                        val dotX = (currentPct * width).toFloat()

                        // Outer border
                        drawCircle(
                            color = Color.White,
                            radius = radius + 1.dp.toPx(),
                            center = Offset(dotX, radius)
                        )
                        // Inner colored dot core
                        drawCircle(
                            color = Color(0xFF1E40AF),
                            radius = radius - 1.dp.toPx(),
                            center = Offset(dotX, radius)
                        )
                    }
                }
            }

            // 5. High Temperature Label
            Text(
                text = maxTempStr,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = theme.primaryTextColor,
                textAlign = TextAlign.End,
                modifier = Modifier.width(32.dp)
            )
        }

        // Expanded Detailed Summary Row
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(chipBg)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Condition summary text
                Text(
                    text = dayItem.conditionText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.primaryTextColor
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // UV Index detail
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.WbSunny,
                            contentDescription = "UV Index",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "UV ${dayItem.uv}",
                            fontSize = 12.sp,
                            color = theme.secondaryTextColor
                        )
                    }

                    // Temperature Range detail
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Thermostat,
                            contentDescription = "Temperature Range",
                            tint = theme.secondaryTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "H: $maxTempStr • L: $minTempStr",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryTextColor
                        )
                    }
                }
            }
        }
    }
}

