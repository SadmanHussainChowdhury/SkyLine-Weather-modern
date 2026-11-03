package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM cached_weather WHERE city = :city LIMIT 1")
    fun getCachedWeatherFlow(city: String): Flow<CachedWeatherEntity?>

    @Query("SELECT * FROM cached_weather WHERE city = :city LIMIT 1")
    suspend fun getCachedWeather(city: String): CachedWeatherEntity?

    @Query("SELECT * FROM cached_weather ORDER BY timestamp DESC LIMIT 1")
    fun getLatestCachedWeatherFlow(): Flow<CachedWeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedWeather(weather: CachedWeatherEntity)

    @Query("DELETE FROM cached_weather")
    suspend fun clearAllCachedWeather()

    @Query("SELECT * FROM favorite_cities ORDER BY addedAt DESC")
    fun getFavoriteCities(): Flow<List<FavoriteCityEntity>>

    @Query("SELECT * FROM favorite_cities ORDER BY addedAt DESC")
    suspend fun getFavoriteCitiesList(): List<FavoriteCityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteCity(city: FavoriteCityEntity)

    @Query("DELETE FROM favorite_cities WHERE cityName = :cityName")
    suspend fun deleteFavoriteCity(cityName: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_cities WHERE cityName = :cityName)")
    suspend fun isFavoriteCity(cityName: String): Boolean

    @Query("SELECT * FROM recent_searches ORDER BY searchedAt DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>

    @Query("SELECT * FROM recent_searches ORDER BY searchedAt DESC")
    suspend fun getRecentSearchesList(): List<RecentSearchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE cityName = :cityName")
    suspend fun deleteRecentSearch(cityName: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearAllRecentSearches()

    @Query("SELECT * FROM monthly_climate_trends WHERE city = :city ORDER BY month ASC")
    fun getMonthlyClimateTrendsFlow(city: String): Flow<List<MonthlyClimateEntity>>

    @Query("SELECT * FROM monthly_climate_trends WHERE city = :city ORDER BY month ASC")
    suspend fun getMonthlyClimateTrends(city: String): List<MonthlyClimateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyClimateTrends(trends: List<MonthlyClimateEntity>)

    @Query("SELECT COUNT(*) FROM monthly_climate_trends WHERE city = :city")
    suspend fun getMonthlyClimateCount(city: String): Int

    @Query("SELECT * FROM cached_weather_maps WHERE city = :city LIMIT 1")
    fun getCachedWeatherMapFlow(city: String): Flow<CachedWeatherMapEntity?>

    @Query("SELECT * FROM cached_weather_maps WHERE city = :city LIMIT 1")
    suspend fun getCachedWeatherMap(city: String): CachedWeatherMapEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedWeatherMap(mapEntity: CachedWeatherMapEntity)

    @Query("DELETE FROM cached_weather_maps WHERE city = :city")
    suspend fun deleteCachedWeatherMap(city: String)

    @Query("SELECT COUNT(*) FROM cached_weather_maps")
    suspend fun getCachedWeatherMapCount(): Int
}
