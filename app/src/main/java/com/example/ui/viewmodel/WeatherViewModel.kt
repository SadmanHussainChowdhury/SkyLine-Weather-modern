package com.example.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.FavoriteCityEntity
import com.example.data.local.SettingsDataStore
import com.example.data.repository.Resource
import com.example.data.repository.WeatherData
import com.example.data.repository.WeatherRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.location.GeocodingService
import com.example.data.location.GeofencingManager
import com.example.data.repository.SmartInsightData
import com.example.data.repository.SmartInsightsRepository
import kotlinx.coroutines.Dispatchers

sealed class WeatherState {
    object Idle : WeatherState()
    object Loading : WeatherState()
    data class Success(val weather: WeatherData) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

class WeatherViewModel(
    val repository: WeatherRepository,
    private val settingsDataStore: SettingsDataStore,
    val geofencingManager: GeofencingManager,
    private val smartInsightsRepository: SmartInsightsRepository
) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Idle)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    private val _smartInsightsState = MutableStateFlow<SmartInsightData?>(null)
    val smartInsightsState: StateFlow<SmartInsightData?> = _smartInsightsState.asStateFlow()

    private val _isGeneratingInsights = MutableStateFlow(false)
    val isGeneratingInsights: StateFlow<Boolean> = _isGeneratingInsights.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _lastSyncTimeMillis = MutableStateFlow<Long>(System.currentTimeMillis())
    val lastSyncTimeMillis: StateFlow<Long> = _lastSyncTimeMillis.asStateFlow()

    val realtimeSyncEnabled: StateFlow<Boolean> = settingsDataStore.realtimeSyncEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val realtimeSyncIntervalSec: StateFlow<Int> = settingsDataStore.realtimeSyncIntervalSecFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60)

    val isCelsius: StateFlow<Boolean> = settingsDataStore.useCelsiusFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val favorites: StateFlow<List<FavoriteCityEntity>> = repository.getFavoriteCities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSearches: StateFlow<List<com.example.data.local.RecentSearchEntity>> = repository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customApiKey: StateFlow<String> = settingsDataStore.customApiKeyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val themeMode: StateFlow<String> = settingsDataStore.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val aestheticTheme: StateFlow<String> = settingsDataStore.aestheticThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "midnight_blue")

    val pinnedWidgetCity: StateFlow<String> = settingsDataStore.pinnedWidgetCityFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "San Francisco")

    val widgetShowUv: StateFlow<Boolean> = settingsDataStore.widgetShowUvFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val widgetShowAqi: StateFlow<Boolean> = settingsDataStore.widgetShowAqiFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val widgetShowHumidity: StateFlow<Boolean> = settingsDataStore.widgetShowHumidityFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val widgetShowWind: StateFlow<Boolean> = settingsDataStore.widgetShowWindFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val widgetShowFeelsLike: StateFlow<Boolean> = settingsDataStore.widgetShowFeelsLikeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val cardVisibility: StateFlow<com.example.data.local.HomeCardVisibility> = settingsDataStore.homeCardVisibilityFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.local.HomeCardVisibility())

    fun setCardVisibility(cardKey: String, show: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setCardVisibility(cardKey, show)
        }
    }

    fun setAllCardsVisibility(show: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAllCardsVisibility(show)
        }
    }

    fun applyPresetMinimalist() {
        viewModelScope.launch {
            settingsDataStore.applyPresetMinimalist()
        }
    }

    fun setRealtimeSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setRealtimeSyncEnabled(enabled)
            if (enabled) {
                refreshWeather()
            }
        }
    }

    fun setRealtimeSyncIntervalSec(seconds: Int) {
        viewModelScope.launch {
            settingsDataStore.setRealtimeSyncIntervalSec(seconds)
            if (realtimeSyncEnabled.value) {
                refreshWeather()
            }
        }
    }

    init {
        // Automatically load the last viewed city when ViewModel is created
        viewModelScope.launch {
            val lastCity = settingsDataStore.lastCityFlow.first()
            val initialCity = if (lastCity.isNotBlank()) lastCity else "San Francisco"
            _searchQuery.value = initialCity
            fetchWeather(initialCity)
        }
        // Start background polling to automatically refresh local conditions in real time
        startWeatherPolling()
    }

    private fun startWeatherPolling() {
        viewModelScope.launch {
            while (true) {
                val isEnabled = realtimeSyncEnabled.value
                val intervalSec = realtimeSyncIntervalSec.value.coerceAtLeast(10)
                val delayMs = if (isEnabled) intervalSec * 1000L else 15 * 60 * 1000L
                kotlinx.coroutines.delay(delayMs)
                if (realtimeSyncEnabled.value) {
                    val currentState = _weatherState.value
                    if (currentState is WeatherState.Success) {
                        fetchWeather(currentState.weather.city, forceRefresh = true)
                    } else {
                        val lastCity = settingsDataStore.lastCityFlow.first()
                        if (lastCity.isNotBlank()) {
                            fetchWeather(lastCity, forceRefresh = true)
                        }
                    }
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleUnit() {
        viewModelScope.launch {
            val current = isCelsius.value
            settingsDataStore.setUseCelsius(!current)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsDataStore.setThemeMode(mode)
        }
    }

    fun setAestheticTheme(theme: String) {
        viewModelScope.launch {
            settingsDataStore.setAestheticTheme(theme)
        }
    }

    fun saveCustomApiKey(key: String) {
        viewModelScope.launch {
            settingsDataStore.setCustomApiKey(key)
            // Reload weather with the new API key if success
            val currentState = _weatherState.value
            if (currentState is WeatherState.Success) {
                fetchWeather(currentState.weather.city, forceRefresh = true)
            }
        }
    }

    fun toggleFavorite(context: Context, city: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val alreadyFavorite = repository.isFavoriteCity(city)
            if (alreadyFavorite) {
                repository.removeFavoriteCity(city)
                geofencingManager.removeGeofence(city)
                _isFavorite.value = false
            } else {
                // Forward geocode city name to retrieve coordinates
                val coords = GeocodingService.getCoordinatesFromCity(context, city)
                repository.addFavoriteCity(city, coords.first, coords.second)
                geofencingManager.addGeofence(city, coords.first, coords.second)
                _isFavorite.value = true
            }
        }
    }

    fun checkIsFavorite(city: String) {
        viewModelScope.launch {
            _isFavorite.value = repository.isFavoriteCity(city)
        }
    }

    fun removeRecentSearch(cityName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecentSearch(cityName)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearRecentSearches()
        }
    }

    suspend fun exportUserDataJson(): String {
        return repository.exportUserDataJson()
    }

    suspend fun importUserDataJson(jsonString: String): Result<com.example.data.repository.UserDataMigrationResult> {
        return repository.importUserDataJson(jsonString)
    }

    fun toggleWidgetMetric(context: Context, metricKey: String, show: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setWidgetShowMetric(metricKey, show)
            val city = pinnedWidgetCity.value
            if (city.isNotBlank()) {
                setPinnedWidgetCity(context, city)
            }
        }
    }

    fun setPinnedWidgetCity(context: android.content.Context, cityName: String) {
        val cleanCity = cityName.trim()
        if (cleanCity.isBlank()) return
        viewModelScope.launch {
            settingsDataStore.setPinnedWidgetCity(cleanCity)
            val apiKey = customApiKey.value
            repository.getWeather(cleanCity, forceRefresh = false, userApiKey = apiKey).collect { resource ->
                if (resource is com.example.data.repository.Resource.Success) {
                    val w = resource.data
                    val highC = w.dailyForecast.firstOrNull()?.maxTempC ?: w.tempC
                    val lowC = w.dailyForecast.firstOrNull()?.minTempC ?: w.tempC
                    com.example.widget.WeatherGlanceWidget.updateAllWidgets(
                        context = context,
                        cityName = w.city,
                        tempC = w.tempC,
                        tempF = w.tempF,
                        condition = w.conditionText,
                        isCelsius = isCelsius.value,
                        highC = highC,
                        lowC = lowC,
                        uv = w.uv,
                        aqi = w.aqi,
                        humidity = w.humidity,
                        windKph = w.windKph,
                        feelsLikeC = w.feelsLikeC,
                        feelsLikeF = w.feelsLikeF
                    )
                }
            }
        }
    }

    fun refreshWeather() {
        val currentState = _weatherState.value
        if (currentState is WeatherState.Success) {
            fetchWeather(currentState.weather.city, forceRefresh = true)
        }
    }

    fun fetchWeather(city: String, forceRefresh: Boolean = false) {
        val cleanCity = city.trim()
        if (cleanCity.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.addRecentSearch(cleanCity)
        }
        viewModelScope.launch {
            if (forceRefresh) {
                _isRefreshing.value = true
            } else {
                _weatherState.value = WeatherState.Loading
            }

            val apiKey = customApiKey.value
            repository.getWeather(cleanCity, forceRefresh, apiKey).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        if (!forceRefresh) {
                            _weatherState.value = WeatherState.Loading
                        }
                    }
                    is Resource.Success -> {
                        _weatherState.value = WeatherState.Success(resource.data)
                        _lastSyncTimeMillis.value = System.currentTimeMillis()
                        _isRefreshing.value = false
                        _searchQuery.value = resource.data.city
                        checkIsFavorite(resource.data.city)
                        // Fetch Smart Insights using Gemini AI
                        generateSmartInsights(resource.data)
                        // Save last viewed city
                        settingsDataStore.setLastCity(resource.data.city)
                        // Automatically save frequently searched cities to local database
                        viewModelScope.launch(Dispatchers.IO) {
                            repository.addFavoriteCity(resource.data.city)
                        }
                    }
                    is Resource.Error -> {
                        _weatherState.value = WeatherState.Error(resource.message)
                        _isRefreshing.value = false
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchWeatherFromGps(context: Context, onPermissionDenied: () -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        _weatherState.value = WeatherState.Loading

        val handleLocationResult = { location: Location? ->
            if (location != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    val cityName = GeocodingService.getCityFromLocation(context, location.latitude, location.longitude)
                    _searchQuery.value = cityName
                    fetchWeather(cityName)
                }
            } else {
                // Try last location from Fused Location Provider
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                    if (lastLoc != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            val cityName = GeocodingService.getCityFromLocation(context, lastLoc.latitude, lastLoc.longitude)
                            _searchQuery.value = cityName
                            fetchWeather(cityName)
                        }
                    } else {
                        // Fallback to system LocationManager providers (Network / GPS / Passive)
                        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
                        val netLoc = try { locationManager?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER) } catch (e: SecurityException) { null }
                        val gpsLoc = try { locationManager?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER) } catch (e: SecurityException) { null }
                        val passLoc = try { locationManager?.getLastKnownLocation(android.location.LocationManager.PASSIVE_PROVIDER) } catch (e: SecurityException) { null }

                        val systemLoc = netLoc ?: gpsLoc ?: passLoc
                        if (systemLoc != null) {
                            viewModelScope.launch(Dispatchers.IO) {
                                val cityName = GeocodingService.getCityFromLocation(context, systemLoc.latitude, systemLoc.longitude)
                                _searchQuery.value = cityName
                                fetchWeather(cityName)
                            }
                        } else {
                            // Call fallback trigger (e.g. Browser Geolocation or default location)
                            onPermissionDenied()
                        }
                    }
                }.addOnFailureListener {
                    onPermissionDenied()
                }
            }
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc -> handleLocationResult(loc) }
            .addOnFailureListener {
                // Try balanced power accuracy if high accuracy fails
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { loc -> handleLocationResult(loc) }
                    .addOnFailureListener { onPermissionDenied() }
            }
    }

    fun fetchWeatherFromCoordinates(context: Context, latitude: Double, longitude: Double) {
        _weatherState.value = WeatherState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val cityName = GeocodingService.getCityFromLocation(context, latitude, longitude)
            _searchQuery.value = cityName
            fetchWeather(cityName)
        }
    }

    fun generateSmartInsights(weather: WeatherData) {
        viewModelScope.launch {
            _isGeneratingInsights.value = true
            val insight = smartInsightsRepository.fetchSmartInsights(weather, isCelsius.value)
            _smartInsightsState.value = insight
            _isGeneratingInsights.value = false
        }
    }

    // Factory pattern to inject Repository and DataStore dependencies
    class Factory(
        private val repository: WeatherRepository,
        private val settingsDataStore: SettingsDataStore,
        private val geofencingManager: GeofencingManager,
        private val smartInsightsRepository: SmartInsightsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
                return WeatherViewModel(repository, settingsDataStore, geofencingManager, smartInsightsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
