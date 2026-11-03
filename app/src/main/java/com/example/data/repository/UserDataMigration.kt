package com.example.data.repository

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SavedLocationMigrationItem(
    val cityName: String,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val addedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class RecentSearchMigrationItem(
    val cityName: String,
    val searchedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class UserDataMigrationPackage(
    val version: Int = 1,
    val appName: String = "Skyline Weather",
    val exportedAt: Long = System.currentTimeMillis(),
    val savedLocations: List<SavedLocationMigrationItem> = emptyList(),
    val searchHistory: List<RecentSearchMigrationItem> = emptyList()
)

data class UserDataMigrationResult(
    val importedLocationsCount: Int,
    val importedSearchesCount: Int
)
