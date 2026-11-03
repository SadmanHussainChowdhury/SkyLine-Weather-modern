package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveWeatherMapScreen
import com.example.ui.screens.SavedLocationsWidgetConfigScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmartWidgetsScreen
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Map
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WeatherViewModel

enum class Screen {
    Home, Map, Favorites, WidgetConfig, Settings, SmartWidgets
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Access the app-wide Container
        val appContainer = (applicationContext as WeatherApplication).container
        val viewModelFactory = WeatherViewModel.Factory(
            repository = appContainer.weatherRepository,
            settingsDataStore = appContainer.settingsDataStore,
            geofencingManager = appContainer.geofencingManager,
            smartInsightsRepository = appContainer.smartInsightsRepository
        )

        setContent {
            val viewModel: WeatherViewModel = viewModel(factory = viewModelFactory)
            val themeMode by viewModel.themeMode.collectAsState()
            val aestheticTheme by viewModel.aestheticTheme.collectAsState()

            MyApplicationTheme(themeMode = themeMode, aestheticTheme = aestheticTheme) {
                var currentScreen by remember { mutableStateOf(Screen.Home) }
                var mapFocusedCity by remember { mutableStateOf<String?>(null) }

                // Detect screen width class dynamically for adaptive layout support
                val configuration = LocalConfiguration.current
                val isTablet = configuration.screenWidthDp >= 600

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (!isTablet) {
                            BottomNavBar(
                                currentScreen = currentScreen,
                                onScreenSelected = { currentScreen = it }
                            )
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (isTablet) {
                            SideNavRail(
                                currentScreen = currentScreen,
                                onScreenSelected = { currentScreen = it }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            when (currentScreen) {
                                Screen.Home -> HomeScreen(
                                    viewModel = viewModel,
                                    onOpenMap = { city ->
                                        mapFocusedCity = city
                                        currentScreen = Screen.Map
                                    },
                                    onOpenSmartWidgets = {
                                        currentScreen = Screen.SmartWidgets
                                    }
                                )
                                Screen.Map -> LiveWeatherMapScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.Home },
                                    initialCityName = mapFocusedCity
                                )
                                Screen.Favorites -> FavoritesScreen(
                                    viewModel = viewModel,
                                    onCitySelected = { city ->
                                        currentScreen = Screen.Home
                                    },
                                    onOpenWidgetConfig = {
                                        currentScreen = Screen.WidgetConfig
                                    }
                                )
                                Screen.WidgetConfig -> SavedLocationsWidgetConfigScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        currentScreen = Screen.Favorites
                                    }
                                )
                                Screen.Settings -> SettingsScreen(
                                    viewModel = viewModel,
                                    onOpenWidgetConfig = {
                                        currentScreen = Screen.WidgetConfig
                                    },
                                    onOpenSmartWidgets = {
                                        currentScreen = Screen.SmartWidgets
                                    }
                                )
                                Screen.SmartWidgets -> SmartWidgetsScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        currentScreen = Screen.Home
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.Home,
            onClick = { onScreenSelected(Screen.Home) },
            modifier = Modifier.testTag("nav_home_tab"),
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.Home) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home Tab Icon"
                )
            },
            label = { Text("Weather", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            selected = currentScreen == Screen.Map,
            onClick = { onScreenSelected(Screen.Map) },
            modifier = Modifier.testTag("nav_map_tab"),
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.Map) Icons.Filled.Map else Icons.Outlined.Map,
                    contentDescription = "Live Weather Map Icon"
                )
            },
            label = { Text("Radar Map", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0284C7),
                selectedTextColor = Color(0xFF0284C7),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            selected = currentScreen == Screen.Favorites,
            onClick = { onScreenSelected(Screen.Favorites) },
            modifier = Modifier.testTag("nav_favorites_tab"),
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.Favorites) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorites Tab Icon"
                )
            },
            label = { Text("Saved", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFFB300), // Warm amber star
                selectedTextColor = Color(0xFFFFB300),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            selected = currentScreen == Screen.WidgetConfig,
            onClick = { onScreenSelected(Screen.WidgetConfig) },
            modifier = Modifier.testTag("nav_widget_tab"),
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.WidgetConfig) Icons.Filled.Widgets else Icons.Outlined.Widgets,
                    contentDescription = "Widget Config Tab Icon"
                )
            },
            label = { Text("Widget", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            selected = currentScreen == Screen.Settings,
            onClick = { onScreenSelected(Screen.Settings) },
            modifier = Modifier.testTag("nav_settings_tab"),
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.Settings) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings Tab Icon"
                )
            },
            label = { Text("Preferences", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun SideNavRail(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxHeight()
            .testTag("side_nav_rail")
    ) {
        NavigationRailItem(
            selected = currentScreen == Screen.Home,
            onClick = { onScreenSelected(Screen.Home) },
            modifier = Modifier.testTag("rail_home_tab"),
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.Home) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home Tab Icon"
                )
            },
            label = { Text("Weather", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationRailItem(
            selected = currentScreen == Screen.Map,
            onClick = { onScreenSelected(Screen.Map) },
            modifier = Modifier.testTag("rail_map_tab"),
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.Map) Icons.Filled.Map else Icons.Outlined.Map,
                    contentDescription = "Live Weather Map Icon"
                )
            },
            label = { Text("Radar Map", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = Color(0xFF0284C7),
                selectedTextColor = Color(0xFF0284C7),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationRailItem(
            selected = currentScreen == Screen.Favorites,
            onClick = { onScreenSelected(Screen.Favorites) },
            modifier = Modifier.testTag("rail_favorites_tab"),
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.Favorites) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorites Tab Icon"
                )
            },
            label = { Text("Saved", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = Color(0xFFFFB300),
                selectedTextColor = Color(0xFFFFB300),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationRailItem(
            selected = currentScreen == Screen.WidgetConfig,
            onClick = { onScreenSelected(Screen.WidgetConfig) },
            modifier = Modifier.testTag("rail_widget_tab"),
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.WidgetConfig) Icons.Filled.Widgets else Icons.Outlined.Widgets,
                    contentDescription = "Widget Config Tab Icon"
                )
            },
            label = { Text("Widget", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationRailItem(
            selected = currentScreen == Screen.Settings,
            onClick = { onScreenSelected(Screen.Settings) },
            modifier = Modifier.testTag("rail_settings_tab"),
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.Settings) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings Tab Icon"
                )
            },
            label = { Text("Preferences", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
