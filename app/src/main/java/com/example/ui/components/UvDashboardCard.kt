package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.data.repository.UvData
import com.example.data.repository.WeatherData
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun UvDashboardCard(
    weather: WeatherData,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val uvData = remember(weather.uv) {
        UvData.calculate(weather.uv)
    }

    val animatedUv by animateFloatAsState(
        targetValue = uvData.uvIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "uv_index_anim"
    )

    val riskColor = Color(uvData.riskLevel.colorHex)
    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("uv_dashboard_card"),
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
                            .background(riskColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WbSunny,
                            contentDescription = "UV Index Icon",
                            tint = riskColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "UV Index & Sun Exposure",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "Skin Protection & Burn Risk",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }

                // Risk Level Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = riskColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(riskColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uvData.riskLevel.category.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = riskColor,
                            modifier = Modifier.testTag("uv_category_text")
                        )
                    }
                }
            }

            // Gauge + Exposure Metrics Layout Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Semi-circular Color Spectrum Gauge
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("animated_uv_gauge")
                    ) {
                        val strokeWidth = 10.dp.toPx()
                        val sizeOffset = strokeWidth / 2f
                        val width = size.width - strokeWidth

                        val spectrumBrush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF10B981), // Low (Green)
                                Color(0xFFF59E0B), // Moderate (Yellow)
                                Color(0xFFF97316), // High (Orange)
                                Color(0xFFEF4444), // Very High (Red)
                                Color(0xFFA855F7)  // Extreme (Purple)
                            ),
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2)
                        )

                        // Draw background track
                        drawArc(
                            color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(sizeOffset, sizeOffset),
                            size = Size(width, width),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Draw colored spectrum bar arc
                        drawArc(
                            brush = spectrumBrush,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(sizeOffset, sizeOffset),
                            size = Size(width, width),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Calculate cursor position along the arc (UV 0 to 12)
                        val clampedUv = animatedUv.coerceIn(0f, 12f)
                        val ratio = clampedUv / 12f
                        val angleDeg = 180f + (ratio * 180f)
                        val angleRad = Math.toRadians(angleDeg.toDouble())

                        val arcRadius = width / 2f
                        val centerX = size.width / 2f
                        val centerY = size.width / 2f + sizeOffset

                        val indicatorX = centerX + cos(angleRad).toFloat() * arcRadius
                        val indicatorY = centerY + sin(angleRad).toFloat() * arcRadius

                        // Draw cursor halo & filled dot
                        drawCircle(
                            color = Color.White,
                            radius = 9.dp.toPx(),
                            center = Offset(indicatorX, indicatorY)
                        )

                        drawCircle(
                            color = riskColor,
                            radius = 6.dp.toPx(),
                            center = Offset(indicatorX, indicatorY)
                        )
                    }

                    // Numeric value inside gauge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 28.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", uvData.uvIndex),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryText,
                            modifier = Modifier.testTag("uv_index_text")
                        )
                        Text(
                            text = "UV INDEX",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = secondaryText,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Exposure Metrics Pills Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Recommended SPF Pill
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
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = "Recommended SPF",
                                tint = riskColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Sunscreen Filter",
                                    fontSize = 10.5.sp,
                                    color = secondaryText
                                )
                                Text(
                                    text = uvData.recommendedSpf,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryText
                                )
                            }
                        }
                    }

                    // Max Unprotected Time Pill
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
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = "Safe Exposure Time",
                                tint = Color(0xFF00B4D8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Max Safe Unprotected",
                                    fontSize = 10.5.sp,
                                    color = secondaryText
                                )
                                Text(
                                    text = uvData.maxUnprotectedTimeMinutes,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryText
                                )
                            }
                        }
                    }
                }
            }

            // Required Protective Gear Chips Row
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "RECOMMENDED PROTECTION GEAR",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = secondaryText,
                    letterSpacing = 0.5.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uvData.requiredGear.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, cardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Gear item",
                                    tint = riskColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primaryText
                                )
                            }
                        }
                    }
                }
            }

            // Protection Advice & Peak Hours Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = riskColor.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, riskColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Advice",
                            tint = riskColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uvData.protectionAdvice,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryText,
                            lineHeight = 16.sp
                        )
                    }
                    Text(
                        text = uvData.peakHoursNote,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryText.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
