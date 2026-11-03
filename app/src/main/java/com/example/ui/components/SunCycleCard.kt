package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.WeatherData
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SunCycleCard(
    weather: WeatherData,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val sunriseStr = weather.sunrise
    val sunsetStr = weather.sunset

    // Parse sunrise/sunset times to minutes of the day
    val sunriseMin = remember(sunriseStr) { parseTimeToMinutes(sunriseStr) }
    val sunsetMin = remember(sunsetStr) { parseTimeToMinutes(sunsetStr) }

    // Get current time of day in minutes
    val calendar = Calendar.getInstance()
    val currentMin = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

    val isDaylight = currentMin in sunriseMin..sunsetMin
    val totalDaylightMinutes = sunsetMin - sunriseMin

    // Calculate daylight progress (0.0f at sunrise, 1.0f at sunset)
    val daylightProgressRaw = if (totalDaylightMinutes > 0) {
        ((currentMin - sunriseMin).toFloat() / totalDaylightMinutes).coerceIn(0f, 1f)
    } else {
        0.5f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = daylightProgressRaw,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "sun_progress_anim"
    )

    // Formulate relative time description strings
    val relativeTimeDesc = remember(currentMin, sunriseMin, sunsetMin, sunriseStr, sunsetStr) {
        getRelativeTimeDescription(currentMin, sunriseMin, sunsetMin, sunriseStr, sunsetStr)
    }

    val daylightLengthDesc = remember(sunriseMin, sunsetMin) {
        val diff = sunsetMin - sunriseMin
        if (diff > 0) {
            val hrs = diff / 60
            val mins = diff % 60
            "$hrs hrs $mins mins of daylight today"
        } else {
            "Daylight cycle is active"
        }
    }

    // Color/Styling rules matching glassmorphic design
    val actualIconColor = if (theme.primaryTextColor == Color.White) Color(0xFFFBBF24) else Color(0xFFD97706)
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("sun_cycle_card"),
        colors = CardDefaults.cardColors(containerColor = glassColor),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.WbTwilight,
                    contentDescription = "Sunrise and Sunset Icon",
                    tint = actualIconColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "SUNRISE & SUNSET CYCLE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.secondaryTextColor,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas visualization: Day/Night curve timeline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("sun_cycle_canvas")
                ) {
                    val width = size.width
                    val height = size.height
                    
                    // Horizon line sits near the bottom
                    val horizonY = height - 20.dp.toPx()

                    // Draw the subtle dashed/dotted horizon line
                    drawLine(
                        color = theme.secondaryTextColor.copy(alpha = 0.3f),
                        start = Offset(0f, horizonY),
                        end = Offset(width, horizonY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    // Draw sunrise and sunset dots on the horizon
                    val startX = 24.dp.toPx()
                    val endX = width - 24.dp.toPx()
                    val arcWidth = endX - startX
                    val arcHeight = height - 44.dp.toPx() // Height of the sun path arc

                    // Center of the arc's horizontal span
                    val arcCenterX = startX + arcWidth / 2f

                    // Draw the Sun Path Arc (Semi-ellipse)
                    val arcPath = Path().apply {
                        moveTo(startX, horizonY)
                        // Cubic curve representing the solar trajectory arc
                        cubicTo(
                            startX + arcWidth * 0.25f, horizonY - arcHeight,
                            startX + arcWidth * 0.75f, horizonY - arcHeight,
                            endX, horizonY
                        )
                    }

                    // Render background daylight gradient glow below the arc
                    val daylightGlowBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFD166).copy(alpha = 0.15f),
                            Color(0xFFFF8C00).copy(alpha = 0.02f),
                            Color.Transparent
                        ),
                        startY = horizonY - arcHeight,
                        endY = horizonY
                    )

                    // Draw filled glowing area under daylight path
                    val filledPath = Path().apply {
                        moveTo(startX, horizonY)
                        cubicTo(
                            startX + arcWidth * 0.25f, horizonY - arcHeight,
                            startX + arcWidth * 0.75f, horizonY - arcHeight,
                            endX, horizonY
                        )
                        lineTo(endX, horizonY)
                        lineTo(startX, horizonY)
                        close()
                    }
                    drawPath(path = filledPath, brush = daylightGlowBrush)

                    // Draw the solar trajectory line (dashed daylight path)
                    drawPath(
                        path = arcPath,
                        color = theme.secondaryTextColor.copy(alpha = 0.4f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                        )
                    )

                    // Draw sunrise / sunset indicator points
                    drawCircle(
                        color = Color(0xFFFF8C00),
                        radius = 4.dp.toPx(),
                        center = Offset(startX, horizonY)
                    )
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 4.dp.toPx(),
                        center = Offset(endX, horizonY)
                    )

                    // Calculate actual sun coordinates on the bezier curve path
                    // Cubic Bézier calculation for a parametric curve from t = 0 to 1
                    val t = animatedProgress
                    val p0 = Offset(startX, horizonY)
                    val p1 = Offset(startX + arcWidth * 0.25f, horizonY - arcHeight)
                    val p2 = Offset(startX + arcWidth * 0.75f, horizonY - arcHeight)
                    val p3 = Offset(endX, horizonY)

                    // B(t) = (1-t)^3 * p0 + 3(1-t)^2 * t * p1 + 3(1-t) * t^2 * p2 + t^3 * p3
                    val oneMinusT = 1f - t
                    val sunX = (oneMinusT * oneMinusT * oneMinusT * p0.x) +
                            (3f * oneMinusT * oneMinusT * t * p1.x) +
                            (3f * oneMinusT * t * t * p2.x) +
                            (t * t * t * p3.x)

                    val sunY = (oneMinusT * oneMinusT * oneMinusT * p0.y) +
                            (3f * oneMinusT * oneMinusT * t * p1.y) +
                            (3f * oneMinusT * t * t * p2.y) +
                            (t * t * t * p3.y)

                    // Draw atmospheric sun glow
                    val sunGlowRadius = 16.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD166).copy(alpha = 0.5f),
                                Color(0xFFFFD166).copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            center = Offset(sunX, sunY),
                            radius = sunGlowRadius * 2
                        ),
                        radius = sunGlowRadius * 2,
                        center = Offset(sunX, sunY)
                    )

                    // Draw sun body (bright yellow-amber sphere)
                    drawCircle(
                        color = Color(0xFFFBBF24),
                        radius = 8.dp.toPx(),
                        center = Offset(sunX, sunY)
                    )
                    drawCircle(
                        color = Color(0xFFFFFFFF),
                        radius = 4.dp.toPx(),
                        center = Offset(sunX, sunY)
                    )

                    // Draw a subtle projection line from the sun down to the horizon
                    if (isDaylight) {
                        drawLine(
                            color = Color(0xFFFFD166).copy(alpha = 0.4f),
                            start = Offset(sunX, sunY),
                            end = Offset(sunX, horizonY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Timings labels aligned with the visual anchors of the timeline
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sunrise details (Left)
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "SUNRISE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.secondaryTextColor.copy(alpha = 0.6f),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = sunriseStr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.primaryTextColor,
                        modifier = Modifier.testTag("sunrise_time_text")
                    )
                }

                // Center Dynamic Stats Info (Daylight Length)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Day length icon",
                        tint = theme.secondaryTextColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = daylightLengthDesc,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.secondaryTextColor
                    )
                }

                // Sunset details (Right)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SUNSET",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.secondaryTextColor.copy(alpha = 0.6f),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = sunsetStr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.primaryTextColor,
                        modifier = Modifier.testTag("sunset_time_text")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Beautiful footer banner summarizing relative countdown or light cycle event
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = theme.primaryTextColor.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = theme.primaryTextColor.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LightMode,
                        contentDescription = "Solar status banner icon",
                        tint = actualIconColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = relativeTimeDesc,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.primaryTextColor,
                        modifier = Modifier.testTag("sun_relative_desc_text")
                    )
                }
            }
        }
    }
}

private fun parseTimeToMinutes(timeStr: String): Int {
    try {
        val parts = timeStr.trim().split(":")
        if (parts.size >= 2) {
            var hour = parts[0].toInt()
            val minParts = parts[1].split(" ")
            val minute = minParts[0].toInt()
            val amPm = if (minParts.size > 1) minParts[1].uppercase() else ""
            
            if (amPm == "PM" && hour < 12) {
                hour += 12
            } else if (amPm == "AM" && hour == 12) {
                hour = 0
            }
            return hour * 60 + minute
        }
    } catch (e: Exception) {
        // Fallback
    }
    return 360 // default 6 AM
}

private fun getRelativeTimeDescription(
    currentMin: Int,
    sunriseMin: Int,
    sunsetMin: Int,
    sunriseStr: String,
    sunsetStr: String
): String {
    return when {
        currentMin < sunriseMin -> {
            val remaining = sunriseMin - currentMin
            val hrs = remaining / 60
            val mins = remaining % 60
            if (hrs > 0) "Sunrise in $hrs hr $mins min ($sunriseStr)" else "Sunrise in $mins min ($sunriseStr)"
        }
        currentMin in sunriseMin..sunsetMin -> {
            val remaining = sunsetMin - currentMin
            val hrs = remaining / 60
            val mins = remaining % 60
            if (hrs > 0) "Sunset in $hrs hr $mins min ($sunsetStr)" else "Sunset in $mins min ($sunsetStr)"
        }
        else -> {
            // Night after sunset
            val remainingToMidnight = 1440 - currentMin
            val totalRemaining = remainingToMidnight + sunriseMin
            val hrs = totalRemaining / 60
            val mins = totalRemaining % 60
            if (hrs > 0) "Sunrise in $hrs hr $mins min ($sunriseStr)" else "Sunrise in $mins min ($sunriseStr)"
        }
    }
}
