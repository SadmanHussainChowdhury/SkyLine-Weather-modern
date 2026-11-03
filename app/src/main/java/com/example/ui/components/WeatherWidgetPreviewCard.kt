package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.WeatherData
import com.example.widget.WeatherGlanceWidget
import kotlinx.coroutines.launch

@Composable
fun WeatherWidgetPreviewCard(
    weather: WeatherData,
    theme: WeatherTheme,
    isCelsius: Boolean = true,
    onToggleUnit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }
    var lastSyncedTimestamp by remember { mutableStateOf<Long?>(null) }

    // Sync widget automatically when weather or unit changes
    LaunchedEffect(weather.city, weather.tempC, isCelsius) {
        val highC = weather.dailyForecast.firstOrNull()?.maxTempC ?: (weather.tempC + 3.0)
        val lowC = weather.dailyForecast.firstOrNull()?.minTempC ?: (weather.tempC - 4.0)
        WeatherGlanceWidget.updateAllWidgets(
            context = context.applicationContext,
            cityName = weather.city,
            tempC = weather.tempC,
            tempF = weather.tempF,
            condition = weather.conditionText,
            isCelsius = isCelsius,
            highC = highC,
            lowC = lowC
        )
        lastSyncedTimestamp = System.currentTimeMillis()
    }

    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor

    val displayTemp = if (isCelsius) "${weather.tempC.toInt()}°C" else "${weather.tempF.toInt()}°F"
    val highC = weather.dailyForecast.firstOrNull()?.maxTempC ?: (weather.tempC + 3.0)
    val lowC = weather.dailyForecast.firstOrNull()?.minTempC ?: (weather.tempC - 4.0)

    val highLowText = if (isCelsius) {
        "H: ${highC.toInt()}° L: ${lowC.toInt()}°"
    } else {
        val highF = (highC * 9 / 5) + 32
        val lowF = (lowC * 9 / 5) + 32
        "H: ${highF.toInt()}° L: ${lowF.toInt()}°"
    }

    val widgetIconRes = when {
        weather.conditionText.contains("Rain", true) || weather.conditionText.contains("Shower", true) || weather.conditionText.contains("Drizzle", true) -> R.drawable.ic_widget_rainy
        weather.conditionText.contains("Snow", true) || weather.conditionText.contains("Ice", true) || weather.conditionText.contains("Flurry", true) -> R.drawable.ic_widget_snowy
        weather.conditionText.contains("Cloud", true) || weather.conditionText.contains("Overcast", true) || weather.conditionText.contains("Mist", true) -> R.drawable.ic_widget_cloudy
        else -> R.drawable.ic_widget_sunny
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("glance_widget_preview_card"),
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
                            imageVector = Icons.Outlined.Home,
                            contentDescription = "Glance Widget",
                            tint = Color(0xFF0288D1),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Home Screen Widget (Glance)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                        }
                        Text(
                            text = "Live AppWidget powered by Jetpack Glance",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF22C55E).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Ready",
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF22C55E)
                        )
                    }
                }
            }

            // High-fidelity Home Screen Widget Layout Simulation Box
            Text(
                text = "Glance AppWidget Live Preview (3x2 Grid Cell):",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = secondaryText
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.5.dp, Color(0xFF0288D1).copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("glance_widget_simulation_container")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = weather.city,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = weather.conditionText,
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Image(
                            painter = painterResource(id = widgetIconRes),
                            contentDescription = weather.conditionText,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = displayTemp,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF29B6F6)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = highLowText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFCBD5E1),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Smartphone,
                                contentDescription = "Home Screen",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }
            }

            // Sync and Settings Control Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0288D1),
                    contentColor = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            scope.launch {
                                isSyncing = true
                                WeatherGlanceWidget.updateAllWidgets(
                                    context = context.applicationContext,
                                    cityName = weather.city,
                                    tempC = weather.tempC,
                                    tempF = weather.tempF,
                                    condition = weather.conditionText,
                                    isCelsius = isCelsius,
                                    highC = highC,
                                    lowC = lowC
                                )
                                lastSyncedTimestamp = System.currentTimeMillis()
                                isSyncing = false
                                Toast.makeText(context, "Glance Widget updated with ${weather.city} weather!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("sync_glance_widget_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Sync Widget",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSyncing) "Syncing Glance..." else "Sync Glance Widget",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleUnit() }
                        .testTag("toggle_widget_unit_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Thermostat,
                            contentDescription = "Toggle Unit",
                            tint = primaryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCelsius) "Unit: °C" else "Unit: °F",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }
                }
            }

            // Launch Widget Configuration Activity Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        try {
                            val intent = android.content.Intent(context, com.example.WidgetConfigActivity::class.java)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot launch widget config: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .testTag("open_widget_configurator_button")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Outlined.Smartphone,
                        contentDescription = "Configure Widget Metrics",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Customize Widget Metrics (UV, AQI, Humidity)",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(color = cardBorder.copy(alpha = 0.5f))

            // Metadata Technical Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Provider: WeatherGlanceWidgetReceiver",
                    fontSize = 10.5.sp,
                    color = secondaryText
                )
                Text(
                    text = if (lastSyncedTimestamp != null) "Last Synced: Just now" else "Sync Pending",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0288D1)
                )
            }
        }
    }
}
