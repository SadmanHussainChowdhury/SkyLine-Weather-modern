package com.example.data.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import java.io.IOException
import java.util.Locale

object GeocodingService {
    private const val TAG = "GeocodingService"

    // Dynamic database of common fallback cities across major world regions
    private val fallbackCities = mapOf(
        "san francisco" to Pair(37.7749, -122.4194),
        "los angeles" to Pair(34.0522, -118.2437),
        "chicago" to Pair(41.8781, -87.6298),
        "new york" to Pair(40.7128, -74.0060),
        "miami" to Pair(25.7617, -80.1918),
        "dallas" to Pair(32.7767, -96.7970),
        "seattle" to Pair(47.6062, -122.3321),
        "toronto" to Pair(43.6532, -79.3832),
        "vancouver" to Pair(49.2827, -123.1207),
        "mexico city" to Pair(19.4326, -99.1332),
        "london" to Pair(51.5074, -0.1278),
        "paris" to Pair(48.8566, 2.3522),
        "berlin" to Pair(52.5200, 13.4050),
        "rome" to Pair(41.9028, 12.4964),
        "madrid" to Pair(40.4168, -3.7038),
        "amsterdam" to Pair(52.3676, 4.9041),
        "tokyo" to Pair(35.6762, 139.6503),
        "beijing" to Pair(39.9042, 116.4074),
        "shanghai" to Pair(31.2304, 121.4737),
        "seoul" to Pair(37.5665, 126.9780),
        "singapore" to Pair(1.3521, 103.8198),
        "mumbai" to Pair(19.0760, 72.8777),
        "delhi" to Pair(28.6139, 77.2090),
        "dhaka" to Pair(23.8103, 90.4125),
        "bangkok" to Pair(13.7563, 100.5018),
        "dubai" to Pair(25.2048, 55.2708),
        "cairo" to Pair(30.0444, 31.2357),
        "sydney" to Pair(-33.8688, 151.2093),
        "melbourne" to Pair(-37.8136, 144.9631),
        "sao paulo" to Pair(-23.5505, -46.6333),
        "buenos aires" to Pair(-34.6037, -58.3816),
        "johannesburg" to Pair(-26.2041, 28.0473)
    )

    /**
     * Converts coordinates into a user-friendly City Name.
     * Uses system Geocoder, with custom fallback logic for offline/sandbox environments.
     */
    fun getCityFromLocation(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea ?: address.adminArea ?: address.featureName
                if (!city.isNullOrBlank()) {
                    return city
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Geocoding failed: ${e.localizedMessage}", e)
        }

        return getFallbackCity(latitude, longitude)
    }

    /**
     * Converts a city name into Coordinates (Latitude, Longitude).
     * Uses system Geocoder, with fallback for offline/sandbox environments.
     */
    fun getCoordinatesFromCity(context: Context, cityName: String): Pair<Double, Double> {
        val trimmed = cityName.trim().lowercase()
        
        // If the query is already in "lat,lon" format, parse it directly
        val latLonParts = trimmed.split(",")
        if (latLonParts.size == 2) {
            val lat = latLonParts[0].toDoubleOrNull()
            val lon = latLonParts[1].toDoubleOrNull()
            if (lat != null && lon != null) {
                return Pair(lat, lon)
            }
        }

        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(cityName, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                return Pair(address.latitude, address.longitude)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Forward geocoding failed: ${e.localizedMessage}", e)
        }

        return fallbackCities[trimmed] ?: Pair(37.7749, -122.4194)
    }

    /**
     * Custom mathematical distance fallback to find the nearest known city.
     */
    fun getFallbackCity(latitude: Double, longitude: Double): String {
        var nearestCity = "San Francisco"
        var minDistance = Double.MAX_VALUE

        for ((city, coords) in fallbackCities) {
            val dLat = latitude - coords.first
            val dLon = longitude - coords.second
            val dist = dLat * dLat + dLon * dLon
            if (dist < minDistance) {
                minDistance = dist
                nearestCity = city.split(" ").joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
            }
        }

        return nearestCity
    }
}
