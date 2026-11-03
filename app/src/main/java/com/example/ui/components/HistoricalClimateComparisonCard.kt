package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.outlined.HistoryToggleOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.HistoricalClimateCalculator
import com.example.data.repository.HistoricalClimateComparisonData
import com.example.data.repository.WeatherData

@Composable
fun HistoricalClimateComparisonCard(
    weather: WeatherData,
    theme: WeatherTheme,
    isCelsius: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()
    val comparison = remember(weather.city, weather.tempC) {
        HistoricalClimateCalculator.generateComparison(weather)
    }

    var useMetric by remember(isCelsius) { mutableStateOf(isCelsius) }
    var showDetails by remember { mutableStateOf(false) }

    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.90f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor

    val badgeBg = Color(comparison.trendClassification.badgeColorHex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("historical_climate_comparison_card"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Title, Badge & Unit Toggle
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
                            .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HistoryToggleOff,
                            contentDescription = "Historical Climate Comparison",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Historical Climate Comparison",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "${comparison.monthName} ${comparison.year} vs Historical Norms",
                            fontSize = 12.sp,
                            color = secondaryText
                        )
                    }
                }

                // °C / °F Unit Switcher
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptics.performToggleMode()
                            useMetric = !useMetric
                        }
                        .testTag("climate_comparison_unit_toggle")
                ) {
                    Text(
                        text = if (useMetric) "°C / mm" else "°F / in",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Trend Badge Banner & Summary Callout
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = badgeBg.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, badgeBg.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeBg
                            ) {
                                Text(
                                    text = comparison.trendClassification.label.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            val anomalyText = if (useMetric) {
                                val sign = if (comparison.tempAnomalyC >= 0) "+" else ""
                                "$sign${comparison.tempAnomalyC}°C vs 5-Yr Norm"
                            } else {
                                val sign = if (comparison.tempAnomalyF >= 0) "+" else ""
                                "$sign${comparison.tempAnomalyF}°F vs 5-Yr Norm"
                            }
                            Text(
                                text = anomalyText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeBg
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = comparison.climateInsight,
                            fontSize = 12.sp,
                            color = primaryText,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Temperature Comparison Bar Visualizer (This Month vs Last Year vs 5-Yr Baseline)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Average Temperature Comparison (${comparison.monthName})",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = secondaryText
                )

                val tThis = if (useMetric) comparison.thisMonthAvgTempC else comparison.thisMonthAvgTempF
                val tLast = if (useMetric) comparison.lastYearAvgTempC else comparison.lastYearAvgTempF
                val tBase = if (useMetric) comparison.fiveYearAvgTempC else comparison.fiveYearAvgTempF

                val unitLabel = if (useMetric) "°C" else "°F"

                ClimateComparisonBar(
                    label = "This Month (${comparison.year})",
                    valStr = "${tThis.toInt()}$unitLabel",
                    value = tThis,
                    baseValue = tBase,
                    barColor = Color(0xFFFF7043),
                    isHighlight = true,
                    isDark = isDark
                )

                ClimateComparisonBar(
                    label = "Last Year (${comparison.year - 1})",
                    valStr = "${tLast.toInt()}$unitLabel",
                    value = tLast,
                    baseValue = tBase,
                    barColor = Color(0xFF38BDF8),
                    isHighlight = false,
                    isDark = isDark
                )

                ClimateComparisonBar(
                    label = "5-Year Climate Baseline",
                    valStr = "${tBase.toInt()}$unitLabel",
                    value = tBase,
                    baseValue = tBase,
                    barColor = Color(0xFF10B981),
                    isHighlight = false,
                    isDark = isDark
                )
            }

            HorizontalDivider(color = cardBorder.copy(alpha = 0.5f))

            // Rainfall Comparison Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = "Precipitation Comparison",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Rainfall Comparison",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        val precipThis = if (useMetric) "${comparison.thisMonthPrecipMm} mm" else "${comparison.thisMonthPrecipInches} in"
                        val precipLast = if (useMetric) "${comparison.lastYearPrecipMm} mm" else "${comparison.lastYearPrecipInches} in"
                        Text(
                            text = "$precipThis this month vs $precipLast last year",
                            fontSize = 11.sp,
                            color = secondaryText
                        )
                    }
                }

                val precipSign = if (comparison.precipAnomalyPercent >= 0) "+" else ""
                Text(
                    text = "$precipSign${comparison.precipAnomalyPercent}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (comparison.precipAnomalyPercent >= 0) Color(0xFF0284C7) else Color(0xFFF59E0B)
                )
            }

            // Expandable All-Time Records & Deep Context Section
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        haptics.performKeyTap()
                        showDetails = !showDetails
                    }
                    .testTag("expand_historical_records_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.ShowChart,
                            contentDescription = "All-time records",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "All-Time Historical Extremes for ${comparison.monthName}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }
                    Icon(
                        imageVector = if (showDetails) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Toggle details",
                        tint = secondaryText
                    )
                }
            }

            AnimatedVisibility(
                visible = showDetails,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val recordHighStr = if (useMetric) "${comparison.recordHighTempC}°C" else "${Math.round((comparison.recordHighTempC * 1.8 + 32) * 10.0) / 10.0}°F"
                        val recordLowStr = if (useMetric) "${comparison.recordLowTempC}°C" else "${Math.round((comparison.recordLowTempC * 1.8 + 32) * 10.0) / 10.0}°F"

                        RecordBox(
                            label = "All-Time High",
                            value = recordHighStr,
                            year = "Year ${comparison.recordHighYear}",
                            color = Color(0xFFEF4444),
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )

                        RecordBox(
                            label = "All-Time Low",
                            value = recordLowStr,
                            year = "Year ${comparison.recordLowYear}",
                            color = Color(0xFF38BDF8),
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClimateComparisonBar(
    label: String,
    valStr: String,
    value: Double,
    baseValue: Double,
    barColor: Color,
    isHighlight: Boolean,
    isDark: Boolean
) {
    val maxBarVal = (baseValue * 1.4).coerceAtLeast(40.0)
    val fillFraction = ((value / maxBarVal)).toFloat().coerceIn(0.15f, 1.0f)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
                color = if (isHighlight) MaterialTheme.colorScheme.primary else if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
            )
            Text(
                text = valStr,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = barColor
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (isDark) Color(0xFF0F172A) else Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fillFraction)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(barColor.copy(alpha = 0.7f), barColor)
                        )
                    )
            )
        }
    }
}

@Composable
private fun RecordBox(
    label: String,
    value: String,
    year: String,
    color: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = label, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (isDark) Color.White else Color(0xFF0F172A))
            Text(text = year, fontSize = 10.sp, color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
        }
    }
}
