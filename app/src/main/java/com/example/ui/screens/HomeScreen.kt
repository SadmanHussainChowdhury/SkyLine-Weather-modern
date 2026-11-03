package com.example.ui.screens

import com.example.ui.components.rememberAppHaptics
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Tune
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.compose.material.icons.filled.Share
import com.example.ui.hooks.rememberBrowserGeolocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AirQualityGaugeCard
import com.example.ui.components.PollenAllergyCard
import com.example.ui.components.DailyForecastList
import com.example.ui.components.FcmAlertsCard
import com.example.ui.components.HourlyForecastRow
import com.example.ui.components.TemperatureTrendGraphCard
import com.example.ui.components.WeatherAlertBanner
import com.example.ui.components.WeatherAudioSummaryCard
import com.example.ui.components.WeatherBackground
import com.example.ui.components.WeatherStatsGrid
import com.example.ui.components.WindDashboardCard
import com.example.ui.components.WindChillCalculatorCard
import com.example.ui.components.ThermalComfortCard
import com.example.ui.components.HumidityGaugeCard
import com.example.ui.components.PrecipitationCard
import com.example.data.repository.PrecipitationForecastData
import com.example.ui.components.SmartInsightsCard
import com.example.ui.components.UvDashboardCard
import com.example.ui.components.MoonPhaseCard
import com.example.ui.components.MonthlyClimateTrendsCard
import com.example.ui.components.HistoricalClimateComparisonCard
import com.example.ui.components.DailyWeatherNarrativeTimelineCard
import com.example.ui.components.OfflineWeatherMapCard
import com.example.ui.components.WeatherWidgetPreviewCard
import com.example.ui.components.GpsSunriseSunsetCalculatorCard
import com.example.ui.components.SunCycleCard
import com.example.ui.components.WeatherLottieAnimation
import com.example.ui.components.WeatherLottieShowcaseCard
import com.example.ui.components.RealTimeSyncCard
import com.example.ui.components.generateWeatherSummaryText
import com.example.ui.components.rememberWeatherTtsManager
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.example.ui.components.getWeatherIcon
import com.example.ui.components.getWeatherTheme
import com.example.ui.viewmodel.WeatherState
import com.example.ui.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LocationRequestType {
    NATIVE, BROWSER
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: WeatherViewModel,
    onOpenMap: ((String?) -> Unit)? = null,
    onOpenSmartWidgets: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val weatherState by viewModel.weatherState.collectAsState()
    val isCelsius by viewModel.isCelsius.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val cardVisibility by viewModel.cardVisibility.collectAsState()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val haptics = rememberAppHaptics()

    var locationRequestType by remember { mutableStateOf<LocationRequestType?>(null) }

    val browserGeolocationTrigger = rememberBrowserGeolocation(
        onLocationFetched = { lat, lon ->
            Toast.makeText(context, "Location acquired successfully!", Toast.LENGTH_SHORT).show()
            viewModel.fetchWeatherFromCoordinates(context, lat, lon)
        },
        onError = { error ->
            Toast.makeText(context, "Location service fallback: loading default forecast.", Toast.LENGTH_SHORT).show()
            val fallbackCity = if (searchQuery.isNotBlank()) searchQuery else "San Francisco"
            viewModel.fetchWeather(fallbackCity)
        }
    )

    // Robust permission handling using Accompanist Permissions
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    ) { permissionsMap ->
        val fineGranted = permissionsMap[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissionsMap[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            if (locationRequestType == LocationRequestType.BROWSER) {
                browserGeolocationTrigger()
            } else {
                viewModel.fetchWeatherFromGps(context) {
                    Toast.makeText(context, "Using web location service fallback...", Toast.LENGTH_SHORT).show()
                    browserGeolocationTrigger()
                }
            }
        } else {
            Toast.makeText(context, "Location permission denied. Loading default city.", Toast.LENGTH_LONG).show()
            val fallbackCity = if (searchQuery.isNotBlank()) searchQuery else "San Francisco"
            viewModel.fetchWeather(fallbackCity)
        }
    }

    val triggerLocationFetch = { isBrowser: Boolean ->
        val hasPermission = locationPermissionsState.permissions.any { it.status.isGranted }
        if (hasPermission) {
            if (isBrowser) {
                browserGeolocationTrigger()
            } else {
                viewModel.fetchWeatherFromGps(context) {
                    Toast.makeText(context, "GPS failed. Trying Browser Geolocation...", Toast.LENGTH_SHORT).show()
                    browserGeolocationTrigger()
                }
            }
        } else {
            locationRequestType = if (isBrowser) LocationRequestType.BROWSER else LocationRequestType.NATIVE
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    val favorites by viewModel.favorites.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val smartInsights by viewModel.smartInsightsState.collectAsState()
    val isGeneratingInsights by viewModel.isGeneratingInsights.collectAsState()

    val isRealtimeSyncEnabled by viewModel.realtimeSyncEnabled.collectAsState()
    val realtimeSyncIntervalSec by viewModel.realtimeSyncIntervalSec.collectAsState()
    val lastSyncTimeMillis by viewModel.lastSyncTimeMillis.collectAsState()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemDark
    }

    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val ttsManager = rememberWeatherTtsManager()

    val popularCities = remember {
        listOf(
            "San Francisco", "New York", "Los Angeles", "Chicago", "Miami", "Dallas", "Seattle",
            "Toronto", "Vancouver", "Mexico City", "London", "Paris", "Berlin", "Rome", "Madrid",
            "Amsterdam", "Tokyo", "Beijing", "Shanghai", "Seoul", "Singapore", "Mumbai", "Delhi",
            "Dhaka", "Bangkok", "Dubai", "Cairo", "Sydney", "Melbourne", "Sao Paulo", "Buenos Aires",
            "Johannesburg", "Istanbul", "Barcelona"
        )
    }

    var isSearchFocused by remember { mutableStateOf(false) }

    val matchingCities = remember(searchQuery, favorites) {
        val q = searchQuery.trim().lowercase(Locale.getDefault())
        if (q.isEmpty()) emptyList()
        else {
            val combined = (favorites.map { it.cityName } + popularCities).distinct()
            combined.filter { it.lowercase(Locale.getDefault()).contains(q) && !it.equals(q, ignoreCase = true) }.take(5)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            val conditionCode = when (val state = weatherState) {
                is WeatherState.Success -> state.weather.conditionCode
                else -> 1000 // default sunny
            }
            val theme = getWeatherTheme(conditionCode, isDarkTheme)
            ModalDrawerSheet(
                drawerContainerColor = if (theme.primaryTextColor == Color.White) Color(0xFF1E293B) else Color(0xFFECF1F6),
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Saved Cities",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primaryTextColor
                )
                Text(
                    text = "Tap to view, or delete to remove",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    fontSize = 12.sp,
                    color = theme.secondaryTextColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider(
                    color = theme.secondaryTextColor.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (favorites.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No saved cities yet.\nSearch for cities to add them here!",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = theme.secondaryTextColor
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(favorites) { favorite ->
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495)
                                    )
                                },
                                label = {
                                    Text(
                                        text = favorite.cityName,
                                        fontWeight = FontWeight.Medium,
                                        color = theme.primaryTextColor,
                                        modifier = Modifier.testTag("saved_city_item_${favorite.cityName.lowercase().replace(" ", "_")}")
                                    )
                                },
                                selected = false,
                                onClick = {
                                    viewModel.updateSearchQuery(favorite.cityName)
                                    viewModel.fetchWeather(favorite.cityName)
                                    scope.launch { drawerState.close() }
                                    keyboardController?.hide()
                                },
                                badge = {
                                    IconButton(
                                        onClick = {
                                            viewModel.toggleFavorite(context, favorite.cityName)
                                        },
                                        modifier = Modifier.testTag("delete_saved_city_${favorite.cityName.lowercase().replace(" ", "_")}").size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete City",
                                            tint = Color.Red.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { innerPadding ->
            
            // Active visual condition animation background
            val conditionCode = when (val state = weatherState) {
                is WeatherState.Success -> state.weather.conditionCode
                else -> 1000 // default sunny
            }
            val theme = getWeatherTheme(conditionCode, isDarkTheme)
    
            WeatherBackground(conditionCode = conditionCode, isDark = isDarkTheme) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                
                // 1. Search Bar with GPS button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    IconButton(
                        onClick = {
                            scope.launch { drawerState.open() }
                        },
                        modifier = Modifier
                            .testTag("menu_button")
                            .padding(top = 4.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open saved cities",
                            tint = theme.primaryTextColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(6.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search city (e.g. Paris)", color = theme.secondaryTextColor.copy(alpha = 0.6f), fontSize = 13.5.sp) },
                            leadingIcon = {
                                IconButton(
                                    onClick = {
                                        if (searchQuery.trim().isNotBlank()) {
                                            viewModel.fetchWeather(searchQuery.trim())
                                            keyboardController?.hide()
                                        }
                                    },
                                    modifier = Modifier.testTag("search_leading_button").size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Icon",
                                        tint = theme.primaryTextColor.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            trailingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(
                                            onClick = { viewModel.updateSearchQuery("") },
                                            modifier = Modifier.testTag("search_clear_button").size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                tint = theme.primaryTextColor.copy(alpha = 0.7f)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (searchQuery.trim().isNotBlank()) {
                                                    viewModel.fetchWeather(searchQuery.trim())
                                                    keyboardController?.hide()
                                                }
                                            },
                                            modifier = Modifier.testTag("search_submit_button").size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Submit Search",
                                                tint = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495)
                                            )
                                        }
                                    } else {
                                        IconButton(
                                            onClick = {
                                                keyboardController?.hide()
                                                Toast.makeText(context, "Acquiring location via Web service...", Toast.LENGTH_SHORT).show()
                                                triggerLocationFetch(true)
                                            },
                                            modifier = Modifier.testTag("browser_geolocation_button").size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Public,
                                                contentDescription = "Browser Geolocation Hook Button",
                                                tint = if (theme.primaryTextColor == Color.White) Color(0xFFFFB300) else Color(0xFFE65100)
                                            )
                                        }
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchQuery.trim().isNotBlank()) {
                                        haptics.performKeyTap()
                                        viewModel.fetchWeather(searchQuery.trim())
                                        keyboardController?.hide()
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = theme.primaryTextColor,
                                unfocusedTextColor = theme.primaryTextColor,
                                focusedBorderColor = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495),
                                unfocusedBorderColor = if (theme.primaryTextColor == Color.White) Color.White.copy(alpha = 0.25f) else Color(0xFFDEE3EB),
                                focusedContainerColor = if (theme.primaryTextColor == Color.White) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.65f),
                                unfocusedContainerColor = if (theme.primaryTextColor == Color.White) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.45f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("weather_search_input")
                        )

                        // Autocomplete Suggestions Dropdown Box
                        AnimatedVisibility(visible = matchingCities.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (theme.primaryTextColor == Color.White) Color(0xFF1E293B) else Color.White,
                                border = BorderStroke(1.dp, if (theme.primaryTextColor == Color.White) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                shadowElevation = 8.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    matchingCities.forEach { suggestion ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    haptics.performKeyTap()
                                                    viewModel.updateSearchQuery(suggestion)
                                                    viewModel.fetchWeather(suggestion)
                                                    keyboardController?.hide()
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = "City Suggestion",
                                                tint = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = suggestion,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = theme.primaryTextColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Recent Searches Chip Row for Quick Access
                        AnimatedVisibility(visible = recentSearches.isNotEmpty() && matchingCities.isEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp)
                                    .testTag("recent_searches_chip_row"),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(recentSearches, key = { it.cityName }) { recent ->
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (theme.primaryTextColor == Color.White) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.75f),
                                        border = BorderStroke(
                                            1.dp,
                                            if (theme.primaryTextColor == Color.White) Color.White.copy(alpha = 0.25f) else Color(0xFFCBD5E1)
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .clickable {
                                                haptics.performKeyTap()
                                                viewModel.updateSearchQuery(recent.cityName)
                                                viewModel.fetchWeather(recent.cityName)
                                                keyboardController?.hide()
                                            }
                                            .testTag("recent_chip_${recent.cityName.lowercase().replace(" ", "_")}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = "Recent Search",
                                                tint = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = recent.cityName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = theme.primaryTextColor
                                            )
                                            IconButton(
                                                onClick = { viewModel.removeRecentSearch(recent.cityName) },
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .testTag("remove_recent_${recent.cityName.lowercase().replace(" ", "_")}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove recent search",
                                                    tint = theme.secondaryTextColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            haptics.performKeyTap()
                            keyboardController?.hide()
                            Toast.makeText(context, "Acquiring current GPS location...", Toast.LENGTH_SHORT).show()
                            triggerLocationFetch(false)
                        },
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(48.dp)
                            .testTag("gps_location_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "GPS Location Button",
                            tint = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            haptics.performPullToRefresh()
                            viewModel.refreshWeather()
                        },
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(48.dp)
                            .testTag("refresh_weather_button")
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Button",
                                tint = theme.primaryTextColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Quick-access Saved Cities Horizontal Suggestions Row
                AnimatedVisibility(visible = favorites.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saved:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.secondaryTextColor
                        )
                        favorites.forEach { favorite ->
                            SuggestionChip(
                                onClick = {
                                    haptics.performKeyTap()
                                    viewModel.updateSearchQuery(favorite.cityName)
                                    viewModel.fetchWeather(favorite.cityName)
                                    keyboardController?.hide()
                                },
                                label = { 
                                    Text(
                                        text = favorite.cityName, 
                                        color = theme.primaryTextColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    ) 
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (theme.primaryTextColor == Color.White) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.5f)
                                ),
                                border = null,
                                modifier = Modifier.testTag("quick_chip_${favorite.cityName.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }

                // 2. Weather Content States
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        haptics.performPullToRefresh()
                        viewModel.refreshWeather()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("pull_to_refresh_box")
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                    when (val state = weatherState) {
                        is WeatherState.Idle -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Placeholder",
                                    tint = theme.primaryTextColor.copy(alpha = 0.4f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Welcome to Skyline Weather",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.primaryTextColor,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Enter a city name above or tap the GPS location target to load real-time forecast data.",
                                    fontSize = 14.sp,
                                    color = theme.secondaryTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        is WeatherState.Loading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Retrieving Weather Reports...",
                                    color = theme.primaryTextColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        is WeatherState.Success -> {
                            val weather = state.weather
                            val currentTemp = if (isCelsius) {
                                "${Math.round(weather.tempC)}°"
                            } else {
                                "${Math.round(weather.tempF)}°"
                            }

                            // Dynamic current formatted Date/Time
                            val formattedDate = remember(weather.timestamp) {
                                SimpleDateFormat("EEEE, MMMM d • hh:mm a", Locale.getDefault()).format(Date())
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                            ) {
                                // Temperature and Condition Header
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = formattedDate,
                                        fontSize = 14.sp,
                                        color = theme.secondaryTextColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // City Name & Favorite Star Toggle
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = weather.city,
                                            fontSize = 30.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.primaryTextColor,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.testTag("weather_city_name")
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = {
                                                haptics.performSuccess()
                                                viewModel.toggleFavorite(context, weather.city)
                                            },
                                            modifier = Modifier.testTag("favorite_toggle_button").size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = "Toggle Favorite",
                                                tint = if (isFavorite) Color(0xFFFFB300) else theme.primaryTextColor,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Mobile-Friendly Action Toolbar
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (theme.primaryTextColor == Color.White) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.05f),
                                        border = BorderStroke(1.dp, if (theme.primaryTextColor == Color.White) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    haptics.performKeyTap()
                                                    onOpenMap?.invoke(weather.city)
                                                },
                                                modifier = Modifier.testTag("header_live_map_button").size(38.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Map,
                                                    contentDescription = "Open Live Weather Map",
                                                    tint = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    haptics.performKeyTap()
                                                    val summaryText = generateWeatherSummaryText(weather, isCelsius)
                                                    ttsManager.speak(summaryText)
                                                },
                                                modifier = Modifier.testTag("header_tts_button").size(38.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = "Read Forecast Aloud",
                                                    tint = if (ttsManager.isSpeaking) Color(0xFFE53935) else (if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495)),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    haptics.performSuccess()
                                                    shareWeatherSummary(context, weather, isCelsius)
                                                },
                                                modifier = Modifier.testTag("header_share_button").size(38.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = "Share Current Weather",
                                                    tint = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    haptics.performKeyTap()
                                                    onOpenSmartWidgets?.invoke()
                                                },
                                                modifier = Modifier.testTag("header_smart_widgets_button").size(38.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Tune,
                                                    contentDescription = "Manage Smart Widgets",
                                                    tint = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = currentTemp,
                                        fontSize = 72.sp,
                                        fontWeight = FontWeight.Thin,
                                        color = theme.primaryTextColor,
                                        modifier = Modifier.testTag("current_temperature_text")
                                    )

                                    WeatherLottieAnimation(
                                        conditionCode = weather.conditionCode,
                                        size = 120.dp,
                                        tintColor = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = getWeatherIcon(weather.conditionCode),
                                            contentDescription = "Condition Icon",
                                            tint = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = weather.conditionText,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = theme.primaryTextColor,
                                            modifier = Modifier.testTag("weather_condition_text")
                                        )
                                    }
                                }

                                // Severe Weather Warning Alert Banner
                                WeatherAlertBanner(
                                    alertHeadline = weather.alertHeadline,
                                    alertDesc = weather.alertDesc,
                                    city = weather.city,
                                    theme = theme,
                                    onScanGps = { triggerLocationFetch(false) }
                                )

                                // Real-Time FCM Severe Weather Warnings Card
                                FcmAlertsCard(
                                    weather = weather,
                                    theme = theme
                                )

                                // Dynamic Lottie Weather FX Interactive Showcase Card
                                WeatherLottieShowcaseCard(
                                    currentConditionCode = weather.conditionCode,
                                    theme = theme
                                )

                                // Real-Time Live Sync Status Indicator Card
                                RealTimeSyncCard(
                                    isSyncEnabled = isRealtimeSyncEnabled,
                                    syncIntervalSec = realtimeSyncIntervalSec,
                                    lastSyncTimestampMillis = lastSyncTimeMillis,
                                    isRefreshing = isRefreshing,
                                    theme = theme,
                                    onToggleSync = { enabled -> viewModel.setRealtimeSyncEnabled(enabled) },
                                    onSetIntervalSec = { sec -> viewModel.setRealtimeSyncIntervalSec(sec) },
                                    onForceSyncNow = { viewModel.refreshWeather() }
                                )

                                 // Daily Audio Forecast Summary Card
                                if (cardVisibility.showAudioSummary) {
                                    WeatherAudioSummaryCard(
                                        weather = weather,
                                        useCelsius = isCelsius,
                                        theme = theme,
                                        ttsManager = ttsManager
                                    )
                                }

                                // Gemini AI Smart Insights Personal Daily Weather Advice Card
                                if (cardVisibility.showSmartInsights) {
                                    SmartInsightsCard(
                                        insight = smartInsights,
                                        isLoading = isGeneratingInsights,
                                        theme = theme,
                                        onRefresh = { viewModel.generateSmartInsights(weather) }
                                    )
                                }

                                // 24-Hour Hourly Forecast
                                if (cardVisibility.showHourlyForecast) {
                                    HourlyForecastRow(
                                        hourlyList = weather.hourlyForecast,
                                        useCelsius = isCelsius,
                                        theme = theme
                                    )
                                }

                                // 7-Day Extended Forecast Vertical List
                                if (cardVisibility.showDailyForecast) {
                                    DailyForecastList(
                                        dailyList = weather.dailyForecast,
                                        useCelsius = isCelsius,
                                        theme = theme,
                                        currentTemp = if (isCelsius) weather.tempC else weather.tempF
                                    )
                                }

                                // 12-Hour Precipitation Probability & Animated Rain Intensity Card
                                if (cardVisibility.showPrecipitation) {
                                    PrecipitationCard(
                                        data = PrecipitationForecastData.generateFromWeather(weather),
                                        theme = theme
                                    )
                                }

                                // Recharts Temperature Trend Graph (Next 24 Hours)
                                if (cardVisibility.showTemperatureTrend) {
                                    TemperatureTrendGraphCard(
                                        weather = weather,
                                        theme = theme,
                                        isCelsius = isCelsius
                                    )
                                }

                                // Dedicated Wind Speed & Direction Animated Dashboard Component
                                if (cardVisibility.showWindDashboard) {
                                    WindDashboardCard(
                                        weather = weather,
                                        useCelsius = isCelsius,
                                        theme = theme
                                    )
                                }

                                // Dedicated Wind Chill Index Calculator Card (NWS Formula & Frostbite Risk)
                                if (cardVisibility.showWindChill) {
                                    WindChillCalculatorCard(
                                        weather = weather,
                                        theme = theme,
                                        isCelsius = isCelsius
                                    )
                                }

                                // Dedicated Thermal Comfort Index & Apparel Strategy Card
                                if (cardVisibility.showThermalComfort) {
                                    ThermalComfortCard(
                                        weather = weather,
                                        theme = theme,
                                        isCelsius = isCelsius
                                    )
                                }

                                // Dedicated Relative Humidity Circular Gauge & Dew Point Card
                                if (cardVisibility.showHumidityGauge) {
                                    HumidityGaugeCard(
                                        weather = weather,
                                        theme = theme,
                                        isCelsius = isCelsius
                                    )
                                }

                                // Dedicated Air Quality Index (AQI) Vector Gauge Component
                                if (cardVisibility.showAirQuality) {
                                    AirQualityGaugeCard(
                                        weather = weather,
                                        theme = theme
                                    )
                                }

                                // Dedicated Pollen Count & Allergy Risk Card (Open-Meteo Integration)
                                if (cardVisibility.showPollen) {
                                    PollenAllergyCard(
                                        city = weather.city,
                                        theme = theme
                                    )
                                }

                                // Dedicated UV Index Dashboard Component with animated gauge
                                if (cardVisibility.showUvIndex) {
                                    UvDashboardCard(
                                        weather = weather,
                                        theme = theme
                                    )
                                }

                                // Dedicated Moon Phase Tracking Component
                                if (cardVisibility.showMoonPhase) {
                                    MoonPhaseCard(
                                        weather = weather,
                                        theme = theme
                                    )
                                }

                                // Monthly Historical Climate Trends Module (Room Database)
                                if (cardVisibility.showClimateTrends) {
                                    MonthlyClimateTrendsCard(
                                        weather = weather,
                                        theme = theme,
                                        isCelsius = isCelsius
                                    )
                                }

                                // Long-Term Historical Climate Comparison Card (This Month vs Last Year & 5-Yr Baseline)
                                if (cardVisibility.showClimateComparison) {
                                    HistoricalClimateComparisonCard(
                                        weather = weather,
                                        theme = theme,
                                        isCelsius = isCelsius
                                    )
                                }

                                // Vertical Narrative Timeline Card (Daily Historical Weather Narrative)
                                if (cardVisibility.showNarrativeTimeline) {
                                    DailyWeatherNarrativeTimelineCard(
                                        weather = weather,
                                        theme = theme,
                                        isCelsius = isCelsius
                                    )
                                }

                                // Offline Weather Map & Radar Caching (Room Database Geographical Coordinates)
                                if (cardVisibility.showOfflineMap) {
                                    OfflineWeatherMapCard(
                                        weather = weather,
                                        theme = theme,
                                        isCelsius = isCelsius
                                    )
                                }

                                // Jetpack Glance Home Screen Widget Component & Sync Control
                                if (cardVisibility.showWidgetPreview) {
                                    WeatherWidgetPreviewCard(
                                        weather = weather,
                                        theme = theme,
                                        isCelsius = isCelsius,
                                        onToggleUnit = { viewModel.toggleUnit() }
                                    )
                                }

                                // GPS Daily Sunrise & Sunset Calculator Component
                                if (cardVisibility.showGpsSun) {
                                    GpsSunriseSunsetCalculatorCard(
                                        weather = weather,
                                        theme = theme,
                                        onRefreshGpsLocation = { triggerLocationFetch(false) }
                                    )
                                }

                                // Dedicated Sunrise & Sunset Solar Path Visualizer
                                if (cardVisibility.showSunCycle) {
                                    SunCycleCard(
                                        weather = weather,
                                        theme = theme
                                    )
                                }

                                // Detailed grid cards
                                if (cardVisibility.showStatsGrid) {
                                    WeatherStatsGrid(
                                        weather = weather,
                                        useCelsius = isCelsius,
                                        theme = theme
                                    )
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                        is WeatherState.Error -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error Icon",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(60.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Error Loading Weather",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.primaryTextColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    fontSize = 14.sp,
                                    color = theme.secondaryTextColor,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { 
                                            val queryToRetry = searchQuery.trim().ifBlank { "San Francisco" }
                                            viewModel.fetchWeather(queryToRetry) 
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (theme.primaryTextColor == Color.White) Color(0xFF0EA5E9) else Color(0xFF006495)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("error_retry_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Retry",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Retry Search")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.updateSearchQuery("San Francisco")
                                            viewModel.fetchWeather("San Francisco")
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("error_reset_button")
                                    ) {
                                        Text("Reset City", color = theme.primaryTextColor)
                                    }
                                }
                            }
                        }
                    }
                    }
                }
            }
        }
    }
    }
}

private fun shareWeatherSummary(
    context: android.content.Context,
    weather: com.example.data.repository.WeatherData,
    isCelsius: Boolean
) {
    val tempStr = if (isCelsius) "${weather.tempC.toInt()}°C (Feels like ${weather.feelsLikeC.toInt()}°C)"
                  else "${weather.tempF.toInt()}°F (Feels like ${weather.feelsLikeF.toInt()}°F)"
    
    val todayForecast = weather.dailyForecast.firstOrNull()
    val maxC = todayForecast?.maxTempC ?: weather.tempC
    val minC = todayForecast?.minTempC ?: (weather.tempC - 5)
    val maxF = todayForecast?.maxTempF ?: weather.tempF
    val minF = todayForecast?.minTempF ?: (weather.tempF - 9)

    val highLowStr = if (isCelsius) "High: ${maxC.toInt()}°C • Low: ${minC.toInt()}°C"
                     else "High: ${maxF.toInt()}°F • Low: ${minF.toInt()}°F"

    val shareText = """
        🌤️ Current Weather in ${weather.city}
        ----------------------------------
        📍 Location: ${weather.city}
        🌡️ Temperature: $tempStr
        📊 High / Low: $highLowStr
        ☁️ Condition: ${weather.conditionText}
        💧 Humidity: ${weather.humidity}% | 💨 Wind: ${weather.windKph.toInt()} km/h
        ☀️ UV Index: ${weather.uv.toInt()} | 🌫️ Air Quality (AQI): ${weather.aqi}
        
        Shared via Live Weather Tracker 📱
    """.trimIndent()

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Weather Report for ${weather.city}")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    val chooserIntent = Intent.createChooser(sendIntent, "Share Current Weather via")
    context.startActivity(chooserIntent)
}
