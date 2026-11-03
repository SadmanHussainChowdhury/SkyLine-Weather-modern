package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.data.location.GeocodingService
import com.example.data.repository.WeatherData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class SolarCalculations(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val sunriseStr: String,
    val sunsetStr: String,
    val solarNoonStr: String,
    val dawnStr: String,
    val duskStr: String,
    val goldenHourStr: String,
    val daylightMinutes: Int,
    val currentElevationDeg: Double,
    val currentAzimuthDeg: Double,
    val sunriseMin: Int,
    val sunsetMin: Int,
    val solarNoonMin: Int
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GpsSunriseSunsetCalculatorCard(
    weather: WeatherData,
    theme: WeatherTheme,
    onRefreshGpsLocation: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var coords by remember(weather.city) {
        mutableStateOf(GeocodingService.getCoordinatesFromCity(context, weather.city))
    }

    var showDetails by remember { mutableStateOf(false) }

    val solarCalc = remember(coords, weather.city) {
        calculateSolarTimes(coords.first, coords.second, weather.city)
    }

    val calendar = Calendar.getInstance()
    val currentMin = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

    val isDaylight = currentMin in solarCalc.sunriseMin..solarCalc.sunsetMin
    val totalDaylightMin = (solarCalc.sunsetMin - solarCalc.sunriseMin).coerceAtLeast(1)

    val daylightProgressRaw = if (isDaylight) {
        ((currentMin - solarCalc.sunriseMin).toFloat() / totalDaylightMin).coerceIn(0f, 1f)
    } else if (currentMin < solarCalc.sunriseMin) {
        0f
    } else {
        1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = daylightProgressRaw,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "gps_sun_progress"
    )

    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor
    val sunAmber = Color(0xFFFFB300)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("gps_sunrise_sunset_card"),
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
                            .background(sunAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WbTwilight,
                            contentDescription = "GPS Solar Calculator",
                            tint = sunAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "GPS Solar & Daylight Calculator",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "${solarCalc.cityName} (${String.format(Locale.US, "%.2f°", coords.first)}, ${String.format(Locale.US, "%.2f°", coords.second)})",
                            fontSize = 12.sp,
                            color = secondaryText
                        )
                    }
                }

                if (onRefreshGpsLocation != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = sunAmber.copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onRefreshGpsLocation() }
                            .testTag("calculate_gps_solar_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MyLocation,
                                contentDescription = "Sync GPS Location",
                                tint = sunAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GPS Sync",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = sunAmber
                            )
                        }
                    }
                }
            }

            // Interactive Arc Timeline Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("gps_solar_arc_canvas")
                ) {
                    val width = size.width
                    val height = size.height

                    val horizonY = height - 24.dp.toPx()
                    val startX = 24.dp.toPx()
                    val endX = width - 24.dp.toPx()
                    val arcWidth = endX - startX
                    val arcHeight = height - 48.dp.toPx()

                    // Horizon Line
                    drawLine(
                        color = secondaryText.copy(alpha = 0.3f),
                        start = Offset(0f, horizonY),
                        end = Offset(width, horizonY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    // Arc Path
                    val arcPath = Path().apply {
                        moveTo(startX, horizonY)
                        cubicTo(
                            startX + arcWidth * 0.25f, horizonY - arcHeight,
                            startX + arcWidth * 0.75f, horizonY - arcHeight,
                            endX, horizonY
                        )
                    }

                    // Daylight Glow Gradient
                    val glowBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFB300).copy(alpha = 0.25f),
                            Color(0xFFFB8C00).copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        startY = horizonY - arcHeight,
                        endY = horizonY
                    )

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
                    drawPath(path = filledPath, brush = glowBrush)

                    // Arc Stroke Line
                    drawPath(
                        path = arcPath,
                        color = secondaryText.copy(alpha = 0.4f),
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                        )
                    )

                    // Sunrise & Sunset Anchors
                    drawCircle(color = Color(0xFFFF9800), radius = 5.dp.toPx(), center = Offset(startX, horizonY))
                    drawCircle(color = Color(0xFFE65100), radius = 5.dp.toPx(), center = Offset(endX, horizonY))

                    // Sun Position on Bezier
                    val t = animatedProgress
                    val p0 = Offset(startX, horizonY)
                    val p1 = Offset(startX + arcWidth * 0.25f, horizonY - arcHeight)
                    val p2 = Offset(startX + arcWidth * 0.75f, horizonY - arcHeight)
                    val p3 = Offset(endX, horizonY)

                    val oneMinusT = 1f - t
                    val sunX = (oneMinusT * oneMinusT * oneMinusT * p0.x) +
                            (3f * oneMinusT * oneMinusT * t * p1.x) +
                            (3f * oneMinusT * t * t * p2.x) +
                            (t * t * t * p3.x)

                    val sunY = (oneMinusT * oneMinusT * oneMinusT * p0.y) +
                            (3f * oneMinusT * oneMinusT * t * p1.y) +
                            (3f * oneMinusT * t * t * p2.y) +
                            (t * t * t * p3.y)

                    // Glow & Sun body
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFC107).copy(alpha = 0.6f),
                                Color(0xFFFF9800).copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(sunX, sunY),
                            radius = 28.dp.toPx()
                        ),
                        radius = 28.dp.toPx(),
                        center = Offset(sunX, sunY)
                    )

                    drawCircle(color = Color(0xFFFFC107), radius = 9.dp.toPx(), center = Offset(sunX, sunY))
                    drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(sunX, sunY))

                    if (isDaylight) {
                        drawLine(
                            color = Color(0xFFFFC107).copy(alpha = 0.5f),
                            start = Offset(sunX, sunY),
                            end = Offset(sunX, horizonY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                        )
                    }
                }
            }

            // Primary Timings Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sunrise Block
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "GPS SUNRISE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryText,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = solarCalc.sunriseStr,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryText,
                        modifier = Modifier.testTag("sunrise_time_value")
                    )
                    Text(
                        text = "Dawn: ${solarCalc.dawnStr}",
                        fontSize = 11.sp,
                        color = secondaryText
                    )
                }

                // Solar Noon Block
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.WbSunny,
                        contentDescription = "Solar Noon",
                        tint = sunAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "SOLAR NOON",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryText
                    )
                    Text(
                        text = solarCalc.solarNoonStr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                }

                // Sunset Block
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "GPS SUNSET",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryText,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = solarCalc.sunsetStr,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryText,
                        modifier = Modifier.testTag("sunset_time_value")
                    )
                    Text(
                        text = "Dusk: ${solarCalc.duskStr}",
                        fontSize = 11.sp,
                        color = secondaryText
                    )
                }
            }

            // Summary Stats Pill Cards
            val hrs = solarCalc.daylightMinutes / 60
            val mins = solarCalc.daylightMinutes % 60

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val itemMod = Modifier.weight(1f)

                SolarStatTile(
                    title = "Total Daylight",
                    value = "$hrs hrs $mins min",
                    icon = Icons.Outlined.Schedule,
                    theme = theme,
                    modifier = itemMod
                )

                SolarStatTile(
                    title = "Solar Elevation",
                    value = "${String.format(Locale.US, "%.1f°", solarCalc.currentElevationDeg)}",
                    icon = Icons.Outlined.Explore,
                    theme = theme,
                    modifier = itemMod
                )

                SolarStatTile(
                    title = "Golden Hour",
                    value = solarCalc.goldenHourStr,
                    icon = Icons.Outlined.LightMode,
                    theme = theme,
                    modifier = itemMod
                )
            }

            // Toggle Astronomical Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDetails = !showDetails }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Solar Details",
                        tint = secondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Astronomical Solar Angles & Position",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = secondaryText
                    )
                }

                Text(
                    text = if (showDetails) "Hide" else "Show Details",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = sunAmber
                )
            }

            AnimatedVisibility(
                visible = showDetails,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "NOAA Solar Calculation Engine Data",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )

                        HorizontalDivider(color = cardBorder)

                        DetailRow("GPS Latitude / Longitude", "${String.format(Locale.US, "%.4f° N", coords.first)}, ${String.format(Locale.US, "%.4f° W", coords.second)}", theme)
                        DetailRow("Solar Azimuth Angle", "${String.format(Locale.US, "%.1f°", solarCalc.currentAzimuthDeg)}", theme)
                        DetailRow("Civil Twilight (Dawn - Dusk)", "${solarCalc.dawnStr} - ${solarCalc.duskStr}", theme)
                        DetailRow("Solar Noon Transit", solarCalc.solarNoonStr, theme)
                    }
                }
            }
        }
    }
}

@Composable
private fun SolarStatTile(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val isDark = theme.primaryTextColor == Color.White
    val bg = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFFFFB300),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 10.5.sp,
                    color = theme.secondaryTextColor
                )
                Text(
                    text = value,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primaryTextColor
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    theme: WeatherTheme
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = theme.secondaryTextColor
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = theme.primaryTextColor
        )
    }
}

/**
 * Astronomical Solar Calculation Engine using standard NOAA algorithms.
 * Calculates precise Sunrise, Sunset, Solar Noon, Twilight, Elevation, and Azimuth.
 */
fun calculateSolarTimes(lat: Double, lon: Double, cityName: String): SolarCalculations {
    val calendar = Calendar.getInstance()
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    val tzOffsetHours = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (1000.0 * 3600.0)

    val latRad = Math.toRadians(lat)

    // Fractional year in radians
    val gamma = 2.0 * Math.PI / 365.0 * (dayOfYear - 1 + (calendar.get(Calendar.HOUR_OF_DAY) - 12) / 24.0)

    // Equation of time in minutes
    val eqTime = 229.18 * (0.000075 + 0.001868 * cos(gamma) - 0.032077 * sin(gamma) - 0.014615 * cos(2 * gamma) - 0.040849 * sin(2 * gamma))

    // Solar declination angle in radians
    val decl = 0.006918 - 0.399912 * cos(gamma) + 0.070257 * sin(gamma) - 0.006758 * cos(2 * gamma) + 0.000907 * sin(2 * gamma) - 0.002697 * cos(3 * gamma) + 0.00148 * sin(3 * gamma)

    // Official Zenith angle for Sunrise/Sunset = 90.833°
    val zenithSunriseSunset = Math.toRadians(90.833)
    val cosHa = (cos(zenithSunriseSunset) - sin(latRad) * sin(decl)) / (cos(latRad) * cos(decl))

    // Handle Polar Day / Night edge cases safely
    val haDeg = when {
        cosHa >= 1.0 -> 0.0 // Polar night
        cosHa <= -1.0 -> 180.0 // Polar day
        else -> Math.toDegrees(acos(cosHa))
    }

    // Solar Noon UTC in minutes from midnight
    val solarNoonUtcMin = 720.0 - (4.0 * lon) - eqTime

    // Sunrise and Sunset UTC in minutes
    val sunriseUtcMin = solarNoonUtcMin - (4.0 * haDeg)
    val sunsetUtcMin = solarNoonUtcMin + (4.0 * haDeg)

    // Convert UTC minutes to Local Time Minutes
    val localOffsetMin = (tzOffsetHours * 60.0).toInt()
    val sunriseLocalMin = (sunriseUtcMin + localOffsetMin).toInt().mod(1440)
    val sunsetLocalMin = (sunsetUtcMin + localOffsetMin).toInt().mod(1440)
    val solarNoonLocalMin = (solarNoonUtcMin + localOffsetMin).toInt().mod(1440)

    // Dawn and Dusk (Civil Twilight, Zenith = 96.0°)
    val zenithTwilight = Math.toRadians(96.0)
    val cosHaTwilight = ((cos(zenithTwilight) - sin(latRad) * sin(decl)) / (cos(latRad) * cos(decl))).coerceIn(-1.0, 1.0)
    val haTwilightDeg = Math.toDegrees(acos(cosHaTwilight))
    val dawnLocalMin = (solarNoonUtcMin - (4.0 * haTwilightDeg) + localOffsetMin).toInt().mod(1440)
    val duskLocalMin = (solarNoonUtcMin + (4.0 * haTwilightDeg) + localOffsetMin).toInt().mod(1440)

    // Golden Hour (Zenith = 84.0°)
    val goldenHourStartMin = (sunsetLocalMin - 45).coerceAtLeast(0)

    // Calculate current Solar Elevation and Azimuth Angle
    val currentMin = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    val timeOffsetUtcMin = currentMin - localOffsetMin
    val trueSolarTimeMin = (timeOffsetUtcMin + eqTime + 4.0 * lon).mod(1440.0)
    val hourAngleRad = Math.toRadians((trueSolarTimeMin / 4.0) - 180.0)

    val elevationRad = asin(sin(latRad) * sin(decl) + cos(latRad) * cos(decl) * cos(hourAngleRad))
    val currentElevationDeg = Math.toDegrees(elevationRad)

    val cosAzimuth = (sin(decl) - sin(latRad) * sin(elevationRad)) / (cos(latRad) * cos(elevationRad))
    var azimuthRad = acos(cosAzimuth.coerceIn(-1.0, 1.0))
    if (sin(hourAngleRad) > 0) {
        azimuthRad = 2.0 * Math.PI - azimuthRad
    }
    val currentAzimuthDeg = Math.toDegrees(azimuthRad)

    val daylightMin = if (sunsetLocalMin >= sunriseLocalMin) {
        sunsetLocalMin - sunriseLocalMin
    } else {
        (1440 - sunriseLocalMin) + sunsetLocalMin
    }

    return SolarCalculations(
        latitude = lat,
        longitude = lon,
        cityName = cityName,
        sunriseStr = formatMinutesToAmPm(sunriseLocalMin),
        sunsetStr = formatMinutesToAmPm(sunsetLocalMin),
        solarNoonStr = formatMinutesToAmPm(solarNoonLocalMin),
        dawnStr = formatMinutesToAmPm(dawnLocalMin),
        duskStr = formatMinutesToAmPm(duskLocalMin),
        goldenHourStr = formatMinutesToAmPm(goldenHourStartMin),
        daylightMinutes = daylightMin,
        currentElevationDeg = currentElevationDeg,
        currentAzimuthDeg = currentAzimuthDeg,
        sunriseMin = sunriseLocalMin,
        sunsetMin = sunsetLocalMin,
        solarNoonMin = solarNoonLocalMin
    )
}

private fun formatMinutesToAmPm(totalMinutes: Int): String {
    val hrs = (totalMinutes / 60) % 24
    val mins = totalMinutes % 60
    val amPm = if (hrs >= 12) "PM" else "AM"
    val formattedHour = when {
        hrs == 0 -> 12
        hrs > 12 -> hrs - 12
        else -> hrs
    }
    return String.format(Locale.US, "%02d:%02d %s", formattedHour, mins, amPm)
}
