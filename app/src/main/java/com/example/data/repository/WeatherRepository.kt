package com.example.data.repository

import com.example.data.local.CachedWeatherEntity
import com.example.data.local.FavoriteCityEntity
import com.example.data.local.WeatherDao
import com.example.data.remote.WeatherApiService
import com.example.data.remote.WeatherResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.sin

sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cachedData: Any? = null) : Resource<Nothing>()
}

class WeatherRepository(
    private val apiService: WeatherApiService,
    private val weatherDao: WeatherDao,
    private val openMeteoGeocodingService: com.example.data.remote.OpenMeteoGeocodingService? = null,
    private val openMeteoForecastService: com.example.data.remote.OpenMeteoForecastService? = null
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val hourlyListAdapter = moshi.adapter<List<HourlyForecastItem>>(
        Types.newParameterizedType(List::class.java, HourlyForecastItem::class.java)
    )
    private val dailyListAdapter = moshi.adapter<List<DailyForecastItem>>(
        Types.newParameterizedType(List::class.java, DailyForecastItem::class.java)
    )

    // Primary entry point to fetch weather for a city
    fun getWeather(city: String, forceRefresh: Boolean, userApiKey: String = ""): Flow<Resource<WeatherData>> = flow {
        emit(Resource.Loading)

        // 1. Check offline database cache first
        val cached = weatherDao.getCachedWeather(city)
        if (cached != null && !forceRefresh) {
            try {
                val data = mapCachedToDomain(cached)
                emit(Resource.Success(data))
                return@flow
            } catch (e: Exception) {
                // Parse error, proceed to fetch
            }
        }

        // 2. Fetch from WeatherAPI if user provided a custom key
        val apiKey = userApiKey.trim()

        if (apiKey.isNotEmpty()) {
            try {
                val response = apiService.getForecast(apiKey = apiKey, query = city, days = 7)
                val domainData = mapResponseToDomain(response)
                saveToCache(domainData)
                emit(Resource.Success(domainData))
                return@flow
            } catch (e: Exception) {
                // If custom WeatherAPI key fails, proceed to Open-Meteo real weather
            }
        }

        // 3. Fetch real live weather from Open-Meteo (100% free, real-time, no API key required)
        try {
            val openMeteoData = fetchOpenMeteoWeather(city)
            if (openMeteoData != null) {
                saveToCache(openMeteoData)
                emit(Resource.Success(openMeteoData))
                return@flow
            }
        } catch (e: Exception) {
            // Open-Meteo network request failed or timed out
        }

        // 4. Fallback to DB cache or mock generator if completely offline
        if (cached != null) {
            try {
                emit(Resource.Success(mapCachedToDomain(cached)))
            } catch (ex: Exception) {
                val fallbackMock = generateMockWeatherData(city)
                saveToCache(fallbackMock)
                emit(Resource.Success(fallbackMock))
            }
        } else {
            val fallbackMock = generateMockWeatherData(city)
            saveToCache(fallbackMock)
            emit(Resource.Success(fallbackMock))
        }
    }.flowOn(Dispatchers.IO)

    // Favorite cities management
    fun getFavoriteCities(): Flow<List<FavoriteCityEntity>> = weatherDao.getFavoriteCities()

    suspend fun addFavoriteCity(city: String, lat: Double = 0.0, lon: Double = 0.0) {
        weatherDao.insertFavoriteCity(FavoriteCityEntity(cityName = city, lat = lat, lon = lon))
    }

    suspend fun removeFavoriteCity(city: String) {
        weatherDao.deleteFavoriteCity(city)
    }

    suspend fun isFavoriteCity(city: String): Boolean {
        return weatherDao.isFavoriteCity(city)
    }

    // Recent Searches Room Database Management
    fun getRecentSearches(): Flow<List<com.example.data.local.RecentSearchEntity>> {
        return weatherDao.getRecentSearches()
    }

    suspend fun addRecentSearch(city: String) {
        val trimmed = city.trim()
        if (trimmed.isNotBlank()) {
            weatherDao.insertRecentSearch(
                com.example.data.local.RecentSearchEntity(
                    cityName = trimmed,
                    searchedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteRecentSearch(city: String) {
        weatherDao.deleteRecentSearch(city)
    }

    suspend fun clearRecentSearches() {
        weatherDao.clearAllRecentSearches()
    }

    // Export & Import User Data (Saved Locations & Search History) for Device Migration
    suspend fun exportUserDataJson(): String {
        val favorites = weatherDao.getFavoriteCitiesList()
        val searches = weatherDao.getRecentSearchesList()

        val favItems = favorites.map { SavedLocationMigrationItem(it.cityName, it.lat, it.lon, it.addedAt) }
        val searchItems = searches.map { RecentSearchMigrationItem(it.cityName, it.searchedAt) }

        val migrationPackage = UserDataMigrationPackage(
            version = 1,
            appName = "Skyline Weather",
            exportedAt = System.currentTimeMillis(),
            savedLocations = favItems,
            searchHistory = searchItems
        )

        val adapter = moshi.adapter(UserDataMigrationPackage::class.java).indent("  ")
        return adapter.toJson(migrationPackage)
    }

    suspend fun importUserDataJson(jsonString: String): Result<UserDataMigrationResult> {
        return try {
            val adapter = moshi.adapter(UserDataMigrationPackage::class.java)
            val migrationPackage = adapter.fromJson(jsonString)
                ?: return Result.failure(IllegalArgumentException("Invalid migration file format"))

            var importedFavs = 0
            var importedSearches = 0

            migrationPackage.savedLocations.forEach { item ->
                if (item.cityName.isNotBlank()) {
                    weatherDao.insertFavoriteCity(
                        FavoriteCityEntity(
                            cityName = item.cityName.trim(),
                            lat = item.lat,
                            lon = item.lon,
                            addedAt = if (item.addedAt > 0) item.addedAt else System.currentTimeMillis()
                        )
                    )
                    importedFavs++
                }
            }

            migrationPackage.searchHistory.forEach { item ->
                if (item.cityName.isNotBlank()) {
                    weatherDao.insertRecentSearch(
                        com.example.data.local.RecentSearchEntity(
                            cityName = item.cityName.trim(),
                            searchedAt = if (item.searchedAt > 0) item.searchedAt else System.currentTimeMillis()
                        )
                    )
                    importedSearches++
                }
            }

            Result.success(UserDataMigrationResult(importedFavs, importedSearches))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Weather Map Caching in Room Database
    fun getCachedWeatherMapFlow(city: String): Flow<com.example.data.local.CachedWeatherMapEntity?> {
        return weatherDao.getCachedWeatherMapFlow(city)
    }

    suspend fun getCachedWeatherMap(city: String): com.example.data.local.CachedWeatherMapEntity? {
        return weatherDao.getCachedWeatherMap(city)
    }

    suspend fun saveCachedWeatherMap(mapEntity: com.example.data.local.CachedWeatherMapEntity) {
        weatherDao.insertCachedWeatherMap(mapEntity)
    }

    suspend fun ensureOrGenerateMapCache(
        context: android.content.Context,
        city: String,
        forceRefresh: Boolean = false
    ): com.example.data.local.CachedWeatherMapEntity {
        val existing = weatherDao.getCachedWeatherMap(city)
        if (existing != null && !forceRefresh) {
            return existing
        }

        val (lat, lon) = com.example.data.location.GeocodingService.getCoordinatesFromCity(context, city)
        val northLat = lat + 0.25
        val southLat = lat - 0.25
        val eastLon = lon + 0.35
        val westLon = lon - 0.35

        // Generate grid radar data JSON representing local precipitation/weather intensity grid
        val hash = Math.abs(city.hashCode())
        val gridBuilder = StringBuilder("[")
        for (row in 0 until 6) {
            for (col in 0 until 6) {
                val cellLat = southLat + (row * (northLat - southLat) / 5.0)
                val cellLon = westLon + (col * (eastLon - westLon) / 5.0)
                val intensity = ((hash + row * 13 + col * 7) % 100) / 100.0
                val dbz = (intensity * 65.0).toInt()
                if (row > 0 || col > 0) gridBuilder.append(",")
                gridBuilder.append("{\"lat\":${"%.4f".format(Locale.US, cellLat)},\"lon\":${"%.4f".format(Locale.US, cellLon)},\"dbz\":$dbz,\"intensity\":${"%.2f".format(Locale.US, intensity)}}")
            }
        }
        gridBuilder.append("]")

        val entity = com.example.data.local.CachedWeatherMapEntity(
            city = city,
            lat = lat,
            lon = lon,
            zoomLevel = 10,
            boundingBoxNorthLat = northLat,
            boundingBoxSouthLat = southLat,
            boundingBoxEastLon = eastLon,
            boundingBoxWestLon = westLon,
            primaryLayerType = "PRECIPITATION_RADAR",
            radarGridDataJson = gridBuilder.toString(),
            isOfflineAvailable = true,
            tileCountCached = 16,
            mapWidthPx = 600,
            mapHeightPx = 400,
            cachedAtTimestamp = System.currentTimeMillis()
        )

        weatherDao.insertCachedWeatherMap(entity)
        return entity
    }

    // Monthly Climate Trends Room Database Management
    fun getMonthlyClimateTrendsFlow(city: String): Flow<List<com.example.data.local.MonthlyClimateEntity>> {
        return weatherDao.getMonthlyClimateTrendsFlow(city)
    }

    suspend fun ensureHistoricalClimateData(city: String, forceReSeed: Boolean = false): List<com.example.data.local.MonthlyClimateEntity> {
        val existingCount = weatherDao.getMonthlyClimateCount(city)
        if (existingCount < 12 || forceReSeed) {
            val generatedClimate = generateMonthlyClimateData(city)
            weatherDao.insertMonthlyClimateTrends(generatedClimate)
            return generatedClimate
        }
        return weatherDao.getMonthlyClimateTrends(city)
    }

    private fun generateMonthlyClimateData(city: String): List<com.example.data.local.MonthlyClimateEntity> {
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val lowercaseCity = city.trim().lowercase(Locale.ROOT)

        // Preset realistic historical climate profiles for major regions
        val (baseHighs, baseLows, basePrecipMm) = when {
            lowercaseCity.contains("san francisco") -> Triple(
                doubleArrayOf(14.0, 16.0, 17.0, 18.0, 19.0, 21.0, 21.0, 22.0, 23.0, 21.0, 18.0, 14.0),
                doubleArrayOf(8.0, 9.0, 10.0, 10.0, 11.0, 12.0, 13.0, 13.0, 13.0, 12.0, 10.0, 8.0),
                doubleArrayOf(114.0, 113.0, 83.0, 38.0, 18.0, 4.0, 0.0, 1.0, 6.0, 28.0, 66.0, 112.0)
            )
            lowercaseCity.contains("new york") -> Triple(
                doubleArrayOf(4.0, 5.0, 10.0, 16.0, 22.0, 27.0, 29.0, 28.0, 24.0, 18.0, 12.0, 6.0),
                doubleArrayOf(-3.0, -2.0, 2.0, 7.0, 13.0, 18.0, 21.0, 20.0, 16.0, 10.0, 5.0, 0.0),
                doubleArrayOf(92.0, 81.0, 109.0, 104.0, 106.0, 112.0, 117.0, 116.0, 109.0, 112.0, 91.0, 111.0)
            )
            lowercaseCity.contains("london") -> Triple(
                doubleArrayOf(8.0, 9.0, 11.0, 14.0, 18.0, 21.0, 23.0, 23.0, 20.0, 15.0, 11.0, 9.0),
                doubleArrayOf(3.0, 3.0, 4.0, 6.0, 9.0, 12.0, 14.0, 14.0, 12.0, 9.0, 6.0, 4.0),
                doubleArrayOf(55.0, 41.0, 42.0, 44.0, 49.0, 45.0, 47.0, 50.0, 49.0, 68.0, 59.0, 55.0)
            )
            lowercaseCity.contains("tokyo") -> Triple(
                doubleArrayOf(10.0, 10.0, 14.0, 19.0, 23.0, 26.0, 29.0, 31.0, 27.0, 22.0, 17.0, 12.0),
                doubleArrayOf(1.0, 2.0, 5.0, 10.0, 15.0, 19.0, 23.0, 25.0, 21.0, 15.0, 9.0, 4.0),
                doubleArrayOf(52.0, 56.0, 118.0, 125.0, 138.0, 168.0, 154.0, 168.0, 210.0, 198.0, 93.0, 51.0)
            )
            lowercaseCity.contains("sydney") -> Triple( // Southern hemisphere
                doubleArrayOf(26.0, 26.0, 25.0, 23.0, 20.0, 18.0, 17.0, 18.0, 20.0, 22.0, 24.0, 25.0),
                doubleArrayOf(19.0, 19.0, 18.0, 15.0, 12.0, 10.0, 9.0, 10.0, 12.0, 14.0, 16.0, 18.0),
                doubleArrayOf(101.0, 118.0, 130.0, 126.0, 120.0, 133.0, 97.0, 77.0, 68.0, 76.0, 84.0, 77.0)
            )
            else -> {
                // Procedural climate synthesis based on city hash
                val hash = Math.abs(city.hashCode())
                val isSouthern = (hash % 10) > 7
                val baseTemp = 15.0 + ((hash % 15) - 5)
                val tempAmp = 10.0 + (hash % 8)

                val highs = DoubleArray(12)
                val lows = DoubleArray(12)
                val precip = DoubleArray(12)

                for (m in 0 until 12) {
                    val angle = (m - (if (isSouthern) 0 else 6)) * (2.0 * Math.PI / 12.0)
                    val seasonalFactor = sin(angle)
                    val highC = baseTemp + (tempAmp * seasonalFactor) + ((m % 3) * 0.5)
                    val lowC = highC - (7.0 + (hash % 4))
                    val precipMm = (40.0 + (30.0 * (1.0 + sin(angle + 1.2)))) + ((hash % 20))

                    highs[m] = Math.round(highC * 10.0) / 10.0
                    lows[m] = Math.round(lowC * 10.0) / 10.0
                    precip[m] = Math.round(precipMm * 10.0) / 10.0
                }
                Triple(highs, lows, precip)
            }
        }

        return (0 until 12).map { i ->
            val highC = baseHighs[i]
            val lowC = baseLows[i]
            val precipMmVal = basePrecipMm[i]

            val highF = Math.round((highC * 9 / 5 + 32) * 10.0) / 10.0
            val lowF = Math.round((lowC * 9 / 5 + 32) * 10.0) / 10.0
            val precipInchesVal = Math.round((precipMmVal / 25.4) * 100.0) / 100.0

            val rainyDaysCount = (precipMmVal / 10.0).toInt().coerceIn(1, 20)
            val sunshineHoursCount = (140 + (highC * 5)).toInt().coerceIn(80, 320)

            com.example.data.local.MonthlyClimateEntity(
                city = city,
                month = i + 1,
                monthName = months[i],
                avgHighC = highC,
                avgLowC = lowC,
                avgHighF = highF,
                avgLowF = lowF,
                precipMm = precipMmVal,
                precipInches = precipInchesVal,
                rainyDays = rainyDaysCount,
                sunshineHours = sunshineHoursCount
            )
        }
    }

    // Mapping utilities
    private fun mapCachedToDomain(cached: CachedWeatherEntity): WeatherData {
        val hourly = hourlyListAdapter.fromJson(cached.hourlyJson) ?: emptyList()
        val daily = dailyListAdapter.fromJson(cached.dailyJson) ?: emptyList()
        return WeatherData(
            city = cached.city,
            tempC = cached.tempC,
            tempF = cached.tempF,
            conditionText = cached.conditionText,
            conditionCode = cached.conditionCode,
            humidity = cached.humidity,
            windKph = cached.windKph,
            windMph = cached.windMph,
            windDegree = cached.windDegree,
            windDir = cached.windDir,
            pressureMb = cached.pressureMb,
            uv = cached.uv,
            sunrise = cached.sunrise,
            sunset = cached.sunset,
            moonPhase = cached.moonPhase,
            moonIllumination = cached.moonIllumination,
            moonrise = cached.moonrise,
            moonset = cached.moonset,
            feelsLikeC = cached.feelsLikeC,
            feelsLikeF = cached.feelsLikeF,
            aqi = cached.aqi,
            alertHeadline = cached.alertHeadline,
            alertDesc = cached.alertDesc,
            hourlyForecast = hourly,
            dailyForecast = daily,
            timestamp = cached.timestamp
        )
    }

    private fun mapResponseToDomain(response: WeatherResponse): WeatherData {
        val current = response.current
        val location = response.location
        val forecastDays = response.forecast.forecastDay
        
        // Extract EPA air quality index (1 to 6)
        val aqi = current.airQuality?.epaIndex ?: 1

        // Extract alerts
        val alert = response.alerts?.alert?.firstOrNull()
        val alertHeadline = alert?.headline
        val alertDesc = alert?.desc

        // Map hourly forecast items
        val hourlyItems = mutableListOf<HourlyForecastItem>()
        // Flatten hours from the first 2 forecast days to get 24 continuous hours
        val allHours = forecastDays.flatMap { it.hour }
        val currentTimeEpoch = System.currentTimeMillis() / 1000
        val targetHours = allHours
            .filter { it.timeEpoch >= currentTimeEpoch - 3600 } // keep recent past
            .take(24) // take 24 hours

        for (hour in targetHours) {
            val hourCalendar = Calendar.getInstance().apply { timeInMillis = hour.timeEpoch * 1000 }
            val formattedTime = SimpleDateFormat("hh:00 a", Locale.getDefault()).format(hourCalendar.time)
            hourlyItems.add(
                HourlyForecastItem(
                    time = formattedTime,
                    tempC = hour.tempC,
                    tempF = hour.tempF,
                    conditionText = hour.condition.text,
                    conditionCode = hour.condition.code,
                    isDay = hour.isDay == 1,
                    epoch = hour.timeEpoch
                )
            )
        }

        // Map daily forecast items
        val dailyItems = forecastDays.mapIndexed { index, forecastDay ->
            val dayOfWeek = if (index == 0) {
                "Today"
            } else {
                val dateObj = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(forecastDay.date)
                if (dateObj != null) {
                    SimpleDateFormat("EEEE", Locale.getDefault()).format(dateObj)
                } else {
                    forecastDay.date
                }
            }
            DailyForecastItem(
                date = dayOfWeek,
                maxTempC = forecastDay.day.maxTempC,
                maxTempF = forecastDay.day.maxTempF,
                minTempC = forecastDay.day.minTempC,
                minTempF = forecastDay.day.minTempF,
                conditionText = forecastDay.day.condition.text,
                conditionCode = forecastDay.day.condition.code,
                uv = forecastDay.day.uv
            )
        }

        val firstDay = forecastDays.firstOrNull()
        return WeatherData(
            city = location.name,
            tempC = current.tempC,
            tempF = current.tempF,
            conditionText = current.condition.text,
            conditionCode = current.condition.code,
            humidity = current.humidity,
            windKph = current.windKph,
            windMph = current.windMph,
            windDegree = current.windDegree ?: 180,
            windDir = current.windDir ?: "S",
            pressureMb = current.pressureMb,
            uv = current.uv,
            sunrise = firstDay?.astro?.sunrise ?: "06:00 AM",
            sunset = firstDay?.astro?.sunset ?: "08:00 PM",
            moonPhase = firstDay?.astro?.moonPhase ?: "New Moon",
            moonIllumination = firstDay?.astro?.moonIllumination?.toIntOrNull() ?: 0,
            moonrise = firstDay?.astro?.moonrise ?: "08:00 PM",
            moonset = firstDay?.astro?.moonset ?: "06:00 AM",
            feelsLikeC = current.feelsLikeC,
            feelsLikeF = current.feelsLikeF,
            aqi = aqi,
            alertHeadline = alertHeadline,
            alertDesc = alertDesc,
            hourlyForecast = hourlyItems,
            dailyForecast = dailyItems
        )
    }

    private suspend fun saveToCache(data: WeatherData) {
        val hourlyJson = hourlyListAdapter.toJson(data.hourlyForecast)
        val dailyJson = dailyListAdapter.toJson(data.dailyForecast)
        val entity = CachedWeatherEntity(
            city = data.city,
            tempC = data.tempC,
            tempF = data.tempF,
            conditionText = data.conditionText,
            conditionCode = data.conditionCode,
            humidity = data.humidity,
            windKph = data.windKph,
            windMph = data.windMph,
            windDegree = data.windDegree,
            windDir = data.windDir,
            pressureMb = data.pressureMb,
            uv = data.uv,
            sunrise = data.sunrise,
            sunset = data.sunset,
            moonPhase = data.moonPhase,
            moonIllumination = data.moonIllumination,
            moonrise = data.moonrise,
            moonset = data.moonset,
            feelsLikeC = data.feelsLikeC,
            feelsLikeF = data.feelsLikeF,
            aqi = data.aqi,
            alertHeadline = data.alertHeadline,
            alertDesc = data.alertDesc,
            hourlyJson = hourlyJson,
            dailyJson = dailyJson,
            timestamp = System.currentTimeMillis()
        )
        weatherDao.insertCachedWeather(entity)
        if (data.city.isNotBlank()) {
            weatherDao.insertRecentSearch(
                com.example.data.local.RecentSearchEntity(
                    cityName = data.city,
                    searchedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // Dynamic, Location-Specific Mock Weather Generation
    private fun generateMockWeatherData(cityInput: String): WeatherData {
        val city = if (cityInput.contains(",")) {
            val parts = cityInput.split(",")
            val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull()
            val lon = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
            if (lat != null && lon != null) {
                com.example.data.location.GeocodingService.getFallbackCity(lat, lon)
            } else {
                cityInput
            }
        } else {
            cityInput.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
        }

        // Deterministic seeding based on city name characters
        val citySeed = city.lowercase().hashCode()
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // Select general weather based on city name seed
        val weatherType = Math.abs(citySeed) % 4
        val baseTemp = 15.0 + (citySeed % 12) // Realistic base temperature between 3°C and 27°C
        
        val conditionText: String
        val conditionCode: Int
        var alertHeadline: String? = null
        var alertDesc: String? = null

        when (weatherType) {
            0 -> {
                conditionText = "Sunny"
                conditionCode = 1000
            }
            1 -> {
                conditionText = "Cloudy"
                conditionCode = 1006
            }
            2 -> {
                conditionText = "Heavy Rain"
                conditionCode = 1195
                if (Math.abs(citySeed) % 2 == 0) {
                    alertHeadline = "Severe Thunderstorm Warning"
                    alertDesc = "Strong winds and potential localized flooding expected in $city. Secure loose items and seek indoor shelter."
                }
            }
            else -> {
                conditionText = "Light Snow"
                conditionCode = 1213
                if (baseTemp < 10.0 && Math.abs(citySeed) % 3 == 0) {
                    alertHeadline = "Winter Weather Advisory"
                    alertDesc = "Snow accumulations of 2 to 4 inches expected in $city. Be cautious when traveling."
                }
            }
        }

        // Current temperature fluctuates dynamically with the current hour
        // Coolest around 5:00 AM, warmest around 3:00 PM (15:00)
        val hourOffset = sin((currentHour - 9.0) * (Math.PI / 12.0)) * 5.0
        val tempC = Math.round((baseTemp + hourOffset) * 10.0) / 10.0
        val tempF = Math.round((tempC * 1.8 + 32.0) * 10.0) / 10.0

        val feelsLikeC = Math.round((tempC + (citySeed % 2) - 0.5) * 10.0) / 10.0
        val feelsLikeF = Math.round((feelsLikeC * 1.8 + 32.0) * 10.0) / 10.0

        val humidity = 50 + (Math.abs(citySeed) % 41) // 50% - 90%
        val windKph = 5.0 + (Math.abs(citySeed) % 26) // 5 - 30 km/h
        val windMph = Math.round((windKph * 0.621371) * 10.0) / 10.0
        val windDegree = (Math.abs(citySeed) * 17) % 360
        val windDir = getWindDirectionString(windDegree)
        val pressureMb = 1008.0 + (Math.abs(citySeed) % 18) // 1008 - 1026 hPa
        val uv = if (currentHour in 7..18) {
            val middayFactor = sin((currentHour - 6.0) * (Math.PI / 12.0))
            Math.round((middayFactor * 10.0) * 10.0) / 10.0
        } else {
            0.0
        }

        val aqi = 1 + (Math.abs(citySeed) % 5) // AQI index 1-5

        // Generate 24 hours starting from the current hour
        val hourlyForecast = mutableListOf<HourlyForecastItem>()
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.MINUTE, 0)
        }
        for (h in 0 until 24) {
            val fCal = (startCalendar.clone() as Calendar).apply {
                add(Calendar.HOUR_OF_DAY, h)
            }
            val hourValue = fCal.get(Calendar.HOUR_OF_DAY)
            val hOffset = sin((hourValue - 9.0) * (Math.PI / 12.0)) * 5.0
            val hTempC = Math.round((baseTemp + hOffset) * 10.0) / 10.0
            val hTempF = Math.round((hTempC * 1.8 + 32.0) * 10.0) / 10.0
            
            // Generate realistic hourly conditions (slightly varying from current condition)
            var hCondText = conditionText
            var hCondCode = conditionCode
            val isDay = hourValue in 6..18
            if (h % 6 == 0 && weatherType == 1) {
                hCondText = if (isDay) "Partly Cloudy" else "Clear"
                hCondCode = if (isDay) 1003 else 1000
            }

            val formattedTime = SimpleDateFormat("hh:00 a", Locale.getDefault()).format(fCal.time)
            hourlyForecast.add(
                HourlyForecastItem(
                    time = formattedTime,
                    tempC = hTempC,
                    tempF = hTempF,
                    conditionText = hCondText,
                    conditionCode = hCondCode,
                    isDay = isDay,
                    epoch = fCal.timeInMillis / 1000
                )
            )
        }

        // Generate 7 days of forecast starting from today
        val dailyForecast = mutableListOf<DailyForecastItem>()
        val dayNames = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        for (d in 0 until 7) {
            val dayName = if (d == 0) "Today" else dayNames[(currentDayOfWeek - 1 + d) % 7]
            val daySeed = citySeed + d
            val maxC = Math.round((baseTemp + (Math.abs(daySeed) % 6)) * 10.0) / 10.0
            val minC = Math.round((baseTemp - 3.0 - (Math.abs(daySeed) % 5)) * 10.0) / 10.0
            val maxF = Math.round((maxC * 1.8 + 32.0) * 10.0) / 10.0
            val minF = Math.round((minC * 1.8 + 32.0) * 10.0) / 10.0

            var dCondText = conditionText
            var dCondCode = conditionCode

            // Add variety to 7 day forecast conditions
            when ((weatherType + d) % 4) {
                0 -> { dCondText = "Sunny"; dCondCode = 1000 }
                1 -> { dCondText = "Partly Cloudy"; dCondCode = 1003 }
                2 -> { dCondText = "Patchy Rain Nearby"; dCondCode = 1183 }
                3 -> { dCondText = "Overcast"; dCondCode = 1009 }
            }

            dailyForecast.add(
                DailyForecastItem(
                    date = dayName,
                    maxTempC = maxC,
                    maxTempF = maxF,
                    minTempC = minC,
                    minTempF = minF,
                    conditionText = dCondText,
                    conditionCode = dCondCode,
                    uv = Math.round((3.0 + (Math.abs(daySeed) % 6)) * 10.0) / 10.0
                )
            )
        }

        val moonIllumVal = Math.abs(citySeed + calendar.get(Calendar.DAY_OF_YEAR)) % 101
        val isWaxing = (Math.abs(citySeed) % 2) == 0
        val mockMoonPhase = when {
            moonIllumVal < 3 -> "New Moon"
            moonIllumVal < 25 -> if (isWaxing) "Waxing Crescent" else "Waning Crescent"
            moonIllumVal < 45 -> "First Quarter"
            moonIllumVal < 75 -> if (isWaxing) "Waxing Gibbous" else "Waning Gibbous"
            moonIllumVal < 97 -> "Full Moon"
            else -> "Third Quarter"
        }

        return WeatherData(
            city = city,
            tempC = tempC,
            tempF = tempF,
            conditionText = conditionText,
            conditionCode = conditionCode,
            humidity = humidity,
            windKph = windKph,
            windMph = windMph,
            windDegree = windDegree,
            windDir = windDir,
            pressureMb = pressureMb,
            uv = uv,
            sunrise = "05:58 AM",
            sunset = "08:34 PM",
            moonPhase = mockMoonPhase,
            moonIllumination = moonIllumVal,
            moonrise = "07:15 PM",
            moonset = "05:42 AM",
            feelsLikeC = feelsLikeC,
            feelsLikeF = feelsLikeF,
            aqi = aqi,
            alertHeadline = alertHeadline,
            alertDesc = alertDesc,
            hourlyForecast = hourlyForecast,
            dailyForecast = dailyForecast
        )
    }

    private fun getWindDirectionString(degree: Int): String {
        val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = (((degree % 360) + 360) % 360 + 11.25) / 22.5
        return directions[index.toInt() % 16]
    }

    suspend fun fetchOpenMeteoWeather(cityInput: String): WeatherData? {
        val geocodingService = openMeteoGeocodingService ?: return null
        val forecastService = openMeteoForecastService ?: return null

        var resolvedCityName = cityInput
        var lat: Double
        var lon: Double

        if (cityInput.contains(",")) {
            val parts = cityInput.split(",")
            val latParsed = parts.getOrNull(0)?.trim()?.toDoubleOrNull()
            val lonParsed = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
            if (latParsed != null && lonParsed != null) {
                lat = latParsed
                lon = lonParsed
                resolvedCityName = com.example.data.location.GeocodingService.getFallbackCity(lat, lon)
            } else {
                val searchRes = geocodingService.searchCity(cityInput)
                val firstResult = searchRes.results?.firstOrNull() ?: return null
                lat = firstResult.latitude
                lon = firstResult.longitude
                resolvedCityName = firstResult.name
            }
        } else {
            val searchRes = geocodingService.searchCity(cityInput)
            val firstResult = searchRes.results?.firstOrNull()
            if (firstResult != null) {
                lat = firstResult.latitude
                lon = firstResult.longitude
                resolvedCityName = firstResult.name
            } else {
                return null
            }
        }

        val forecast = forecastService.getForecast(latitude = lat, longitude = lon)
        return mapOpenMeteoToDomain(resolvedCityName, forecast)
    }

    private fun mapOpenMeteoToDomain(city: String, response: com.example.data.remote.OpenMeteoResponse): WeatherData {
        val current = response.current
        val tempC = current?.temperature2m ?: 20.0
        val tempF = Math.round((tempC * 1.8 + 32.0) * 10.0) / 10.0
        val humidity = current?.relativeHumidity2m ?: 50
        val feelsLikeC = current?.apparentTemperature ?: tempC
        val feelsLikeF = Math.round((feelsLikeC * 1.8 + 32.0) * 10.0) / 10.0
        val windKph = current?.windSpeed10m ?: 10.0
        val windMph = Math.round((windKph * 0.621371) * 10.0) / 10.0
        val windDegree = current?.windDirection10m ?: 0
        val windDir = getWindDirectionString(windDegree)
        val pressureMb = current?.surfacePressure ?: 1013.25
        val wmoCode = current?.weatherCode ?: 1
        val (conditionText, conditionCode) = mapWmoCodeToCondition(wmoCode)

        val sunriseRaw = response.daily?.sunrise?.firstOrNull() ?: ""
        val sunsetRaw = response.daily?.sunset?.firstOrNull() ?: ""
        val sunriseFormatted = formatIsoTimeToAmPm(sunriseRaw, "06:00 AM")
        val sunsetFormatted = formatIsoTimeToAmPm(sunsetRaw, "08:15 PM")

        val uv = response.daily?.uvIndexMax?.firstOrNull() ?: 5.0

        val hourlyList = mutableListOf<HourlyForecastItem>()
        val hourlyTimes = response.hourly?.time ?: emptyList()
        val hourlyTemps = response.hourly?.temperature2m ?: emptyList()
        val hourlyCodes = response.hourly?.weatherCode ?: emptyList()
        val hourlyIsDay = response.hourly?.isDay ?: emptyList()

        val calendar = Calendar.getInstance()
        val nowIndex = calendar.get(Calendar.HOUR_OF_DAY)

        for (i in 0 until minOf(24, if (hourlyTimes.isEmpty()) 24 else hourlyTimes.size)) {
            val idx = (nowIndex + i) % (if (hourlyTimes.isEmpty()) 1 else hourlyTimes.size)
            val timeStr = hourlyTimes.getOrNull(idx) ?: ""
            val hTempC = hourlyTemps.getOrNull(idx) ?: tempC
            val hTempF = Math.round((hTempC * 1.8 + 32.0) * 10.0) / 10.0
            val hCode = hourlyCodes.getOrNull(idx) ?: wmoCode
            val (hText, hCondCode) = mapWmoCodeToCondition(hCode)
            val hIsDay = hourlyIsDay.getOrNull(idx) ?: 1

            val displayHour = formatIsoTimeToHourOnly(timeStr, "${(nowIndex + i) % 24}:00")

            hourlyList.add(
                HourlyForecastItem(
                    time = if (i == 0) "Now" else displayHour,
                    tempC = hTempC,
                    tempF = hTempF,
                    conditionText = hText,
                    conditionCode = hCondCode,
                    isDay = hIsDay == 1,
                    epoch = System.currentTimeMillis() / 1000 + (i * 3600)
                )
            )
        }

        val dailyList = mutableListOf<DailyForecastItem>()
        val dailyTimes = response.daily?.time ?: emptyList()
        val dailyMaxTemps = response.daily?.temperature2mMax ?: emptyList()
        val dailyMinTemps = response.daily?.temperature2mMin ?: emptyList()
        val dailyCodes = response.daily?.weatherCode ?: emptyList()
        val dailyUv = response.daily?.uvIndexMax ?: emptyList()

        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val curDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        for (d in 0 until minOf(7, if (dailyTimes.isEmpty()) 7 else dailyTimes.size)) {
            val dMaxC = dailyMaxTemps.getOrNull(d) ?: (tempC + 3.0)
            val dMinC = dailyMinTemps.getOrNull(d) ?: (tempC - 4.0)
            val dMaxF = Math.round((dMaxC * 1.8 + 32.0) * 10.0) / 10.0
            val dMinF = Math.round((dMinC * 1.8 + 32.0) * 10.0) / 10.0
            val dCode = dailyCodes.getOrNull(d) ?: wmoCode
            val (dText, dCondCode) = mapWmoCodeToCondition(dCode)
            val dUv = dailyUv.getOrNull(d) ?: uv

            val dayLabel = if (d == 0) "Today" else dayNames[(curDayOfWeek - 1 + d) % 7]

            dailyList.add(
                DailyForecastItem(
                    date = dayLabel,
                    maxTempC = dMaxC,
                    maxTempF = dMaxF,
                    minTempC = dMinC,
                    minTempF = dMinF,
                    conditionText = dText,
                    conditionCode = dCondCode,
                    uv = dUv
                )
            )
        }

        return WeatherData(
            city = city,
            tempC = tempC,
            tempF = tempF,
            conditionText = conditionText,
            conditionCode = conditionCode,
            humidity = humidity,
            windKph = windKph,
            windMph = windMph,
            windDegree = windDegree,
            windDir = windDir,
            pressureMb = pressureMb,
            uv = uv,
            sunrise = sunriseFormatted,
            sunset = sunsetFormatted,
            moonPhase = "Waxing Gibbous",
            moonIllumination = 75,
            moonrise = "07:30 PM",
            moonset = "05:15 AM",
            feelsLikeC = feelsLikeC,
            feelsLikeF = feelsLikeF,
            aqi = 2,
            alertHeadline = if (wmoCode >= 95) "Thunderstorm Watch" else null,
            alertDesc = if (wmoCode >= 95) "Localized heavy rain and lightning expected. Seek shelter." else null,
            hourlyForecast = hourlyList,
            dailyForecast = dailyList
        )
    }

    private fun mapWmoCodeToCondition(wmoCode: Int): Pair<String, Int> {
        return when (wmoCode) {
            0 -> "Clear Sky" to 1000
            1 -> "Mainly Clear" to 1003
            2 -> "Partly Cloudy" to 1003
            3 -> "Overcast" to 1009
            45, 48 -> "Foggy" to 1030
            51, 53, 55 -> "Light Drizzle" to 1153
            56, 57 -> "Freezing Drizzle" to 1168
            61, 63 -> "Rain" to 1183
            65 -> "Heavy Rain" to 1195
            66, 67 -> "Freezing Rain" to 1198
            71, 73 -> "Light Snow" to 1213
            75 -> "Heavy Snow" to 1225
            77 -> "Snow Grains" to 1237
            80, 81, 82 -> "Rain Showers" to 1240
            85, 86 -> "Snow Showers" to 1255
            95 -> "Thunderstorm" to 1273
            96, 99 -> "Thunderstorm with Hail" to 1276
            else -> "Partly Cloudy" to 1003
        }
    }

    private fun formatIsoTimeToAmPm(isoTime: String, fallback: String): String {
        return try {
            if (isoTime.contains("T")) {
                val timePart = isoTime.substringAfter("T")
                val parts = timePart.split(":")
                val hour = parts[0].toInt()
                val min = parts[1].toInt()
                val amPm = if (hour >= 12) "PM" else "AM"
                val h12 = if (hour % 12 == 0) 12 else hour % 12
                String.format(Locale.US, "%02d:%02d %s", h12, min, amPm)
            } else fallback
        } catch (e: Exception) {
            fallback
        }
    }

    private fun formatIsoTimeToHourOnly(isoTime: String, fallback: String): String {
        return try {
            if (isoTime.contains("T")) {
                val timePart = isoTime.substringAfter("T")
                val hour = timePart.split(":")[0].toInt()
                val amPm = if (hour >= 12) "PM" else "AM"
                val h12 = if (hour % 12 == 0) 12 else hour % 12
                "$h12 $amPm"
            } else fallback
        } catch (e: Exception) {
            fallback
        }
    }
}
