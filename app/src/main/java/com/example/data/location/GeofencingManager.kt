package com.example.data.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofencingManager(private val context: Context) {

    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(context)
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    /**
     * Registers a physical geofence with play-services-location.
     */
    @SuppressLint("MissingPermission")
    fun addGeofence(cityName: String, latitude: Double, longitude: Double, radiusInMeters: Float = 1000f) {
        try {
            val geofence = Geofence.Builder()
                .setRequestId(cityName)
                .setCircularRegion(latitude, longitude, radiusInMeters)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()

            val request = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            geofencingClient.addGeofences(request, geofencePendingIntent).run {
                addOnSuccessListener {
                    Log.d("GeofencingManager", "Successfully registered real Geofence for $cityName at ($latitude, $longitude)")
                }
                addOnFailureListener { e ->
                    Log.e("GeofencingManager", "Failed to register real Geofence for $cityName: ${e.localizedMessage}")
                }
            }
        } catch (e: Exception) {
            Log.e("GeofencingManager", "Exception registering Geofence: ${e.localizedMessage}")
        }
    }

    /**
     * Removes a physical geofence.
     */
    fun removeGeofence(cityName: String) {
        geofencingClient.removeGeofences(listOf(cityName)).run {
            addOnSuccessListener {
                Log.d("GeofencingManager", "Successfully removed Geofence for $cityName")
            }
            addOnFailureListener { e ->
                Log.e("GeofencingManager", "Failed to remove Geofence for $cityName: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Instantly triggers a simulated geofence transition.
     * This bypasses physical GPS triggers and is ideal for sandbox streaming verification.
     */
    fun simulateGeofenceTransition(cityName: String, isEntering: Boolean) {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = GeofenceBroadcastReceiver.ACTION_SIMULATE_GEOFENCE
            putExtra("cityName", cityName)
            putExtra("transitionType", if (isEntering) Geofence.GEOFENCE_TRANSITION_ENTER else Geofence.GEOFENCE_TRANSITION_EXIT)
        }
        context.sendBroadcast(intent)
    }
}
