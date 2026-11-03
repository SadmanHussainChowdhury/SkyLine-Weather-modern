package com.example.service

import android.util.Log
import com.example.WeatherApplication
import com.example.data.notification.WeatherNotificationManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class WeatherFirebaseMessagingService : FirebaseMessagingService() {

    @Suppress("DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM Registration Token: $token")
        
        // Pass token to application notification manager instance
        val app = applicationContext as? WeatherApplication
        app?.container?.notificationManager?.updateFcmToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        val notificationManager = (applicationContext as? WeatherApplication)?.container?.notificationManager
            ?: WeatherNotificationManager(applicationContext)

        // Check if message contains a notification payload or data payload
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Real-time Weather Alert"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Severe weather conditions reported for your GPS location."

        val severity = remoteMessage.data["severity"] ?: "Severe"
        val location = remoteMessage.data["location"] ?: "Current Location"

        Log.d(TAG, "Handling incoming FCM alert payload: $title - $body")

        notificationManager.showWeatherAlertNotification(
            title = title,
            body = body,
            severity = severity,
            location = location,
            isFcm = true
        )
    }

    companion object {
        private const val TAG = "WeatherFcmService"
    }
}
