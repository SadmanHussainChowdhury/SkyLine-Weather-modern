package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.HumidityComfortLevel
import com.example.data.repository.HumidityData
import com.example.data.repository.WeatherData
import kotlin.math.roundToInt

@Composable
fun HumidityGaugeCard(
    weather: WeatherData,
    theme: WeatherTheme,
    isCelsius: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor

    val humidityData = remember(weather.humidity, weather.tempC, weather.tempF) {
        HumidityData.calculate(
            humidity = weather.humidity,
            tempC = weather.tempC,
            tempF = weather.tempF
        )
    }

    val comfortColor = Color(humidityData.comfortLevel.colorHex)
    val moldColor = Color(humidityData.moldRisk.colorHex)

    // Gauge sweep angle animation (from 0 to progress fraction)
    var targetProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(weather.humidity) {
        targetProgress = weather.humidity.coerceIn(0, 100) / 100f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "HumidityGaugeAnimation"
    )

    val dewPointDisplay = if (isCelsius) humidityData.dewPointC else humidityData.dewPointF
    val tempUnit = if (isCelsius) "°C" else "°F"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("humidity_gauge_card"),
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
                            .background(Color(0xFF0288D1).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WaterDrop,
                            contentDescription = "Relative Humidity",
                            tint = Color(0xFF0288D1),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Relative Humidity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "Moisture & Dew Point Analysis",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }

                // Comfort Level Status Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = comfortColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(comfortColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = humidityData.comfortLevel.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = comfortColor
                        )
                    }
                }
            }

            // Main Circular Progress Gauge & Stats Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                    .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular Gauge Box with Canvas
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(130.dp)) {
                        val strokeWidth = 12.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(diameter, diameter)

                        // 240 degree gauge arc (from 150deg to 390deg)
                        val startAngle = 150f
                        val maxSweepAngle = 240f

                        // Background Track Arc
                        drawArc(
                            color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                            startAngle = startAngle,
                            sweepAngle = maxSweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Animated Active Sweep Arc
                        val activeSweep = maxSweepAngle * animatedProgress
                        val gradientBrush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF38BDF8),
                                Color(0xFF0288D1),
                                comfortColor
                            )
                        )

                        drawArc(
                            brush = gradientBrush,
                            startAngle = startAngle,
                            sweepAngle = activeSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Center Percentage Text Display
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${(animatedProgress * 100).roundToInt()}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryText
                            )
                            Text(
                                text = "%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = comfortColor,
                                modifier = Modifier.padding(bottom = 4.dp, start = 1.dp)
                            )
                        }
                        Text(
                            text = "HUMIDITY",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = secondaryText,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Key Atmospheric Metrics Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Dew Point Metric Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Thermostat,
                                contentDescription = "Dew Point",
                                tint = Color(0xFF00B4D8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Dew Point",
                                    fontSize = 10.5.sp,
                                    color = secondaryText
                                )
                                Text(
                                    text = "${dewPointDisplay.roundToInt()}$tempUnit",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryText
                                )
                            }
                        }
                    }

                    // Mold Risk Metric Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Opacity,
                                contentDescription = "Mold Risk",
                                tint = moldColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Indoor Mold Risk",
                                    fontSize = 10.5.sp,
                                    color = secondaryText
                                )
                                Text(
                                    text = humidityData.moldRisk.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = moldColor
                                )
                            }
                        }
                    }
                }
            }

            // Health & Indoor Comfort Recommendations Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = comfortColor.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, comfortColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Status",
                            tint = comfortColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = humidityData.statusSummary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = "Indoor Recommendation",
                            tint = secondaryText,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = humidityData.indoorAdvice,
                            fontSize = 12.sp,
                            color = primaryText,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
