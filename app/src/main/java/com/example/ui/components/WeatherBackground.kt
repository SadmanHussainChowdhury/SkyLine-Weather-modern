package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Weather codes from WeatherAPI.com to categorize conditions
fun getWeatherTheme(conditionCode: Int, isDark: Boolean = false): WeatherTheme {
    return when (conditionCode) {
        1000 -> if (isDark) WeatherTheme.SunnyDark else WeatherTheme.Sunny
        1003, 1006, 1009 -> if (isDark) WeatherTheme.CloudyDark else WeatherTheme.Cloudy
        1063, 1180, 1183, 1186, 1189, 1192, 1195, 1240, 1243 -> if (isDark) WeatherTheme.RainyDark else WeatherTheme.Rainy
        1066, 1114, 1210, 1213, 1216, 1219, 1222, 1225, 1255 -> if (isDark) WeatherTheme.SnowyDark else WeatherTheme.Snowy
        1087, 1273, 1276, 1279, 1282 -> if (isDark) WeatherTheme.ThunderstormDark else WeatherTheme.Thunderstorm
        else -> if (isDark) WeatherTheme.CloudyDark else WeatherTheme.Cloudy // Fallback
    }
}

sealed class WeatherTheme(
    val topColor: Color,
    val bottomColor: Color,
    val primaryTextColor: Color,
    val secondaryTextColor: Color
) {
    val backgroundBrush: Brush get() = Brush.verticalGradient(listOf(topColor, bottomColor))

    object Sunny : WeatherTheme(
        topColor = Color(0xFFE0F1FF),
        bottomColor = Color(0xFFD1E4FF),
        primaryTextColor = Color(0xFF1A1C1E),
        secondaryTextColor = Color(0xFF42474E)
    )

    object SunnyDark : WeatherTheme(
        topColor = Color(0xFF0F172A),
        bottomColor = Color(0xFF1E293B),
        primaryTextColor = Color.White,
        secondaryTextColor = Color(0xFFCBD5E1)
    )

    object Cloudy : WeatherTheme(
        topColor = Color(0xFFECF1F6),
        bottomColor = Color(0xFFD7E2EC),
        primaryTextColor = Color(0xFF1A1C1E),
        secondaryTextColor = Color(0xFF535F70)
    )

    object CloudyDark : WeatherTheme(
        topColor = Color(0xFF182232),
        bottomColor = Color(0xFF0F172A),
        primaryTextColor = Color.White,
        secondaryTextColor = Color(0xFFCBD5E1)
    )

    object Rainy : WeatherTheme(
        topColor = Color(0xFFD1DFED),
        bottomColor = Color(0xFFBCCCDD),
        primaryTextColor = Color(0xFF1A1C1E),
        secondaryTextColor = Color(0xFF42474E)
    )

    object RainyDark : WeatherTheme(
        topColor = Color(0xFF0F172A),
        bottomColor = Color(0xFF1E1B4B),
        primaryTextColor = Color.White,
        secondaryTextColor = Color(0xFFCBD5E1)
    )

    object Snowy : WeatherTheme(
        topColor = Color(0xFFEDF4F9),
        bottomColor = Color(0xFFD3E0EA),
        primaryTextColor = Color(0xFF1A1C1E),
        secondaryTextColor = Color(0xFF42474E)
    )

    object SnowyDark : WeatherTheme(
        topColor = Color(0xFF0F172A),
        bottomColor = Color(0xFF1E293B),
        primaryTextColor = Color.White,
        secondaryTextColor = Color(0xFFCBD5E1)
    )

    object Thunderstorm : WeatherTheme(
        topColor = Color(0xFF1E293B),
        bottomColor = Color(0xFF0F172A),
        primaryTextColor = Color.White,
        secondaryTextColor = Color(0xFF94A3B8)
    )

    object ThunderstormDark : WeatherTheme(
        topColor = Color(0xFF1E1B4B),
        bottomColor = Color(0xFF090D16),
        primaryTextColor = Color.White,
        secondaryTextColor = Color(0xFF94A3B8)
    )
}

// Immutable animation seed data generated once per condition code to keep animations stable and performant
private class SunRaySeed(val angle: Float, val beamWidth: Float)

private class DustSeed(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val maxAlpha: Float,
    val phase: Float
)

private class CloudSeed(
    val startX: Float,
    val y: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float
)

private class RainSeed(
    val x: Float,
    val speed: Float,
    val length: Float,
    val width: Float,
    val alpha: Float,
    val layer: Int // 0: background, 1: midground, 2: foreground
)

private class SnowSeed(
    val x: Float,
    val size: Float,
    val speed: Float,
    val swaySpeed: Float,
    val swayAmplitude: Float,
    val alpha: Float,
    val type: Int, // 0: crystal, 1: soft flake, 2: small dot
    val phase: Float,
    val rotationSpeed: Float
)

@Composable
fun WeatherBackground(
    conditionCode: Int,
    isDark: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val theme = getWeatherTheme(conditionCode, isDark)

    // Setup an infinite transition as the timeline driver
    val infiniteTransition = rememberInfiniteTransition(label = "weather_background_anim")
    val timeMs by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Remember procedural seeds based on conditionCode to avoid object allocations during render
    val sunRays = remember(conditionCode) {
        val r = Random(conditionCode)
        List(16) { i ->
            SunRaySeed(
                angle = i * (360f / 16f),
                beamWidth = 1.5f + r.nextFloat() * 2.5f
            )
        }
    }

    val dustSeeds = remember(conditionCode) {
        val r = Random(conditionCode)
        List(25) {
            DustSeed(
                x = r.nextFloat(),
                y = r.nextFloat(),
                size = 2f + r.nextFloat() * 6f,
                speed = 0.4f + r.nextFloat() * 1.6f,
                maxAlpha = 0.15f + r.nextFloat() * 0.25f,
                phase = r.nextFloat() * 6.28f
            )
        }
    }

    val cloudSeeds = remember(conditionCode) {
        val r = Random(conditionCode)
        List(6) {
            CloudSeed(
                startX = r.nextFloat(),
                y = 0.04f + r.nextFloat() * 0.38f,
                speed = 0.25f + r.nextFloat() * 0.5f,
                size = 110f + r.nextFloat() * 90f,
                alpha = 0.14f + r.nextFloat() * 0.22f
            )
        }
    }

    val rainSeeds = remember(conditionCode) {
        val r = Random(conditionCode)
        List(85) { index ->
            val layer = when {
                index % 5 == 0 -> 2 // Foreground
                index % 2 == 0 -> 1 // Midground
                else -> 0 // Background
            }
            RainSeed(
                x = r.nextFloat(),
                speed = when (layer) {
                    2 -> 1.1f + r.nextFloat() * 0.5f
                    1 -> 0.75f + r.nextFloat() * 0.4f
                    else -> 0.5f + r.nextFloat() * 0.3f
                },
                length = when (layer) {
                    2 -> 45f + r.nextFloat() * 25f
                    1 -> 30f + r.nextFloat() * 18f
                    else -> 18f + r.nextFloat() * 12f
                },
                width = when (layer) {
                    2 -> 2.2f + r.nextFloat() * 1.0f
                    1 -> 1.4f + r.nextFloat() * 0.6f
                    else -> 0.8f + r.nextFloat() * 0.4f
                },
                alpha = when (layer) {
                    2 -> 0.35f + r.nextFloat() * 0.35f
                    1 -> 0.25f + r.nextFloat() * 0.25f
                    else -> 0.12f + r.nextFloat() * 0.18f
                },
                layer = layer
            )
        }
    }

    val snowSeeds = remember(conditionCode) {
        val r = Random(conditionCode)
        List(65) { index ->
            val type = index % 3 // 0: crystal, 1: soft flake, 2: small dot
            SnowSeed(
                x = r.nextFloat(),
                size = when (type) {
                    0 -> 6f + r.nextFloat() * 4f // Detailed crystal arms
                    1 -> 4f + r.nextFloat() * 5f // Soft large flake
                    else -> 2f + r.nextFloat() * 2.5f // Tiny snow dot
                },
                speed = 0.4f + r.nextFloat() * 0.6f,
                swaySpeed = 0.6f + r.nextFloat() * 1.2f,
                swayAmplitude = 10f + r.nextFloat() * 22f,
                alpha = 0.25f + r.nextFloat() * 0.5f,
                type = type,
                phase = r.nextFloat() * 6.28f,
                rotationSpeed = (if (r.nextBoolean()) 1 else -1) * (0.5f + r.nextFloat() * 1.5f)
            )
        }
    }

    val animTopColor by animateColorAsState(
        targetValue = theme.topColor,
        animationSpec = tween(durationMillis = 850),
        label = "weather_top_color_anim"
    )
    val animBottomColor by animateColorAsState(
        targetValue = theme.bottomColor,
        animationSpec = tween(durationMillis = 850),
        label = "weather_bottom_color_anim"
    )

    val animatedBackgroundBrush = remember(animTopColor, animBottomColor) {
        Brush.verticalGradient(listOf(animTopColor, animBottomColor))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(animatedBackgroundBrush)
    ) {
        // Pure Native High-Performance Canvas Animation Layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (theme) {
                is WeatherTheme.Sunny, is WeatherTheme.SunnyDark -> {
                    val cx = size.width * 0.82f
                    val cy = size.height * 0.13f
                    val baseRadius = 68.dp.toPx()
                    val sunPulse = 1f + 0.06f * sin(timeMs * 0.002f)
                    val currentRadius = baseRadius * sunPulse

                    // Glowing outer multi-ring coronal aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFF176).copy(alpha = 0.5f),
                                Color(0xFFFFB74D).copy(alpha = 0.22f),
                                Color(0xFFFF9800).copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = currentRadius * 2.2f
                        ),
                        radius = currentRadius * 2.2f,
                        center = Offset(cx, cy)
                    )

                    // Bright core sun sphere
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFFFF59D),
                                Color(0xFFFFD54F)
                            ),
                            center = Offset(cx, cy),
                            radius = currentRadius
                        ),
                        radius = currentRadius,
                        center = Offset(cx, cy)
                    )

                    // Rotating 16 radial sun rays
                    val rayLength = 220.dp.toPx()
                    sunRays.forEach { ray ->
                        val angleDeg = ray.angle + (timeMs * 0.005f)
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val startX = cx + cos(angleRad).toFloat() * currentRadius * 0.8f
                        val startY = cy + sin(angleRad).toFloat() * currentRadius * 0.8f
                        val endX = cx + cos(angleRad).toFloat() * rayLength
                        val endY = cy + sin(angleRad).toFloat() * rayLength

                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFEB3B).copy(alpha = 0.25f),
                                    Color(0xFFFFCA28).copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY)
                            ),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = ray.beamWidth.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Lens Flare Hexagon & Circular Rings along light vector
                    val axisVectorX = cx - (size.width * 0.6f)
                    val axisVectorY = cy + (size.height * 0.5f)
                    listOf(0.25f, 0.45f, 0.70f).forEachIndexed { idx, factor ->
                        val fx = cx - axisVectorX * factor
                        val fy = cy + axisVectorY * factor
                        val flareRadius = (15 + idx * 12).dp.toPx() * (0.9f + 0.1f * sin(timeMs * 0.003f + idx))
                        drawCircle(
                            color = when (idx) {
                                0 -> Color(0xFF81D4FA).copy(alpha = 0.15f)
                                1 -> Color(0xFFFFD54F).copy(alpha = 0.18f)
                                else -> Color(0xFFCE93D8).copy(alpha = 0.12f)
                            },
                            radius = flareRadius,
                            center = Offset(fx, fy)
                        )
                    }

                    // Floating golden dust particles
                    dustSeeds.forEach { seed ->
                        val elapsedY = (seed.y - (timeMs * 0.000018f * seed.speed)) % 1f
                        val finalY = (if (elapsedY < 0f) elapsedY + 1f else elapsedY) * size.height
                        val sway = sin(timeMs * 0.0014f * seed.speed + seed.phase) * 14.dp.toPx()
                        val finalX = (seed.x * size.width + sway) % size.width
                        val alpha = seed.maxAlpha * (0.5f + 0.5f * sin(timeMs * 0.0025f * seed.speed + seed.phase))

                        drawCircle(
                            color = Color(0xFFFFF176).copy(alpha = alpha),
                            radius = seed.size.dp.toPx(),
                            center = Offset(finalX, finalY)
                        )
                    }
                }

                is WeatherTheme.Cloudy, is WeatherTheme.CloudyDark -> {
                    cloudSeeds.forEach { seed ->
                        val totalDriftWidth = size.width * 1.6f
                        val startOffset = -size.width * 0.3f
                        val currentDriftX = (seed.startX * totalDriftWidth + (timeMs * 0.016f * seed.speed)) % totalDriftWidth
                        val finalX = startOffset + currentDriftX
                        val finalY = seed.y * size.height
                        val cloudSize = seed.size.dp.toPx()
                        val cloudColor = Color.White.copy(alpha = seed.alpha)

                        // Compound soft cloud shapes
                        drawCircle(
                            color = cloudColor,
                            radius = cloudSize * 0.45f,
                            center = Offset(finalX, finalY)
                        )
                        drawCircle(
                            color = cloudColor,
                            radius = cloudSize * 0.35f,
                            center = Offset(finalX - cloudSize * 0.3f, finalY + cloudSize * 0.05f)
                        )
                        drawCircle(
                            color = cloudColor,
                            radius = cloudSize * 0.35f,
                            center = Offset(finalX + cloudSize * 0.3f, finalY + cloudSize * 0.05f)
                        )
                    }
                }

                is WeatherTheme.Rainy, is WeatherTheme.RainyDark -> {
                    val slantAngle = -12f
                    val angleRad = Math.toRadians(slantAngle.toDouble())
                    val cosAngle = cos(angleRad).toFloat()
                    val sinAngle = sin(angleRad).toFloat()

                    val dropColor = if (theme is WeatherTheme.RainyDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

                    // Render rain streaks with surface splash effects
                    rainSeeds.forEach { seed ->
                        val totalHeightRange = size.height + 120.dp.toPx()
                        val startYOffset = -60.dp.toPx()
                        val elapsedY = (timeMs * 0.65f * seed.speed) % totalHeightRange
                        val finalY = startYOffset + elapsedY

                        val finalX = (seed.x * size.width + (finalY * sinAngle)) % size.width
                        val finalXClamped = if (finalX < 0) finalX + size.width else finalX
                        val endX = finalXClamped - seed.length * sinAngle
                        val endY = finalY + seed.length * cosAngle

                        // Rain Drop Streak
                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, dropColor.copy(alpha = seed.alpha)),
                                startY = finalY,
                                endY = endY
                            ),
                            start = Offset(finalXClamped, finalY),
                            end = Offset(endX, endY),
                            strokeWidth = seed.width.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Surface Splash Effect when drop approaches bottom
                        if (seed.layer >= 1 && finalY > size.height * 0.82f && finalY < size.height * 0.98f) {
                            val splashProgress = (finalY - size.height * 0.82f) / (size.height * 0.16f)
                            val splashRadius = (4 + seed.layer * 5).dp.toPx() * splashProgress
                            val splashAlpha = seed.alpha * (1f - splashProgress)

                            // Splash expanding ring arc
                            drawArc(
                                color = dropColor.copy(alpha = splashAlpha),
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = Offset(finalXClamped - splashRadius, finalY - splashRadius * 0.4f),
                                size = Size(splashRadius * 2, splashRadius * 0.8f),
                                style = Stroke(width = 1.2.dp.toPx())
                            )

                            // Splash micro droplets
                            drawCircle(
                                color = dropColor.copy(alpha = splashAlpha),
                                radius = 1.2.dp.toPx(),
                                center = Offset(finalXClamped - splashRadius * 0.5f, finalY - splashRadius * 0.6f)
                            )
                            drawCircle(
                                color = dropColor.copy(alpha = splashAlpha),
                                radius = 1.2.dp.toPx(),
                                center = Offset(finalXClamped + splashRadius * 0.5f, finalY - splashRadius * 0.6f)
                            )
                        }
                    }

                    // Overcast top mist cloud gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF64748B).copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = size.height * 0.22f
                        )
                    )
                }

                is WeatherTheme.Snowy, is WeatherTheme.SnowyDark -> {
                    snowSeeds.forEach { seed ->
                        val totalHeightRange = size.height + 50.dp.toPx()
                        val startYOffset = -25.dp.toPx()
                        val elapsedY = (timeMs * 0.08f * seed.speed) % totalHeightRange
                        val finalY = startYOffset + elapsedY

                        val sway = sin(timeMs * 0.0012f * seed.swaySpeed + seed.phase) * seed.swayAmplitude.dp.toPx()
                        val finalX = (seed.x * size.width + sway) % size.width
                        val finalXClamped = if (finalX < 0) finalX + size.width else finalX

                        when (seed.type) {
                            0 -> {
                                // Real 6-pointed Snow Crystal arms
                                val radiusPx = seed.size.dp.toPx()
                                val rotationAngle = timeMs * 0.015f * seed.rotationSpeed
                                for (i in 0 until 6) {
                                    val armAngleDeg = i * 60f + rotationAngle
                                    val armAngleRad = Math.toRadians(armAngleDeg.toDouble())
                                    val armEndX = finalXClamped + cos(armAngleRad).toFloat() * radiusPx
                                    val armEndY = finalY + sin(armAngleRad).toFloat() * radiusPx

                                    drawLine(
                                        color = Color.White.copy(alpha = seed.alpha),
                                        start = Offset(finalXClamped, finalY),
                                        end = Offset(armEndX, armEndY),
                                        strokeWidth = 1.5.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                                drawCircle(
                                    color = Color.White.copy(alpha = seed.alpha * 0.8f),
                                    radius = radiusPx * 0.25f,
                                    center = Offset(finalXClamped, finalY)
                                )
                            }
                            1 -> {
                                // Soft large fluff flake
                                drawCircle(
                                    color = Color.White.copy(alpha = seed.alpha * 0.85f),
                                    radius = seed.size.dp.toPx(),
                                    center = Offset(finalXClamped, finalY)
                                )
                            }
                            else -> {
                                // Tiny background snow dot
                                drawCircle(
                                    color = Color.White.copy(alpha = seed.alpha * 0.6f),
                                    radius = seed.size.dp.toPx() * 0.6f,
                                    center = Offset(finalXClamped, finalY)
                                )
                            }
                        }
                    }

                    // Bottom soft snow atmosphere gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.15f)
                            ),
                            startY = size.height * 0.85f,
                            endY = size.height
                        )
                    )
                }

                is WeatherTheme.Thunderstorm, is WeatherTheme.ThunderstormDark -> {
                    // Lightning overlay flash phase controller
                    val cycleTime = timeMs % 8500
                    val flashAlpha = when {
                        cycleTime in 7100f..7140f -> ((cycleTime - 7100f) / 40f) * 0.5f
                        cycleTime in 7140f..7180f -> 0.5f - ((cycleTime - 7140f) / 40f) * 0.4f
                        cycleTime in 7180f..7220f -> 0.1f + ((cycleTime - 7180f) / 40f) * 0.85f
                        cycleTime in 7220f..7380f -> 0.85f * (1f - (cycleTime - 7220f) / 160f)
                        else -> 0f
                    }

                    if (flashAlpha > 0f) {
                        drawRect(color = Color.White.copy(alpha = flashAlpha))

                        // Procedural Jagged Lightning Bolt Path
                        val boltPath = Path().apply {
                            var startX = size.width * 0.6f
                            var startY = size.height * 0.05f
                            moveTo(startX, startY)
                            val segments = 6
                            val segHeight = (size.height * 0.55f) / segments
                            val r = Random(7140)
                            for (s in 1..segments) {
                                startX += (r.nextFloat() - 0.48f) * 90.dp.toPx()
                                startY += segHeight
                                lineTo(startX, startY)
                            }
                        }

                        drawPath(
                            path = boltPath,
                            color = Color(0xFFE0F2FE).copy(alpha = flashAlpha),
                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Heavy, rapid, vertical storm rain
                    val slantAngle = -15f
                    val angleRad = Math.toRadians(slantAngle.toDouble())
                    val cosAngle = cos(angleRad).toFloat()
                    val sinAngle = sin(angleRad).toFloat()

                    rainSeeds.forEach { seed ->
                        val totalHeightRange = size.height + 140.dp.toPx()
                        val startYOffset = -70.dp.toPx()
                        val elapsedY = (timeMs * 0.95f * seed.speed) % totalHeightRange
                        val finalY = startYOffset + elapsedY

                        val finalX = (seed.x * size.width + (finalY * sinAngle)) % size.width
                        val finalXClamped = if (finalX < 0) finalX + size.width else finalX
                        val endX = finalXClamped - (seed.length * 1.3f) * sinAngle
                        val endY = finalY + (seed.length * 1.3f) * cosAngle

                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF94A3B8).copy(alpha = seed.alpha * 1.3f)),
                                startY = finalY,
                                endY = endY
                            ),
                            start = Offset(finalXClamped, finalY),
                            end = Offset(endX, endY),
                            strokeWidth = (seed.width * 1.3f).dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Native Content (foreground UI elements)
        content()
    }
}

