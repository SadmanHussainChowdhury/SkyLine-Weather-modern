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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.HistoricalWeatherTimelineGenerator
import com.example.data.repository.TimelineEventType
import com.example.data.repository.WeatherData
import com.example.data.repository.WeatherTimelineEvent

@Composable
fun DailyWeatherNarrativeTimelineCard(
    weather: WeatherData,
    theme: WeatherTheme,
    isCelsius: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()
    val events = remember(weather.city, weather.tempC) {
        HistoricalWeatherTimelineGenerator.generateDailyNarrative(weather)
    }

    var isExpanded by remember { mutableStateOf(true) }

    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.90f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("daily_weather_narrative_timeline_card"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Header
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
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "Daily Weather Narrative Timeline",
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Today's Weather Narrative",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "Chronological timeline of weather events in ${weather.city}",
                            fontSize = 12.sp,
                            color = secondaryText
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptics.performKeyTap()
                            isExpanded = !isExpanded
                        }
                        .testTag("timeline_expand_toggle")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Toggle Timeline View",
                        tint = primaryText,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    events.forEachIndexed { index, event ->
                        val isLast = index == events.lastIndex
                        TimelineItemRow(
                            event = event,
                            isLast = isLast,
                            isCelsius = isCelsius,
                            isDark = isDark,
                            primaryText = primaryText,
                            secondaryText = secondaryText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineItemRow(
    event: WeatherTimelineEvent,
    isLast: Boolean,
    isCelsius: Boolean,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color
) {
    val haptics = rememberAppHaptics()
    val eventColor = Color(event.eventType.categoryColorHex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { haptics.performKeyTap() },
        verticalAlignment = Alignment.Top
    ) {
        // Time Stamp Label Column (Left)
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .width(68.dp)
                .padding(top = 2.dp, end = 10.dp)
        ) {
            Text(
                text = event.timeLabel,
                fontSize = 11.sp,
                fontWeight = if (event.isCurrentNow) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (event.isCurrentNow) Color(0xFF006495) else secondaryText
            )
            val tempStr = if (isCelsius) "${event.tempC.toInt()}°C" else "${event.tempF.toInt()}°F"
            Text(
                text = tempStr,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = eventColor
            )
        }

        // Vertical Line & Event Node Column (Center)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            // Node Bullet / Emoji Container
            Box(
                modifier = Modifier
                    .size(if (event.isCurrentNow) 28.dp else 22.dp)
                    .clip(CircleShape)
                    .background(
                        if (event.isCurrentNow) eventColor else eventColor.copy(
                            alpha = if (event.isPastEvent) 0.25f else 0.15f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = event.eventType.defaultIconEmoji,
                    fontSize = if (event.isCurrentNow) 13.sp else 10.sp
                )
            }

            // Connecting Vertical Timeline Line
            if (!isLast) {
                Canvas(
                    modifier = Modifier
                        .width(2.dp)
                        .height(52.dp)
                ) {
                    drawLine(
                        color = if (event.isPastEvent) eventColor.copy(alpha = 0.5f) else Color(0xFF94A3B8).copy(alpha = 0.3f),
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 3f,
                        pathEffect = if (event.isPastEvent) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Narrative Content Details (Right)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (event.isCurrentNow) {
                eventColor.copy(alpha = 0.12f)
            } else if (isDark) {
                Color(0xFF0F172A).copy(alpha = 0.6f)
            } else {
                Color(0xFFF8FAFC)
            },
            border = BorderStroke(
                1.dp,
                if (event.isCurrentNow) eventColor.copy(alpha = 0.4f) else Color.Transparent
            ),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    if (event.isCurrentNow) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = eventColor
                        ) {
                            Text(
                                text = "NOW",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = event.description,
                    fontSize = 11.sp,
                    color = secondaryText,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
