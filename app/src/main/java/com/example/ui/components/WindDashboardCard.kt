package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.WeatherData

@Composable
fun WindDashboardCard(
    weather: WeatherData,
    useCelsius: Boolean,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val windSpeed = if (useCelsius) weather.windKph else weather.windMph
    val windUnit = if (useCelsius) "km/h" else "mph"
    
    // Beaufort scale calculation based on windKph
    val beaufortDesc = getBeaufortDescription(weather.windKph)
    
    // Smoothly animate the compass needle rotation using Compose spring physics
    val animatedDegree by animateFloatAsState(
        targetValue = weather.windDegree.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "wind_degree_anim"
    )

    val actualIconColor = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495)
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
            .testTag("wind_dashboard_card"),
        colors = CardDefaults.cardColors(containerColor = glassColor),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Air,
                    contentDescription = "Wind Dashboard Icon",
                    tint = actualIconColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "WIND & DIRECTION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.secondaryTextColor,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Split Layout: Compass on Left, Metrics on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Part: Beautiful Animated Canvas Compass Dial
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val textMeasurer = rememberTextMeasurer()
                    Canvas(modifier = Modifier.fillMaxSize().testTag("animated_compass_dial")) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f
                        val labelPadding = 12.dp.toPx()

                        // 1. Draw outer thin ring
                        drawCircle(
                            color = theme.secondaryTextColor.copy(alpha = 0.25f),
                            radius = radius - 2.dp.toPx(),
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // 2. Draw minor ticks around the dial
                        val tickLength = 5.dp.toPx()
                        for (angle in 0 until 360 step 30) {
                            if (angle % 90 != 0) { // skip cardinal tick positions for readability
                                val angleRad = Math.toRadians(angle.toDouble())
                                val startX = center.x + Math.cos(angleRad).toFloat() * (radius - 4.dp.toPx())
                                val startY = center.y + Math.sin(angleRad).toFloat() * (radius - 4.dp.toPx())
                                val endX = center.x + Math.cos(angleRad).toFloat() * (radius - 4.dp.toPx() - tickLength)
                                val endY = center.y + Math.sin(angleRad).toFloat() * (radius - 4.dp.toPx() - tickLength)
                                drawLine(
                                    color = theme.secondaryTextColor.copy(alpha = 0.4f),
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }

                        // 3. Draw Cardinal Text Labels ("N", "E", "S", "W")
                        val cardinalStyle = TextStyle(
                            color = theme.primaryTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // N Label
                        val nLayout = textMeasurer.measure(AnnotatedString("N"), cardinalStyle)
                        drawText(
                            textLayoutResult = nLayout,
                            topLeft = Offset(center.x - nLayout.size.width / 2f, labelPadding)
                        )

                        // S Label
                        val sLayout = textMeasurer.measure(AnnotatedString("S"), cardinalStyle)
                        drawText(
                            textLayoutResult = sLayout,
                            topLeft = Offset(center.x - sLayout.size.width / 2f, size.height - labelPadding - sLayout.size.height)
                        )

                        // E Label
                        val eLayout = textMeasurer.measure(AnnotatedString("E"), cardinalStyle)
                        drawText(
                            textLayoutResult = eLayout,
                            topLeft = Offset(size.width - labelPadding - eLayout.size.width, center.y - eLayout.size.height / 2f)
                        )

                        // W Label
                        val wLayout = textMeasurer.measure(AnnotatedString("W"), cardinalStyle)
                        drawText(
                            textLayoutResult = wLayout,
                            topLeft = Offset(labelPadding, center.y - wLayout.size.height / 2f)
                        )

                        // 4. Draw Rotating Compass Needle
                        rotate(degrees = animatedDegree, pivot = center) {
                            val needleWidth = 8.dp.toPx()
                            val needleLength = radius - 16.dp.toPx()

                            // North side pointer (Red)
                            val northPath = Path().apply {
                                moveTo(center.x, center.y - needleLength)
                                lineTo(center.x - needleWidth / 2f, center.y)
                                lineTo(center.x + needleWidth / 2f, center.y)
                                close()
                            }
                            drawPath(
                                path = northPath,
                                color = Color(0xFFEF5350)
                            )

                            // South side pointer (Muted secondary style)
                            val southPath = Path().apply {
                                moveTo(center.x, center.y + needleLength)
                                lineTo(center.x - needleWidth / 2f, center.y)
                                lineTo(center.x + needleWidth / 2f, center.y)
                                close()
                            }
                            drawPath(
                                path = southPath,
                                color = theme.secondaryTextColor.copy(alpha = 0.4f)
                            )

                            // Tiny center pin hub
                            drawCircle(
                                color = theme.primaryTextColor,
                                radius = 3.5.dp.toPx(),
                                center = center
                            )
                        }
                    }
                }

                // Right Part: Text Metrics
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$windSpeed $windUnit",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.primaryTextColor,
                        modifier = Modifier.testTag("wind_speed_text")
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${weather.windDir} (${weather.windDegree}°)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = actualIconColor,
                            modifier = Modifier.testTag("wind_direction_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = beaufortDesc,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.secondaryTextColor
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Wind coming from ${weather.windDir}",
                        fontSize = 11.sp,
                        color = theme.secondaryTextColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

private fun getBeaufortDescription(windKph: Double): String {
    return when {
        windKph < 1.0 -> "Calm"
        windKph < 6.0 -> "Light Air"
        windKph < 12.0 -> "Light Breeze"
        windKph < 20.0 -> "Gentle Breeze"
        windKph < 29.0 -> "Moderate Breeze"
        windKph < 39.0 -> "Fresh Breeze"
        windKph < 50.0 -> "Strong Breeze"
        windKph < 62.0 -> "Near Gale"
        else -> "Gale / High Wind"
    }
}
