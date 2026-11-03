package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.HourlyForecastItem
import com.example.data.repository.WeatherData
import kotlin.math.roundToInt

@Composable
fun TemperatureTrendGraphCard(
    weather: WeatherData,
    theme: WeatherTheme,
    isCelsius: Boolean = true,
    modifier: Modifier = Modifier
) {
    val hourlyData = weather.hourlyForecast
    if (hourlyData.isEmpty()) return

    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor

    // Temperatures calculation
    val temps = hourlyData.map { if (isCelsius) it.tempC else it.tempF }
    val maxTemp = temps.maxOrNull() ?: 25.0
    val minTemp = temps.minOrNull() ?: 15.0
    val tempRange = (maxTemp - minTemp).coerceAtLeast(3.0)

    val currentTemp = temps.firstOrNull() ?: 20.0
    val peakIndex = temps.indexOfFirst { it == maxTemp }.coerceAtLeast(0)
    val peakHour = hourlyData.getOrNull(peakIndex)?.time ?: "3 PM"

    var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedItem = hourlyData.getOrNull(selectedIndex) ?: hourlyData.first()
    val selectedTemp = if (isCelsius) selectedItem.tempC else selectedItem.tempF

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("temperature_trend_graph_card"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
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
                            .background(Color(0xFF00B4D8).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShowChart,
                            contentDescription = "Temperature Trend",
                            tint = Color(0xFF00B4D8),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "24-Hour Temperature Trend",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "Interactive weather shift visualization",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF00B4D8).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "RECHARTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00B4D8),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Temperature Peak & Range Summary Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TrendingUp,
                            contentDescription = "Peak",
                            tint = Color(0xFFFF7043),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("High Peak", fontSize = 10.sp, color = secondaryText)
                            Text(
                                "${maxTemp.roundToInt()}° at $peakHour",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TrendingDown,
                            contentDescription = "Low",
                            tint = Color(0xFF29B6F6),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Low Trough", fontSize = 10.sp, color = secondaryText)
                            Text(
                                "${minTemp.roundToInt()}° overnight",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                        }
                    }
                }
            }

            // Interactive Graph Canvas Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                    .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp)
                    .testTag("recharts_temperature_graph_canvas")
            ) {
                RechartsTemperatureCanvas(
                    hourlyList = hourlyData,
                    isCelsius = isCelsius,
                    selectedIndex = selectedIndex,
                    onSelectIndex = { selectedIndex = it },
                    isDark = isDark
                )
            }

            // Recharts Interactive Floating Tooltip Bar
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recharts_active_tooltip_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = getWeatherIcon(selectedItem.conditionCode),
                            contentDescription = selectedItem.conditionText,
                            tint = Color(0xFF0288D1),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Selected Hour: ${if (selectedIndex == 0) "Now (${selectedItem.time})" else selectedItem.time}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Text(
                                text = selectedItem.conditionText,
                                fontSize = 11.sp,
                                color = secondaryText
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${selectedTemp.roundToInt()}°",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0288D1)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.WaterDrop,
                                    contentDescription = "Rain",
                                    tint = Color(0xFF0288D1),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = selectedItem.conditionText,
                                    fontSize = 10.5.sp,
                                    color = secondaryText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RechartsTemperatureCanvas(
    hourlyList: List<HourlyForecastItem>,
    isCelsius: Boolean,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    isDark: Boolean
) {
    val temps = hourlyList.map { if (isCelsius) it.tempC else it.tempF }
    val maxTemp = (temps.maxOrNull() ?: 30.0) + 2.0
    val minTemp = (temps.minOrNull() ?: 10.0) - 2.0
    val range = (maxTemp - minTemp).coerceAtLeast(4.0)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(hourlyList.size) {
                detectTapGestures { offset ->
                    val stepX = size.width / (hourlyList.size - 1).coerceAtLeast(1)
                    val index = (offset.x / stepX).roundToInt().coerceIn(0, hourlyList.size - 1)
                    onSelectIndex(index)
                }
            }
            .pointerInput(hourlyList.size) {
                detectDragGestures { change, _ ->
                    val stepX = size.width / (hourlyList.size - 1).coerceAtLeast(1)
                    val index = (change.position.x / stepX).roundToInt().coerceIn(0, hourlyList.size - 1)
                    onSelectIndex(index)
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val paddingBottom = 24.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val chartHeight = h - paddingBottom - paddingTop

        val points = mutableListOf<Offset>()
        val stepX = w / (hourlyList.size - 1).coerceAtLeast(1)

        // Calculate (x, y) coordinates for all hourly items
        hourlyList.forEachIndexed { i, item ->
            val temp = if (isCelsius) item.tempC else item.tempF
            val x = i * stepX
            val y = paddingTop + chartHeight - (((temp - minTemp) / range) * chartHeight).toFloat()
            points.add(Offset(x, y))
        }

        // Draw horizontal grid guide lines (3 lines)
        for (grid in 0..2) {
            val gridY = paddingTop + (chartHeight * (grid / 2f))
            drawLine(
                color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                start = Offset(0f, gridY),
                end = Offset(w, gridY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            )
        }

        // Create Smooth Cubic Bezier Path
        val strokePath = Path()
        val fillPath = Path()

        if (points.isNotEmpty()) {
            strokePath.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, h - paddingBottom)
            fillPath.lineTo(points[0].x, points[0].y)

            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)

                strokePath.cubicTo(
                    controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    p2.x, p2.y
                )

                fillPath.cubicTo(
                    controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    p2.x, p2.y
                )
            }

            fillPath.lineTo(points.last().x, h - paddingBottom)
            fillPath.close()

            // Gradient Area Fill under the line (Recharts Gradient)
            val fillGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0288D1).copy(alpha = 0.45f),
                    Color(0xFF00B4D8).copy(alpha = 0.20f),
                    Color.Transparent
                ),
                startY = paddingTop,
                endY = h - paddingBottom
            )
            drawPath(path = fillPath, brush = fillGradient)

            // Smooth Trend Line
            val lineGradient = Brush.horizontalGradient(
                colors = listOf(Color(0xFF0288D1), Color(0xFF00B4D8), Color(0xFF38BDF8))
            )
            drawPath(
                path = strokePath,
                brush = lineGradient,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Data Points
            points.forEachIndexed { idx, pt ->
                val isSelected = idx == selectedIndex
                if (isSelected) {
                    // Vertical Scrubber Line
                    drawLine(
                        color = Color(0xFF0288D1).copy(alpha = 0.7f),
                        start = Offset(pt.x, paddingTop),
                        end = Offset(pt.x, h - paddingBottom),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )

                    // Glow circle for active point
                    drawCircle(
                        color = Color(0xFF0288D1).copy(alpha = 0.3f),
                        radius = 10.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color(0xFF0288D1),
                        radius = 6.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = pt
                    )
                } else if (idx % 3 == 0) {
                    drawCircle(
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0288D1),
                        radius = 3.5.dp.toPx(),
                        center = pt
                    )
                }
            }
        }
    }
}
