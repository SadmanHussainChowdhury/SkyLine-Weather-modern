package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WeatherAlertNotification(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val body: String,
    val severity: String = "Severe", // Extreme, Severe, Moderate, Minor
    val location: String,
    val timestamp: String = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date()),
    val alertType: String = "Weather Warning",
    val isRealtimeFcm: Boolean = true
)

class WeatherNotificationManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

    private val _subscribedTopics = MutableStateFlow<Set<String>>(emptySet())
    val subscribedTopics: StateFlow<Set<String>> = _subscribedTopics.asStateFlow()

    private val _recentAlerts = MutableStateFlow<List<WeatherAlertNotification>>(emptyList())
    val recentAlerts: StateFlow<List<WeatherAlertNotification>> = _recentAlerts.asStateFlow()

    // TTS Auto-Read Settings & State
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isAutoReadFcmAlerts = MutableStateFlow(true)
    val isAutoReadFcmAlerts: StateFlow<Boolean> = _isAutoReadFcmAlerts.asStateFlow()

    private val _isTtsSpeaking = MutableStateFlow(false)
    val isTtsSpeaking: StateFlow<Boolean> = _isTtsSpeaking.asStateFlow()

    private val _currentlySpeakingAlertId = MutableStateFlow<String?>(null)
    val currentlySpeakingAlertId: StateFlow<String?> = _currentlySpeakingAlertId.asStateFlow()

    init {
        createNotificationChannels()
        fetchFcmToken()
        initTtsEngine()
    }

    private fun initTtsEngine() {
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("WeatherFcmTts", "Error initializing TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.getDefault())
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsInitialized = true
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isTtsSpeaking.value = true
                        _currentlySpeakingAlertId.value = utteranceId
                    }

                    override fun onDone(utteranceId: String?) {
                        _isTtsSpeaking.value = false
                        _currentlySpeakingAlertId.value = null
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isTtsSpeaking.value = false
                        _currentlySpeakingAlertId.value = null
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isTtsSpeaking.value = false
                        _currentlySpeakingAlertId.value = null
                    }
                })
                Log.d("WeatherFcmTts", "FCM Alert Text-To-Speech initialized successfully.")
            } else {
                Log.w("WeatherFcmTts", "TTS language not supported.")
            }
        } else {
            Log.e("WeatherFcmTts", "TTS initialization failed with status $status")
        }
    }

    fun setAutoReadFcmAlerts(enabled: Boolean) {
        _isAutoReadFcmAlerts.value = enabled
        if (!enabled) {
            stopTts()
        }
    }

    fun speakAlert(alert: WeatherAlertNotification) {
        if (!isTtsInitialized || textToSpeech == null) {
            Log.w("WeatherFcmTts", "TTS engine not ready to speak alert.")
            return
        }

        stopTts()

        val speechText = "Critical Weather Alert for ${alert.location}. ${alert.severity} level ${alert.title}. ${alert.body}"
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, alert.id)
        }

        textToSpeech?.speak(speechText, TextToSpeech.QUEUE_FLUSH, params, alert.id)
        _isTtsSpeaking.value = true
        _currentlySpeakingAlertId.value = alert.id
    }

    fun stopTts() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e("WeatherFcmTts", "Error stopping TTS", e)
        }
        _isTtsSpeaking.value = false
        _currentlySpeakingAlertId.value = null
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel 1: Severe Weather Warnings (High Priority, Sound, Vibration)
            val severeChannel = NotificationChannel(
                CHANNEL_SEVERE_ALERTS,
                "Severe Weather Warnings (FCM)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time emergency severe weather alerts based on GPS location"
                enableVibration(true)
                enableLights(true)
            }

            // Channel 2: Daily Forecast Briefings
            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_BRIEF,
                "Daily Weather Briefings",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily morning and evening weather summaries"
            }

            notificationManager.createNotificationChannel(severeChannel)
            notificationManager.createNotificationChannel(dailyChannel)
        }
    }

    private fun getFirebaseMessaging(): FirebaseMessaging? {
        return try {
            if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(context)
            }
            if (com.google.firebase.FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseMessaging.getInstance()
            } else null
        } catch (t: Throwable) {
            Log.w("WeatherFcm", "FirebaseMessaging unavailable: ${t.message}")
            null
        }
    }

    fun fetchFcmToken() {
        try {
            val messaging = getFirebaseMessaging()
            if (messaging != null) {
                messaging.token.addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        val token = task.result
                        _fcmToken.value = token
                        Log.d("WeatherFcm", "FCM Token obtained: $token")
                    } else {
                        Log.w("WeatherFcm", "Fetching FCM token failed", task.exception)
                        _fcmToken.value = "simulated_fcm_token_${System.currentTimeMillis()}"
                    }
                }
            } else {
                _fcmToken.value = "simulated_fcm_token_local_mode"
            }
        } catch (t: Throwable) {
            Log.w("WeatherFcm", "Error accessing FirebaseMessaging instance: ${t.message}")
            _fcmToken.value = "simulated_fcm_token_fallback"
        }
    }

    fun updateFcmToken(token: String) {
        _fcmToken.value = token
    }

    fun subscribeToGpsLocationTopic(cityName: String, latitude: Double, longitude: Double) {
        val sanitizedCity = cityName.replace("[^a-zA-Z0-9_]".toRegex(), "_").lowercase(Locale.ROOT)
        val cityTopic = "weather_alerts_$sanitizedCity"
        val generalTopic = "severe_weather_global"

        try {
            val messaging = getFirebaseMessaging()
            if (messaging != null) {
                messaging.subscribeToTopic(cityTopic)
                messaging.subscribeToTopic(generalTopic)
                Log.d("WeatherFcm", "Subscribed to FCM topic: $cityTopic for location ($latitude, $longitude)")
            } else {
                Log.d("WeatherFcm", "Simulated topic subscription for $cityTopic (Firebase offline or uninitialized)")
            }

            val currentSet = _subscribedTopics.value.toMutableSet()
            currentSet.add(cityTopic)
            currentSet.add(generalTopic)
            _subscribedTopics.value = currentSet
        } catch (t: Throwable) {
            Log.w("WeatherFcm", "Failed to subscribe to FCM topic $cityTopic: ${t.message}")
            val currentSet = _subscribedTopics.value.toMutableSet()
            currentSet.add(cityTopic)
            currentSet.add(generalTopic)
            _subscribedTopics.value = currentSet
        }
    }

    fun showWeatherAlertNotification(
        title: String,
        body: String,
        severity: String = "Severe",
        location: String = "Current Location",
        isFcm: Boolean = true
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationId = (System.currentTimeMillis() % 100000).toInt()

        val notification = NotificationCompat.Builder(context, CHANNEL_SEVERE_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 $title")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n\nLocation: $location • Severity: $severity"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)

        // Store in alert history flow
        val alert = WeatherAlertNotification(
            title = title,
            body = body,
            severity = severity,
            location = location,
            isRealtimeFcm = isFcm
        )
        val updatedList = listOf(alert) + _recentAlerts.value
        _recentAlerts.value = updatedList.take(20)

        // Automatically read FCM alert aloud for screen-free accessibility
        if (_isAutoReadFcmAlerts.value) {
            speakAlert(alert)
        }
    }

    fun simulateIncomingFcmPush(
        title: String = "Tornado Watch Issued",
        body: String = "Severe thunderstorms capable of producing tornadoes detected within 15 miles of your GPS coordinates. Take shelter immediately.",
        severity: String = "Extreme",
        location: String = "San Francisco, CA"
    ) {
        showWeatherAlertNotification(
            title = title,
            body = body,
            severity = severity,
            location = location,
            isFcm = true
        )
    }

    companion object {
        const val CHANNEL_SEVERE_ALERTS = "severe_weather_alerts_channel"
        const val CHANNEL_DAILY_BRIEF = "daily_weather_brief_channel"
    }
}
