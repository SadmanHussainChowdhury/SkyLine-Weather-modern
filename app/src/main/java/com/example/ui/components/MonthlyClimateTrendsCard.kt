package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.local.MonthlyClimateEntity
import com.example.data.repository.WeatherData
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthlyClimateTrendsCard(
    weather: WeatherData,
    theme: WeatherTheme,
    isCelsius: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as? WeatherApplication
    val repository = remember { app?.container?.weatherRepository }
    val scope = rememberCoroutineScope()

    var climateList by remember { mutableStateOf<List<MonthlyClimateEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Observe DB Room state flow for reactive updating
    val roomFlow = remember(weather.city, repository) {
        repository?.getMonthlyClimateTrendsFlow(weather.city)
    }

    val roomDataState by (roomFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    // Seed/Ensure Room DB has data for current city
    LaunchedEffect(weather.city, repository) {
        if (repository != null) {
            isLoading = true
            val data = repository.ensureHistoricalClimateData(weather.city)
            climateList = data
            isLoading = false
        }
    }

    // Update whenever room flow emits
    LaunchedEffect(roomDataState) {
        if (roomDataState.isNotEmpty()) {
            climateList = roomDataState
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Temp, 1: Precip, 2: Highlights
    val currentMonthIndex = remember { Calendar.getInstance().get(Calendar.MONTH) } // 0..11
    var selectedMonthIndex by remember { mutableIntStateOf(currentMonthIndex) }
    var useMetricUnits by remember(isCelsius) { mutableStateOf(isCelsius) }

    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor
    val orangeTemp = Color(0xFFFF7043)
    val bluePrecip = Color(0xFF29B6F6)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("monthly_climate_trends_card"),
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
                            .background(Color(0xFF0288D1).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BarChart,
                            contentDescription = "Monthly Climate Trends",
                            tint = Color(0xFF0288D1),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Monthly Climate Trends",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "Historical averages for ${weather.city}",
                            fontSize = 12.sp,
                            color = secondaryText
                        )
                    }
                }

                // Unit Toggle Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { useMetricUnits = !useMetricUnits }
                        .testTag("climate_unit_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (useMetricUnits) "°C / mm" else "°F / in",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }
                }
            }

            // Tabs for Temperature / Precipitation / Highlights
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = primaryText,
                divider = { HorizontalDivider(color = cardBorder.copy(alpha = 0.5f)) },
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            height = 3.dp,
                            color = if (selectedTab == 0) orangeTemp else bluePrecip
                        )
                    }
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Thermostat,
                                contentDescription = "Temperature",
                                tint = if (selectedTab == 0) orangeTemp else secondaryText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Temperature", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.WaterDrop,
                                contentDescription = "Precipitation",
                                tint = if (selectedTab == 1) bluePrecip else secondaryText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rainfall", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )

                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.WbSunny,
                                contentDescription = "Highlights",
                                tint = secondaryText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Highlights", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            if (climateList.size >= 12) {
                when (selectedTab) {
                    0 -> TemperatureTrendChart(
                        climateList = climateList,
                        useMetric = useMetricUnits,
                        selectedMonthIndex = selectedMonthIndex,
                        currentMonthIndex = currentMonthIndex,
                        onMonthSelect = { selectedMonthIndex = it },
                        theme = theme
                    )
                    1 -> PrecipitationTrendChart(
                        climateList = climateList,
                        useMetric = useMetricUnits,
                        selectedMonthIndex = selectedMonthIndex,
                        currentMonthIndex = currentMonthIndex,
                        onMonthSelect = { selectedMonthIndex = it },
                        theme = theme
                    )
                    2 -> ClimateHighlightsView(
                        climateList = climateList,
                        useMetric = useMetricUnits,
                        theme = theme
                    )
                }

                // Month Detailed Inspector Card (for Temp & Precip tabs)
                if (selectedTab != 2 && selectedMonthIndex in climateList.indices) {
                    val m = climateList[selectedMonthIndex]
                    val isCurrent = selectedMonthIndex == currentMonthIndex

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) Color(0xFF0F172A) else Color(0xFFF0F4F8),
                        border = BorderStroke(1.dp, cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("month_detail_inspector_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${m.monthName} Historical Climate Summary",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryText
                                    )
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF22C55E).copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "CURRENT MONTH",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF22C55E),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "Month ${m.month} of 12",
                                    fontSize = 11.sp,
                                    color = secondaryText
                                )
                            }

                            HorizontalDivider(color = cardBorder.copy(alpha = 0.5f))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val itemWeight = Modifier.weight(1f)

                                MetricPill(
                                    label = "Avg High / Low",
                                    value = if (useMetricUnits) "${m.avgHighC.toInt()}° / ${m.avgLowC.toInt()}°C" else "${m.avgHighF.toInt()}° / ${m.avgLowF.toInt()}°F",
                                    icon = Icons.Outlined.Thermostat,
                                    color = orangeTemp,
                                    theme = theme,
                                    modifier = itemWeight
                                )

                                MetricPill(
                                    label = "Precipitation",
                                    value = if (useMetricUnits) "${m.precipMm} mm" else "${m.precipInches} in",
                                    icon = Icons.Outlined.WaterDrop,
                                    color = bluePrecip,
                                    theme = theme,
                                    modifier = itemWeight
                                )

                                MetricPill(
                                    label = "Rainy Days",
                                    value = "${m.rainyDays} days",
                                    icon = Icons.Outlined.Grain,
                                    color = Color(0xFFAB47BC),
                                    theme = theme,
                                    modifier = itemWeight
                                )

                                MetricPill(
                                    label = "Sunshine",
                                    value = "${m.sunshineHours} hrs",
                                    icon = Icons.Outlined.WbSunny,
                                    color = Color(0xFFFFB300),
                                    theme = theme,
                                    modifier = itemWeight
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Loading climate trends from Room Database...",
                    fontSize = 13.sp,
                    color = secondaryText,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            // Room Database Connection Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Storage,
                        contentDescription = "Room DB Status",
                        tint = secondaryText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Room Table: monthly_climate_trends (${climateList.size} items stored)",
                        fontSize = 11.sp,
                        color = secondaryText
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            repository?.let { repo ->
                                scope.launch {
                                    repo.ensureHistoricalClimateData(weather.city, forceReSeed = true)
                                }
                            }
                        }
                        .testTag("reseed_climate_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Reseed",
                            tint = Color(0xFF0288D1),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Re-seed DB",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0288D1)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemperatureTrendChart(
    climateList: List<MonthlyClimateEntity>,
    useMetric: Boolean,
    selectedMonthIndex: Int,
    currentMonthIndex: Int,
    onMonthSelect: (Int) -> Unit,
    theme: WeatherTheme
) {
    val highs = climateList.map { if (useMetric) it.avgHighC else it.avgHighF }
    val lows = climateList.map { if (useMetric) it.avgLowC else it.avgLowF }

    val minVal = (lows.minOrNull() ?: 0.0) - 5.0
    val maxVal = (highs.maxOrNull() ?: 30.0) + 5.0
    val range = (maxVal - minVal).coerceAtLeast(1.0)

    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor
    val highColor = Color(0xFFFF7043)
    val lowColor = Color(0xFF26A69A)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Avg High & Low Temperatures", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = secondaryText)
            Text("Tap month to inspect", fontSize = 11.sp, color = secondaryText)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("temperature_climate_chart")
            ) {
                if (climateList.size < 2) return@Canvas
                val w = size.width
                val h = size.height
                val paddingX = 24.dp.toPx()
                val availableW = w - (2 * paddingX)
                val stepX = availableW / (climateList.size - 1)

                val bottomY = h - 28.dp.toPx()
                val topY = 16.dp.toPx()
                val availableH = bottomY - topY

                // Horizontal reference grid lines
                for (i in 0..3) {
                    val gridY = topY + (availableH * i / 3f)
                    drawLine(
                        color = secondaryText.copy(alpha = 0.15f),
                        start = Offset(paddingX, gridY),
                        end = Offset(w - paddingX, gridY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                }

                // Points calculation
                val highPoints = highs.mapIndexed { idx, value ->
                    val x = paddingX + (idx * stepX)
                    val y = bottomY - (((value - minVal) / range) * availableH).toFloat()
                    Offset(x, y)
                }

                val lowPoints = lows.mapIndexed { idx, value ->
                    val x = paddingX + (idx * stepX)
                    val y = bottomY - (((value - minVal) / range) * availableH).toFloat()
                    Offset(x, y)
                }

                // Draw filled band between high and low curves
                val bandPath = Path().apply {
                    moveTo(highPoints.first().x, highPoints.first().y)
                    for (i in 1 until highPoints.size) {
                        lineTo(highPoints[i].x, highPoints[i].y)
                    }
                    for (i in lowPoints.indices.reversed()) {
                        lineTo(lowPoints[i].x, lowPoints[i].y)
                    }
                    close()
                }

                drawPath(
                    path = bandPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(highColor.copy(alpha = 0.25f), lowColor.copy(alpha = 0.15f))
                    )
                )

                // High line
                val highPath = Path().apply {
                    moveTo(highPoints.first().x, highPoints.first().y)
                    for (i in 1 until highPoints.size) {
                        lineTo(highPoints[i].x, highPoints[i].y)
                    }
                }
                drawPath(highPath, color = highColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                // Low line
                val lowPath = Path().apply {
                    moveTo(lowPoints.first().x, lowPoints.first().y)
                    for (i in 1 until lowPoints.size) {
                        lineTo(lowPoints[i].x, lowPoints[i].y)
                    }
                }
                drawPath(lowPath, color = lowColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                // Points & Selection indicator
                highPoints.forEachIndexed { idx, pt ->
                    val isSelected = idx == selectedMonthIndex
                    val isCurrent = idx == currentMonthIndex

                    if (isSelected) {
                        drawLine(
                            color = primaryText.copy(alpha = 0.4f),
                            start = Offset(pt.x, topY),
                            end = Offset(pt.x, bottomY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                        )
                    }

                    // High dot
                    drawCircle(color = highColor, radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(), center = pt)
                    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = pt)

                    // Low dot
                    val lowPt = lowPoints[idx]
                    drawCircle(color = lowColor, radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(), center = lowPt)
                    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = lowPt)
                }
            }
        }

        // Month Selector Bar below chart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            climateList.forEachIndexed { idx, m ->
                val isSelected = idx == selectedMonthIndex
                val isCurrent = idx == currentMonthIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onMonthSelect(idx) }
                        .padding(horizontal = 2.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = m.monthName,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) highColor else if (isCurrent) Color(0xFF22C55E) else secondaryText
                    )

                    if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                    } else if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(highColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrecipitationTrendChart(
    climateList: List<MonthlyClimateEntity>,
    useMetric: Boolean,
    selectedMonthIndex: Int,
    currentMonthIndex: Int,
    onMonthSelect: (Int) -> Unit,
    theme: WeatherTheme
) {
    val precips = climateList.map { if (useMetric) it.precipMm else it.precipInches }
    val maxVal = (precips.maxOrNull() ?: 100.0) * 1.15

    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor
    val precipColor = Color(0xFF29B6F6)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Monthly Average Rainfall Depth", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = secondaryText)
            Text("Unit: ${if (useMetric) "mm" else "inches"}", fontSize = 11.sp, color = secondaryText)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("precipitation_climate_chart")
            ) {
                if (climateList.isEmpty()) return@Canvas
                val w = size.width
                val h = size.height
                val paddingX = 18.dp.toPx()
                val availableW = w - (2 * paddingX)
                val barWidth = (availableW / climateList.size) * 0.65f
                val stepX = availableW / climateList.size

                val bottomY = h - 28.dp.toPx()
                val topY = 16.dp.toPx()
                val availableH = bottomY - topY

                // Draw Bar Charts for 12 Months
                precips.forEachIndexed { idx, valAmount ->
                    val isSelected = idx == selectedMonthIndex
                    val isCurrent = idx == currentMonthIndex

                    val barHeight = ((valAmount / maxVal) * availableH).toFloat().coerceAtLeast(6.dp.toPx())
                    val barLeft = paddingX + (idx * stepX) + ((stepX - barWidth) / 2f)
                    val barTop = bottomY - barHeight

                    val barBrush = Brush.verticalGradient(
                        colors = if (isSelected) listOf(Color(0xFF0288D1), Color(0xFF29B6F6))
                        else if (isCurrent) listOf(Color(0xFF22C55E), Color(0xFF81C784))
                        else listOf(precipColor.copy(alpha = 0.8f), precipColor.copy(alpha = 0.4f))
                    )

                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(barLeft, barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    if (isSelected) {
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.6f),
                            topLeft = Offset(barLeft, barTop),
                            size = Size(barWidth, barHeight),
                            style = Stroke(width = 1.5.dp.toPx()),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }

                // Average line
                val avgVal = precips.average()
                val avgY = bottomY - ((avgVal / maxVal) * availableH).toFloat()
                drawLine(
                    color = secondaryText.copy(alpha = 0.4f),
                    start = Offset(paddingX, avgY),
                    end = Offset(w - paddingX, avgY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                )
            }
        }

        // Month Selector Bar below chart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            climateList.forEachIndexed { idx, m ->
                val isSelected = idx == selectedMonthIndex
                val isCurrent = idx == currentMonthIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onMonthSelect(idx) }
                        .padding(horizontal = 2.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = m.monthName,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) precipColor else if (isCurrent) Color(0xFF22C55E) else secondaryText
                    )

                    if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                    } else if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(precipColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClimateHighlightsView(
    climateList: List<MonthlyClimateEntity>,
    useMetric: Boolean,
    theme: WeatherTheme
) {
    val hottest = climateList.maxByOrNull { if (useMetric) it.avgHighC else it.avgHighF }
    val coldest = climateList.minByOrNull { if (useMetric) it.avgLowC else it.avgLowF }
    val wettest = climateList.maxByOrNull { if (useMetric) it.precipMm else it.precipInches }
    val driest = climateList.minByOrNull { if (useMetric) it.precipMm else it.precipInches }

    val totalAnnualRainfallMm = climateList.sumOf { it.precipMm }
    val totalAnnualRainfallInches = climateList.sumOf { it.precipInches }
    val avgAnnualHighC = climateList.map { it.avgHighC }.average()
    val avgAnnualHighF = climateList.map { it.avgHighF }.average()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Annual Climate Benchmarks & Extremes",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = theme.primaryTextColor
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val mod = Modifier.weight(1f)

            if (hottest != null) {
                HighlightCard(
                    title = "Hottest Month",
                    month = hottest.monthName,
                    metric = if (useMetric) "${hottest.avgHighC.toInt()}°C" else "${hottest.avgHighF.toInt()}°F",
                    subtitle = "Avg High Temperature",
                    color = Color(0xFFFF7043),
                    icon = Icons.Outlined.WbSunny,
                    theme = theme,
                    modifier = mod
                )
            }

            if (coldest != null) {
                HighlightCard(
                    title = "Coldest Month",
                    month = coldest.monthName,
                    metric = if (useMetric) "${coldest.avgLowC.toInt()}°C" else "${coldest.avgLowF.toInt()}°F",
                    subtitle = "Avg Low Temperature",
                    color = Color(0xFF26C6DA),
                    icon = Icons.Outlined.Cloud,
                    theme = theme,
                    modifier = mod
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val mod = Modifier.weight(1f)

            if (wettest != null) {
                HighlightCard(
                    title = "Wettest Month",
                    month = wettest.monthName,
                    metric = if (useMetric) "${wettest.precipMm} mm" else "${wettest.precipInches} in",
                    subtitle = "${wettest.rainyDays} rainy days",
                    color = Color(0xFF29B6F6),
                    icon = Icons.Outlined.WaterDrop,
                    theme = theme,
                    modifier = mod
                )
            }

            HighlightCard(
                title = "Annual Rainfall Total",
                month = "Full Year",
                metric = if (useMetric) "${String.format(Locale.US, "%.0f", totalAnnualRainfallMm)} mm" else "${String.format(Locale.US, "%.1f", totalAnnualRainfallInches)} in",
                subtitle = "Avg High: ${if (useMetric) String.format(Locale.US, "%.1f°C", avgAnnualHighC) else String.format(Locale.US, "%.1f°F", avgAnnualHighF)}",
                color = Color(0xFFAB47BC),
                icon = Icons.Outlined.BarChart,
                theme = theme,
                modifier = mod
            )
        }
    }
}

@Composable
private fun HighlightCard(
    title: String,
    month: String,
    metric: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val isDark = theme.primaryTextColor == Color.White
    val bg = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = theme.secondaryTextColor
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = metric,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = theme.primaryTextColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "($month)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = theme.secondaryTextColor
            )
        }
    }
}

@Composable
private fun MetricPill(
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
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = label, fontSize = 9.5.sp, color = theme.secondaryTextColor)
                Text(text = value, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = theme.primaryTextColor)
            }
        }
    }
}
