package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedWeatherEntity::class,
        FavoriteCityEntity::class,
        MonthlyClimateEntity::class,
        CachedWeatherMapEntity::class,
        RecentSearchEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}
