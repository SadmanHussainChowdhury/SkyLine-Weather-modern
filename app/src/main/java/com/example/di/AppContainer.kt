package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.SettingsDataStore
import com.example.data.local.WeatherDatabase
import com.example.data.remote.WeatherApiService
import com.example.data.repository.WeatherRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AppContainer(private val context: Context) {

    private val database: WeatherDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            WeatherDatabase::class.java,
            "weather_database"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(context.applicationContext)
    }

    private val apiService: WeatherApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    val pollenApiService: com.example.data.remote.PollenApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://air-quality-api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(com.example.data.remote.PollenApiService::class.java)
    }

    val pollenRepository: com.example.data.repository.PollenRepository by lazy {
        com.example.data.repository.PollenRepository(pollenApiService)
    }

    val openMeteoGeocodingService: com.example.data.remote.OpenMeteoGeocodingService by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(com.example.data.remote.OpenMeteoGeocodingService::class.java)
    }

    val openMeteoForecastService: com.example.data.remote.OpenMeteoForecastService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(com.example.data.remote.OpenMeteoForecastService::class.java)
    }

    val weatherRepository: WeatherRepository by lazy {
        WeatherRepository(
            apiService = apiService,
            weatherDao = database.weatherDao(),
            openMeteoGeocodingService = openMeteoGeocodingService,
            openMeteoForecastService = openMeteoForecastService
        )
    }

    val geofencingManager: com.example.data.location.GeofencingManager by lazy {
        com.example.data.location.GeofencingManager(context.applicationContext)
    }

    val notificationManager: com.example.data.notification.WeatherNotificationManager by lazy {
        com.example.data.notification.WeatherNotificationManager(context.applicationContext)
    }

    val geminiApiService: com.example.data.remote.GeminiApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(com.example.data.remote.GeminiApiService::class.java)
    }

    val smartInsightsRepository: com.example.data.repository.SmartInsightsRepository by lazy {
        com.example.data.repository.SmartInsightsRepository(geminiApiService)
    }
}
