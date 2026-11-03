package com.example.data.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.WeatherApplication
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceReceiver"
        const val ACTION_SIMULATE_GEOFENCE = "com.example.ACTION_SIMULATE_GEOFENCE"
        const val CHANNEL_ID = "weather_geofence_channel"
        const val NOTIFICATION_ID = 4004
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var cityName = "Unknown City"
                var transitionType = -1
                
                if (intent.action == ACTION_SIMULATE_GEOFENCE) {
                    cityName = intent.getStringExtra("cityName") ?: "San Francisco"
                    transitionType = intent.getIntExtra("transitionType", Geofence.GEOFENCE_TRANSITION_ENTER)
                    Log.d(TAG, "Simulated geofence trigger received for $cityName")
                } else {
                    val geofencingEvent = GeofencingEvent.fromIntent(intent)
                    if (geofencingEvent != null && !geofencingEvent.hasError()) {
                        transitionType = geofencingEvent.geofenceTransition
                        val triggeringGeofences = geofencingEvent.triggeringGeofences
                        if (!triggeringGeofences.isNullOrEmpty()) {
                            cityName = triggeringGeofences[0].requestId
                            Log.d(TAG, "Real geofence trigger received: $cityName, transition: $transitionType")
                        }
                    }
                }

                if (transitionType != -1) {
                    handleGeofenceTransition(context, cityName, transitionType)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing geofence transition: ${e.localizedMessage}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleGeofenceTransition(context: Context, cityName: String, transitionType: Int) {
        val transitionName = when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Entered"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Exited"
            else -> "Moved in"
        }

        // 1. Fetch updated weather and update cache in Room database
        val appContainer = (context.applicationContext as WeatherApplication).container
        val repository = appContainer.weatherRepository
        val settings = appContainer.settingsDataStore
        
        val apiKey = settings.customApiKeyFlow.first()
        // Fire a background update
        repository.getWeather(cityName, forceRefresh = true, userApiKey = apiKey).first()

        // 2. Trigger active notification
        showNotification(context, cityName, "$transitionName geofence boundary of $cityName. Auto-updating local weather cache.")
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Skyline Geofencing Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when entering or leaving weather geofenced areas."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Using a standard checkmark or alert icon
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Skyline Weather Geofence")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
