package com.example

import android.app.Application
import android.util.Log
import com.example.di.AppContainer
import com.google.firebase.FirebaseApp

class WeatherApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            Log.w("WeatherApplication", "FirebaseApp initialization skipped or failed: ${e.message}")
        }
        container = AppContainer(this)
    }
}
