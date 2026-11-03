package com.example

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SettingsDataStore
import com.example.ui.components.rememberAppHaptics
import com.example.ui.theme.MyApplicationTheme
import com.example.widget.WeatherGlanceWidget
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set default result to CANCELED in case user exits early
        setResult(RESULT_CANCELED)

        // Extract appWidgetId from launching Intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        setContent {
            MyApplicationTheme {
                WidgetConfigScreen(
                    appWidgetId = appWidgetId,
                    onApplyConfig = {
                        val resultValue = Intent().apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                        setResult(RESULT_OK, resultValue)
                        finish()
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(
    appWidgetId: Int,
    onApplyConfig: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val haptics = rememberAppHaptics()
    val scope = rememberCoroutineScope()
    val settingsDataStore = remember { SettingsDataStore(context.applicationContext) }

    val showUv by settingsDataStore.widgetShowUvFlow.collectAsState(initial = true)
    val showAqi by settingsDataStore.widgetShowAqiFlow.collectAsState(initial = true)
    val showHumidity by settingsDataStore.widgetShowHumidityFlow.collectAsState(initial = true)
    val showWind by settingsDataStore.widgetShowWindFlow.collectAsState(initial = true)
    val showFeelsLike by settingsDataStore.widgetShowFeelsLikeFlow.collectAsState(initial = true)
    val pinnedCity by settingsDataStore.pinnedWidgetCityFlow.collectAsState(initial = "San Francisco")
    val useCelsius by settingsDataStore.useCelsiusFlow.collectAsState(initial = true)

    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Glance Widget Configurator",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) "Widget ID #$appWidgetId" else "Home Screen App Widget",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            haptics.performKeyTap()
                            onCancel()
                        },
                        modifier = Modifier.testTag("widget_config_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Cancel & Close"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            haptics.performKeyTap()
                            onCancel()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("widget_config_cancel_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            haptics.performSuccess()
                            scope.launch {
                                isSaving = true
                                try {
                                    // Refresh glance widget with chosen configuration
                                    WeatherGlanceWidget.updateAllWidgets(
                                        context = context.applicationContext,
                                        cityName = pinnedCity,
                                        tempC = 22.0,
                                        tempF = 71.6,
                                        condition = "Partly Cloudy",
                                        isCelsius = useCelsius,
                                        highC = 25.0,
                                        lowC = 16.0,
                                        uv = 5.2,
                                        aqi = 35,
                                        humidity = 62,
                                        windKph = 14.5,
                                        feelsLikeC = 21.5,
                                        feelsLikeF = 70.7
                                    )
                                    Toast.makeText(context, "Widget configuration saved!", Toast.LENGTH_SHORT).show()
                                    onApplyConfig()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("widget_config_apply_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply & Add Widget", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Live Interactive Preview Box
            item {
                Text(
                    text = "LIVE WIDGET PREVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                WidgetPreviewCard(
                    cityName = pinnedCity,
                    showUv = showUv,
                    showAqi = showAqi,
                    showHumidity = showHumidity,
                    showWind = showWind,
                    showFeelsLike = showFeelsLike,
                    isCelsius = useCelsius
                )
            }

            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            // Customize Specific Weather Metrics
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Customize Widget Metrics",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Select which extra environmental parameters are rendered on your Glance widget strip.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricToggleRow(
                            title = "UV Index",
                            description = "Shows sun intensity & warning level (e.g., UV 5 Moderate)",
                            icon = Icons.Default.WbSunny,
                            iconColor = Color(0xFFF59E0B),
                            isChecked = showUv,
                            onToggle = { checked ->
                                scope.launch { settingsDataStore.setWidgetShowMetric("uv", checked) }
                            },
                            testTag = "toggle_widget_uv"
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        MetricToggleRow(
                            title = "Air Quality Index (AQI)",
                            description = "Displays live air quality index status (e.g., AQI Good)",
                            icon = Icons.Default.Air,
                            iconColor = Color(0xFF10B981),
                            isChecked = showAqi,
                            onToggle = { checked ->
                                scope.launch { settingsDataStore.setWidgetShowMetric("aqi", checked) }
                            },
                            testTag = "toggle_widget_aqi"
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        MetricToggleRow(
                            title = "Humidity Percentage",
                            description = "Displays relative atmosphere moisture level (e.g., Hum: 62%)",
                            icon = Icons.Default.WaterDrop,
                            iconColor = Color(0xFF3B82F6),
                            isChecked = showHumidity,
                            onToggle = { checked ->
                                scope.launch { settingsDataStore.setWidgetShowMetric("humidity", checked) }
                            },
                            testTag = "toggle_widget_humidity"
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        MetricToggleRow(
                            title = "Wind Velocity",
                            description = "Displays current wind speed & direction",
                            icon = Icons.Default.Cloud,
                            iconColor = Color(0xFF8B5CF6),
                            isChecked = showWind,
                            onToggle = { checked ->
                                scope.launch { settingsDataStore.setWidgetShowMetric("wind", checked) }
                            },
                            testTag = "toggle_widget_wind"
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        MetricToggleRow(
                            title = "Feels Like Temperature",
                            description = "Applies wind-chill and heat-index adjusted temperature",
                            icon = Icons.Default.Thermostat,
                            iconColor = Color(0xFFEF4444),
                            isChecked = showFeelsLike,
                            onToggle = { checked ->
                                scope.launch { settingsDataStore.setWidgetShowMetric("feels_like", checked) }
                            },
                            testTag = "toggle_widget_feels_like"
                        )
                    }
                }
            }

            // Location Target Info
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pinned Location: $pinnedCity",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "You can change your pinned widget city anytime in Saved Locations.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun MetricToggleRow(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    testTag: String
) {
    val haptics = rememberAppHaptics()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = { checked ->
                haptics.performKeyTap()
                onToggle(checked)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = iconColor
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun WidgetPreviewCard(
    cityName: String,
    showUv: Boolean,
    showAqi: Boolean,
    showHumidity: Boolean,
    showWind: Boolean,
    showFeelsLike: Boolean,
    isCelsius: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = cityName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Partly Cloudy",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Text(
                    text = "🌤️",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (isCelsius) "22°C" else "72°F",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isCelsius) "H: 25° L: 16°" else "H: 77° L: 61°",
                    fontSize = 11.5.sp,
                    color = Color(0xFFCBD5E1)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Strip of active metrics
            val activeMetrics = mutableListOf<String>()
            if (showUv) activeMetrics.add("UV 5 (Moderate)")
            if (showAqi) activeMetrics.add("AQI: Good (35)")
            if (showHumidity) activeMetrics.add("Hum: 62%")
            if (showWind) activeMetrics.add("Wind: 14 km/h")
            if (showFeelsLike) activeMetrics.add("Feels: 21°")

            if (activeMetrics.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = activeMetrics.take(3).joinToString(" • "),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else {
                Text(
                    text = "(No metrics toggled on widget)",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
