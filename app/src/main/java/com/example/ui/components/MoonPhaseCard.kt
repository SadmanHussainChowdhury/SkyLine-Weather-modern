package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Brightness3
import androidx.compose.material.icons.outlined.NightlightRound
import androidx.compose.material.icons.outlined.WbSunny
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.MoonPhaseData
import com.example.data.repository.WeatherData

@Composable
fun MoonPhaseCard(
    weather: WeatherData,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val data = remember(weather) { MoonPhaseData.calculateFromWeather(weather) }
    
    // Smoothly animate the illumination percentage for the progress bar & canvas
    var animationTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationTrigger = true
    }

    val animatedIllumFraction by animateFloatAsState(
        targetValue = if (animationTrigger) (data.illuminationPercentage / 100f) else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "moon_illum_progress_anim"
    )

    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.92f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val moonAccentColor = if (isDark) Color(0xFFC084FC) else Color(0xFF8B5CF6)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("moon_phase_card"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, cardBorder)
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
                            .background(moonAccentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bedtime,
                            contentDescription = "Moon Phase Icon",
                            tint = moonAccentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Moon Phase & Lunar Cycle",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "Astronomical Illumination Tracking",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }

                // Stage Emoji Tag
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = moonAccentColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = data.stageEmoji,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${data.illuminationPercentage}% LIT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = moonAccentColor,
                            modifier = Modifier.testTag("moon_illum_badge")
                        )
                    }
                }
            }

            // Split Layout: Illuminated Disc Canvas on Left, Details & Illumination Gauge on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // High-fidelity glowing lunar canvas disc
                Box(
                    modifier = Modifier
                        .size(125.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("moon_canvas")
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2.3f

                        // Background space star dots
                        val starColor = Color.White.copy(alpha = 0.4f)
                        val starCoordinates = listOf(
                            Offset(15.dp.toPx(), 20.dp.toPx()),
                            Offset(110.dp.toPx(), 25.dp.toPx()),
                            Offset(20.dp.toPx(), 110.dp.toPx()),
                            Offset(105.dp.toPx(), 95.dp.toPx()),
                            Offset(50.dp.toPx(), 10.dp.toPx()),
                            Offset(85.dp.toPx(), 115.dp.toPx())
                        )
                        for (coord in starCoordinates) {
                            drawCircle(color = starColor, radius = 1.dp.toPx(), center = coord)
                        }

                        // Corona aura
                        drawCircle(
                            color = moonAccentColor.copy(alpha = 0.10f),
                            radius = radius + 8.dp.toPx(),
                            center = center
                        )

                        // Dark side base
                        val darkMoonColor = Color(0xFF0F172A)
                        drawCircle(color = darkMoonColor, radius = radius, center = center)

                        // Clip circle for illuminated part
                        val circleClipPath = Path().apply {
                            addOval(androidx.compose.ui.geometry.Rect(center, radius))
                        }

                        val litMoonColor = Color(0xFFFEF08A) // Glowing Moon Yellow

                        clipPath(circleClipPath) {
                            val isWaxing = data.stageIndex in 1..3
                            val isFull = data.stageIndex == 4
                            val isNew = data.stageIndex == 0

                            if (isFull) {
                                drawCircle(color = litMoonColor, radius = radius, center = center)
                            } else if (!isNew) {
                                val illumFraction = animatedIllumFraction
                                val isRightLit = isWaxing

                                if (illumFraction >= 0.5f) {
                                    val startAngle = if (isRightLit) 270f else 90f
                                    drawArc(
                                        color = litMoonColor,
                                        startAngle = startAngle,
                                        sweepAngle = 180f,
                                        useCenter = true,
                                        size = Size(radius * 2, radius * 2),
                                        topLeft = Offset(center.x - radius, center.y - radius)
                                    )
                                    val ellipseWidth = radius * 2 * (illumFraction - 0.5f) * 2
                                    val ellipseLeft = if (isRightLit) center.x - ellipseWidth / 2 else center.x - radius
                                    drawOval(
                                        color = litMoonColor,
                                        topLeft = Offset(ellipseLeft, center.y - radius),
                                        size = Size(ellipseWidth, radius * 2)
                                    )
                                } else {
                                    val startAngle = if (isRightLit) 270f else 90f
                                    drawArc(
                                        color = litMoonColor,
                                        startAngle = startAngle,
                                        sweepAngle = 180f,
                                        useCenter = true,
                                        size = Size(radius * 2, radius * 2),
                                        topLeft = Offset(center.x - radius, center.y - radius)
                                    )
                                    val ellipseWidth = radius * 2 * (0.5f - illumFraction) * 2
                                    val ellipseLeft = if (isRightLit) center.x - ellipseWidth else center.x
                                    drawOval(
                                        color = darkMoonColor,
                                        topLeft = Offset(ellipseLeft, center.y - radius),
                                        size = Size(ellipseWidth, radius * 2)
                                    )
                                }
                            }

                            // Lunar craters overlay
                            val craterColor = Color(0xFFE2E8F0).copy(alpha = 0.25f)
                            val craterDarkColor = Color(0xFF0F172A).copy(alpha = 0.15f)
                            val craters = listOf(
                                Pair(Offset(-radius * 0.4f, -radius * 0.3f), radius * 0.15f),
                                Pair(Offset(radius * 0.3f, radius * 0.4f), radius * 0.18f),
                                Pair(Offset(radius * 0.1f, -radius * 0.5f), radius * 0.12f),
                                Pair(Offset(-radius * 0.2f, radius * 0.5f), radius * 0.10f),
                                Pair(Offset(-radius * 0.5f, radius * 0.1f), radius * 0.14f)
                            )

                            for ((offset, crRadius) in craters) {
                                val craterCenter = Offset(center.x + offset.x, center.y + offset.y)
                                drawCircle(
                                    color = if (isNew) craterDarkColor else craterColor,
                                    radius = crRadius,
                                    center = craterCenter
                                )
                            }
                        }

                        // Outline
                        drawCircle(
                            color = Color.White.copy(alpha = 0.2f),
                            radius = radius,
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                        )
                    }
                }

                // Right Column: Phase Name, Illumination Visual Indicator Bar, Description
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = data.phaseName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryText,
                        modifier = Modifier.testTag("moon_phase_title")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Visual Indicator: Illumination Progress Bar with Animated Fraction
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Illumination",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText
                            )
                            Text(
                                text = "${data.illuminationPercentage}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = moonAccentColor,
                                modifier = Modifier.testTag("moon_illum_text")
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { animatedIllumFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .testTag("moon_illum_progress_bar"),
                            color = moonAccentColor,
                            trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = data.phaseDescription,
                        fontSize = 11.5.sp,
                        color = secondaryText,
                        lineHeight = 15.sp
                    )
                }
            }

            // 8-Stage Lunar Cycle Timeline Visual Tracker
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "8-STAGE LUNAR CYCLE PROGRESS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = secondaryText,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stages = listOf("🌑", "🌒", "🌓", "🌔", "🌕", "🌖", "🌗", "🌘")
                    stages.forEachIndexed { idx, emoji ->
                        val isCurrentStage = idx == data.stageIndex
                        Surface(
                            shape = CircleShape,
                            color = if (isCurrentStage) moonAccentColor.copy(alpha = 0.25f) else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isCurrentStage) moonAccentColor else Color.Transparent
                            ),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = emoji,
                                    fontSize = if (isCurrentStage) 15.sp else 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Stats Banner: Age, Days to Full Moon, Tip
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "LUNAR AGE",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = secondaryText
                            )
                            Text(
                                text = "${data.lunarAgeDays} Days",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "NEXT FULL MOON",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = secondaryText
                            )
                            Text(
                                text = "In ${data.nextFullMoonDays} Days",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = moonAccentColor
                            )
                        }
                    }

                    Text(
                        text = data.observationTip,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryText,
                        lineHeight = 15.sp
                    )
                }
            }

            // Footer Times Row: Moonrise & Moonset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Moonrise
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.NightlightRound,
                        contentDescription = "Moonrise icon",
                        tint = moonAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "MOONRISE",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = secondaryText
                        )
                        Text(
                            text = data.moonrise,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                            modifier = Modifier.testTag("moonrise_text")
                        )
                    }
                }

                // Divider
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(cardBorder)
                )

                // Moonset
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.WbSunny,
                        contentDescription = "Moonset icon",
                        tint = moonAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "MOONSET",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = secondaryText
                        )
                        Text(
                            text = data.moonset,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                            modifier = Modifier.testTag("moonset_text")
                        )
                    }
                }
            }
        }
    }
}
