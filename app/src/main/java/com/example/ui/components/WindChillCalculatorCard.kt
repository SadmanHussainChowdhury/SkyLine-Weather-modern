package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.SevereCold
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.FrostbiteRiskLevel
import com.example.data.repository.WeatherData
import com.example.data.repository.WindChillData
import kotlin.math.roundToInt

@Composable
fun WindChillCalculatorCard(
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

    // Interactive custom simulator state
    var isSimulatorExpanded by remember { mutableStateOf(false) }
    var customTemp by remember(weather.tempC) { mutableDoubleStateOf(if (isCelsius) weather.tempC else weather.tempF) }
    var customWindSpeed by remember(weather.windKph) { mutableDoubleStateOf(if (isCelsius) weather.windKph else weather.windMph) }

    // Live calculation for current weather
    val currentWindChill = remember(weather.tempC, weather.tempF, weather.windKph, weather.windMph) {
        WindChillData.calculate(
            tempC = weather.tempC,
            tempF = weather.tempF,
            windKph = weather.windKph,
            windMph = weather.windMph
        )
    }

    // Calculation for active display (either current weather or simulated)
    val activeWindChill = if (!isSimulatorExpanded) {
        currentWindChill
    } else {
        val tempC = if (isCelsius) customTemp else (customTemp - 32) * 5 / 9
        val tempF = if (isCelsius) (customTemp * 9 / 5) + 32 else customTemp
        val windKph = if (isCelsius) customWindSpeed else customWindSpeed * 1.60934
        val windMph = if (isCelsius) customWindSpeed / 1.60934 else customWindSpeed

        WindChillData.calculate(tempC, tempF, windKph, windMph)
    }

    val riskColor = Color(activeWindChill.riskLevel.colorHex)

    val displayedChill = if (isCelsius) activeWindChill.windChillC else activeWindChill.windChillF
    val displayedActual = if (isCelsius) activeWindChill.actualTempC else activeWindChill.actualTempF
    val displayedDiff = if (isCelsius) activeWindChill.tempDifferenceC else activeWindChill.tempDifferenceF
    val unitLabel = if (isCelsius) "°C" else "°F"
    val windUnitLabel = if (isCelsius) "km/h" else "mph"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("wind_chill_calculator_card"),
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
                            .background(riskColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SevereCold,
                            contentDescription = "Wind Chill Calculator",
                            tint = riskColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Wind Chill Index Calculator",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = if (isSimulatorExpanded) "Simulated Custom Conditions" else "NWS Wind Chill Formula",
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
                            text = activeWindChill.riskLevel.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = riskColor
                        )
                    }
                }
            }

            // Wind Chill vs Actual Temp Primary Display Box
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Wind Chill Big Number
                        Column {
                            Text(
                                text = "WIND CHILL INDEX",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = riskColor,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${displayedChill.roundToInt()}°",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = riskColor
                                )
                                Text(
                                    text = unitLabel,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = secondaryText,
                                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                                )
                            }
                        }

                        // Right: Actual Temp & Wind Speed Pills
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Thermostat,
                                        contentDescription = "Air Temp",
                                        tint = primaryText,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Air Temp: ${displayedActual.roundToInt()}$unitLabel",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryText
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Air,
                                        contentDescription = "Wind Speed",
                                        tint = Color(0xFF00B4D8),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val currentWindVal = if (isCelsius) activeWindChill.windKph else activeWindChill.windMph
                                    Text(
                                        text = "Wind: ${currentWindVal.roundToInt()} $windUnitLabel",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryText
                                    )
                                }
                            }
                        }
                    }

                    // Cooling Effect Difference Badge
                    if (displayedDiff > 0.5) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0288D1).copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AcUnit,
                                    contentDescription = "Cold Draught",
                                    tint = Color(0xFF0288D1),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Wind makes outdoor air feel ${"%.1f".format(displayedDiff)}$unitLabel colder!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0288D1)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Wind speed is too low or temperature is above 50°F (10°C) — wind chill effect is minimal.",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }
            }

            // Frostbite Time & Advice Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = riskColor.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, riskColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = "Risk Estimate",
                            tint = riskColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeWindChill.frostbiteEstimateTime,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Outlined.Checkroom,
                            contentDescription = "Clothing Recommendation",
                            tint = secondaryText,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeWindChill.clothingAdvice,
                            fontSize = 12.sp,
                            color = primaryText,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = cardBorder.copy(alpha = 0.5f))

            // Expandable Custom Interactive Scenario Test
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { isSimulatorExpanded = !isSimulatorExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Speed,
                        contentDescription = "Interactive Simulator",
                        tint = Color(0xFF00B4D8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSimulatorExpanded) "Close Custom Wind Chill Simulator" else "Test Custom Temperature & Wind Speed",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00B4D8)
                    )
                }

                Icon(
                    imageVector = if (isSimulatorExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = "Toggle Simulator",
                    tint = Color(0xFF00B4D8),
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isSimulatorExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Adjust sliders below to calculate custom wind chill index:",
                        fontSize = 11.5.sp,
                        color = secondaryText
                    )

                    // Temperature Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Air Temperature", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = primaryText)
                            Text("${customTemp.roundToInt()} $unitLabel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = riskColor)
                        }
                        val minTemp = if (isCelsius) -30f else -22f
                        val maxTemp = if (isCelsius) 15f else 59f
                        Slider(
                            value = customTemp.toFloat(),
                            onValueChange = { customTemp = it.toDouble() },
                            valueRange = minTemp..maxTemp,
                            colors = SliderDefaults.colors(
                                thumbColor = riskColor,
                                activeTrackColor = riskColor
                            )
                        )
                    }

                    // Wind Speed Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Wind Speed", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = primaryText)
                            Text("${customWindSpeed.roundToInt()} $windUnitLabel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00B4D8))
                        }
                        val maxWind = if (isCelsius) 100f else 62f
                        Slider(
                            value = customWindSpeed.toFloat(),
                            onValueChange = { customWindSpeed = it.toDouble() },
                            valueRange = 0f..maxWind,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00B4D8),
                                activeTrackColor = Color(0xFF00B4D8)
                            )
                        )
                    }
                }
            }
        }
    }
}
