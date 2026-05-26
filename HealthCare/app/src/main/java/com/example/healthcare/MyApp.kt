package com.example.healthcare

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.healthcare.workers.scheduleDailyReportWorker

class MyApp : Application() {

    companion object {
        var currentNightMode: Int = AppCompatDelegate.MODE_NIGHT_NO
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        com.google.firebase.FirebaseApp.initializeApp(this)

        // Load saved theme
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        currentNightMode = if (prefs.getString("theme", "Light") == "Dark") {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }

        AppCompatDelegate.setDefaultNightMode(currentNightMode)

        scheduleDailyReportWorker(this)
    }
}
