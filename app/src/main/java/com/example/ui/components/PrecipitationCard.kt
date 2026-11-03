package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.HourlyPrecipitationItem
import com.example.data.repository.PrecipitationForecastData

@Composable
fun PrecipitationCard(
    data: PrecipitationForecastData,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.92f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor
    val rainAccentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    var expandedHourIndex by remember { mutableStateOf<Int?>(0) } // Default expand 1st hour

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("precipitation_card"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
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
                            .background(rainAccentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WaterDrop,
                            contentDescription = "Precipitation Probability Icon",
                            tint = rainAccentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "12-Hour Precipitation Risk",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "Rain Chance & Intensity Timeline",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }

                // Peak Badge Tag
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = rainAccentColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Umbrella,
                            contentDescription = "Peak Rain Risk",
                            tint = rainAccentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${data.overallPeakChance}% PEAK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = rainAccentColor
                        )
                    }
                }
            }

            // Summary Stats Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Grain,
                                contentDescription = "Rain Headline",
                                tint = rainAccentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = data.summaryHeadline,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText,
                                modifier = Modifier.testTag("precipitation_headline")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Stat 1: Peak Risk Time
                        Column {
                            Text(
                                text = "PEAK RISK TIME",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = secondaryText
                            )
                            Text(
                                text = data.peakTime,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = rainAccentColor
                            )
                        }

                        // Stat 2: Total Expected Accumulation
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "EST. 12H ACCUMULATION",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = secondaryText
                            )
                            Text(
                                text = "${data.totalRainfallMm} mm",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                        }
                    }
                }
            }

            // 12-Hour Timeline Rows with Animated Progress Bars
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("precipitation_hourly_list")
            ) {
                Text(
                    text = "NEXT 12 HOURS PROBABILITY & INTENSITY",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = secondaryText,
                    letterSpacing = 0.5.sp
                )

                data.hourlyList.forEachIndexed { index, item ->
                    PrecipitationHourRow(
                        item = item,
                        index = index,
                        isSelected = expandedHourIndex == index,
                        theme = theme,
                        onClick = {
                            expandedHourIndex = if (expandedHourIndex == index) null else index
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrecipitationHourRow(
    item: HourlyPrecipitationItem,
    index: Int,
    isSelected: Boolean,
    theme: WeatherTheme,
    onClick: () -> Unit
) {
    val isDark = theme.primaryTextColor == Color.White
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor

    // Animate target float for probability and intensity
    var animationTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationTrigger = true
    }

    val animatedProbability by animateFloatAsState(
        targetValue = if (animationTrigger) (item.chancePercentage / 100f) else 0f,
        animationSpec = tween(durationMillis = 800 + index * 50, easing = FastOutSlowInEasing),
        label = "rain_probability_animation_$index"
    )

    // Progress bar color based on intensity / chance
    val barColor = when {
        item.chancePercentage >= 75 -> Color(0xFF6366F1) // Deep rain purple-blue
        item.chancePercentage >= 45 -> Color(0xFF0284C7) // Sky blue
        item.chancePercentage >= 25 -> Color(0xFF38BDF8) // Light blue
        else -> Color(0xFF94A3B8) // Slate gray
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) barColor.copy(alpha = 0.12f) else (if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, if (isSelected) barColor.copy(alpha = 0.5f) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("precipitation_hour_item_$index")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time & Condition
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.width(110.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(barColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (item.chancePercentage > 50) "🌧️" else if (item.chancePercentage > 20) "🌦️" else "☁️",
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.time,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = item.intensityLabel,
                            fontSize = 10.5.sp,
                            color = secondaryText
                        )
                    }
                }

                // Probability Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = barColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${item.chancePercentage}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = barColor,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("precipitation_chance_badge_$index")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Intensity Progress Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = { animatedProbability },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .testTag("precipitation_progress_bar_$index"),
                    color = barColor,
                    trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "${item.intensityMmPerHour} mm/h",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryText,
                    modifier = Modifier.width(55.dp)
                )
            }
        }
    }
}
