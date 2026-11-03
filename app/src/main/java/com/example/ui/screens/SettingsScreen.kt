package com.example.ui.screens

import com.example.ui.components.rememberAppHaptics
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.viewmodel.WeatherViewModel
import com.example.ui.components.DeviceMigrationCard

import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.FileDownload

@Composable
fun SettingsScreen(
    viewModel: WeatherViewModel,
    onOpenWidgetConfig: (() -> Unit)? = null,
    onOpenSmartWidgets: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val useCelsius by viewModel.isCelsius.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val aestheticTheme by viewModel.aestheticTheme.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val realtimeSyncEnabled by viewModel.realtimeSyncEnabled.collectAsState()
    val realtimeSyncIntervalSec by viewModel.realtimeSyncIntervalSec.collectAsState()
    val lastSyncTimestampMillis by viewModel.lastSyncTimeMillis.collectAsState()
    var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
    val haptics = rememberAppHaptics()
    
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Preferences",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Section 1: Appearance Theme Card (System Default, Light, Dark)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("theme_settings_card"),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = "Theme Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "App Theme",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose light, dark, or system setting",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                // Option 1: System Default
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (themeMode != "system") {
                                haptics.performToggleMode()
                                viewModel.setThemeMode("system")
                            }
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = themeMode == "system",
                        onClick = {
                            if (themeMode != "system") {
                                haptics.performToggleMode()
                                viewModel.setThemeMode("system")
                            }
                        },
                        modifier = Modifier.testTag("theme_option_system"),
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "System Default (Auto)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Automatically switches with device dark/light settings",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Option 2: Light Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (themeMode != "light") {
                                haptics.performToggleMode()
                                viewModel.setThemeMode("light")
                            }
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = themeMode == "light",
                        onClick = {
                            if (themeMode != "light") {
                                haptics.performToggleMode()
                                viewModel.setThemeMode("light")
                            }
                        },
                        modifier = Modifier.testTag("theme_option_light"),
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Light Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Always keep light appearance",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Option 3: Dark Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (themeMode != "dark") {
                                haptics.performToggleMode()
                                viewModel.setThemeMode("dark")
                            }
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = themeMode == "dark",
                        onClick = {
                            if (themeMode != "dark") {
                                haptics.performToggleMode()
                                viewModel.setThemeMode("dark")
                            }
                        },
                        modifier = Modifier.testTag("theme_option_dark"),
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Dark Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Always keep dark appearance",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Section 2: Aesthetic Color Themes Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("aesthetic_theme_card"),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ColorLens,
                        contentDescription = "Aesthetic Theme Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Aesthetic Color Palette",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Select custom accent & gradient color styles",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                val aestheticOptions = listOf(
                    Triple(
                        "midnight_blue",
                        "Midnight Blue",
                        "Deep sapphire, cosmic indigo & neon cyan glow"
                    ) to listOf(Color(0xFF38BDF8), Color(0xFF1E293B), Color(0xFF090D16)),
                    Triple(
                        "sunset_gold",
                        "Sunset Gold",
                        "Warm twilight gradient, golden amber & coral orange"
                    ) to listOf(Color(0xFFF59E0B), Color(0xFF78350F), Color(0xFF180E0A)),
                    Triple(
                        "monochrome",
                        "Monochrome",
                        "Minimalist obsidian slate, silver & high contrast"
                    ) to listOf(Color(0xFFE2E8F0), Color(0xFF27272A), Color(0xFF0F0F10)),
                    Triple(
                        "emerald_aurora",
                        "Emerald Aurora",
                        "Nordic polar night green, mint & aurora teal"
                    ) to listOf(Color(0xFF34D399), Color(0xFF065F46), Color(0xFF061412)),
                    Triple(
                        "skyline_classic",
                        "Skyline Classic",
                        "Original atmospheric sky blue & cloud palette"
                    ) to listOf(Color(0xFF006495), Color(0xFFD1E4FF), Color(0xFFF0F4F8))
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    aestheticOptions.forEach { (info, colors) ->
                        val (key, title, subtitle) = info
                        val isSelected = aestheticTheme == key

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (!isSelected) {
                                        haptics.performToggleMode()
                                        viewModel.setAestheticTheme(key)
                                    }
                                }
                                .testTag("theme_option_$key")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Visual Color Swatch
                                    Row(
                                        modifier = Modifier.testTag("theme_swatch_$key"),
                                        horizontalArrangement = Arrangement.spacedBy((-6).dp)
                                    ) {
                                        colors.forEach { swatchColor ->
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(swatchColor)
                                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = title,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = subtitle,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setAestheticTheme(key) },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("radio_$key")
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Temperature Units Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptics.performToggleMode()
                        viewModel.toggleUnit()
                    }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Thermostat,
                        contentDescription = "Temperature Unit Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Temperature Unit",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (useCelsius) "Celsius (°C)" else "Fahrenheit (°F)",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = !useCelsius, // false means Celsius, true means Fahrenheit
                    onCheckedChange = {
                        haptics.performToggleMode()
                        viewModel.toggleUnit()
                    },
                    modifier = Modifier.testTag("unit_toggle_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        // Smart Widgets & Home Cards Manager Link
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptics.performKeyTap()
                    onOpenSmartWidgets?.invoke()
                }
                .testTag("settings_smart_widgets_card"),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Widgets,
                        contentDescription = "Smart Widgets Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Smart Widgets Manager",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Enable or disable UV, AQI, Pollen & Thermal cards on Home",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Outlined.Widgets,
                    contentDescription = "Open Smart Widgets Manager",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Section 2.5: Saved Locations Widget Configuration Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptics.performKeyTap()
                    onOpenWidgetConfig?.invoke()
                }
                .testTag("settings_widget_config_card"),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Widgets,
                        contentDescription = "Widget Setup Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Saved Locations Widget",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pin specific cities from search history to Android Home Screen",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Outlined.Widgets,
                    contentDescription = "Navigate to Widget Setup",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Section 2.7: Real-Time Live Weather Sync Controls
        RealTimeSyncSettingsCard(
            realtimeSyncEnabled = realtimeSyncEnabled,
            realtimeSyncIntervalSec = realtimeSyncIntervalSec,
            lastSyncTimestampMillis = lastSyncTimestampMillis,
            onToggleSync = { enabled -> viewModel.setRealtimeSyncEnabled(enabled) },
            onSetIntervalSec = { sec -> viewModel.setRealtimeSyncIntervalSec(sec) },
            onForceSyncNow = { viewModel.refreshWeather() },
            haptics = haptics
        )

        // Section 3: API Connection and Key Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Key,
                        contentDescription = "API Configuration Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "API Key Configuration",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "By default, Skyline Weather operates in 'Local High-Fidelity Simulation' mode with fully randomized, location-specific weather reports. To load real-time global weather, register a free key on WeatherAPI.com and enter it below.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    placeholder = { Text("Enter WeatherAPI Key", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_text_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = { viewModel.saveCustomApiKey(apiKeyInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_api_key_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Check Icon",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Apply API Key", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Section 4: Connection Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudSync,
                    contentDescription = "Status Icon",
                    tint = if (customApiKey.isNotEmpty()) Color(0xFF2E7D32) else Color(0xFFFFB300),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "System Sync Mode",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (customApiKey.isNotEmpty()) "Active: Real-time WeatherAPI Sync" else "Active: Local High-Fidelity Simulation",
                        fontSize = 13.sp,
                        color = if (customApiKey.isNotEmpty()) Color(0xFF2E7D32) else Color(0xFFFFB300)
                    )
                }
            }
        }

        // Section 5: Recently Searched Cities (Room DB) Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recent_searches_card"),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "Recent Searches Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Recently Searched Cities",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Persisted in local Room database",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (recentSearches.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                haptics.performKeyTap()
                                viewModel.clearRecentSearches()
                            },
                            modifier = Modifier.testTag("clear_recent_searches_button")
                        ) {
                            Text(
                                text = "Clear All",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                if (recentSearches.isEmpty()) {
                    Text(
                        text = "No recent searches yet. Search for any city to save it locally for quick access.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    val sdf = remember { java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault()) }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentSearches.forEach { recent ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.updateSearchQuery(recent.cityName)
                                        viewModel.fetchWeather(recent.cityName)
                                    }
                                    .testTag("settings_recent_item_${recent.cityName.lowercase().replace(" ", "_")}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.History,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = recent.cityName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Searched ${sdf.format(java.util.Date(recent.searchedAt))}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeRecentSearch(recent.cityName) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("delete_recent_${recent.cityName.lowercase().replace(" ", "_")}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete recent search",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Device Migration & Backup (Export / Import Saved Locations & Search History)
        DeviceMigrationCard(
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth()
        )

        // Section 6: Mobile Download & Installation Guide
        MobileInstallationGuideCard(
            modifier = Modifier.fillMaxWidth()
        )

        // Section 7: Info Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "About Applet Icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Skyline Weather • Version 1.0",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MobileInstallationGuideCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("mobile_installation_guide_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.PhoneAndroid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Install on Mobile Phone",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Download APK or test directly on your device",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "1. ", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Download APK directly: In the top bar of AI Studio, open Settings / Export menu and click 'Download APK' to get the installer file for your Android phone.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "2. ", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Install on Android: Open the downloaded APK on your phone and tap Install. If prompted, allow 'Install from unknown sources'.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "3. ", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Export Source Code: Click 'Export Project as ZIP' to inspect or build the full Jetpack Compose Kotlin project in Android Studio.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun RealTimeSyncSettingsCard(
    realtimeSyncEnabled: Boolean,
    realtimeSyncIntervalSec: Int,
    lastSyncTimestampMillis: Long,
    onToggleSync: (Boolean) -> Unit,
    onSetIntervalSec: (Int) -> Unit,
    onForceSyncNow: () -> Unit,
    haptics: com.example.ui.components.AppHaptics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("realtime_sync_settings_card"),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudSync,
                        contentDescription = "Real-Time Sync",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Real-Time Weather Sync",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Automatic live background updates and sensor refresh",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = realtimeSyncEnabled,
                    onCheckedChange = {
                        haptics.performToggleMode()
                        onToggleSync(it)
                    },
                    modifier = Modifier.testTag("settings_realtime_sync_switch"),
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Live Sync Interval",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val intervals = listOf(
                        30 to "30s",
                        60 to "1 min",
                        120 to "2 min",
                        300 to "5 min",
                        900 to "15 min"
                    )
                    intervals.forEach { (sec, label) ->
                        val selected = realtimeSyncEnabled && realtimeSyncIntervalSec == sec
                        FilterChip(
                            selected = selected,
                            onClick = {
                                haptics.performKeyTap()
                                onToggleSync(true)
                                onSetIntervalSec(sec)
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("settings_sync_interval_chip_${sec}s")
                        )
                    }
                }
            }

            Button(
                onClick = {
                    haptics.performSuccess()
                    onForceSyncNow()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_force_sync_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudSync,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Sync All Weather Data Now")
            }
        }
    }
}
