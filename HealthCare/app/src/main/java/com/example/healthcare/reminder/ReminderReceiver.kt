package com.example.healthcare.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.entities.TodayNotification
import com.example.healthcare.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val planId = intent.getIntExtra("PLAN_ID", -1)
        val planType = intent.getStringExtra("PLAN_TYPE") ?: "Unknown"
        val planName = intent.getStringExtra("PLAN_NAME") ?: "Unnamed"

        android.util.Log.d(
            "AlarmDebug",
            "Alarm fired for planId=$planId, type=$planType, name=$planName"
        )

        // Keep receiver alive while handling
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            // Acquire a short WakeLock to avoid Doze delay
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "HealthCare:ReminderWakeLock"
            )
            wakeLock.acquire(5000) // max 5 seconds

            try {
                handleReminder(context, intent)
            } finally {
                wakeLock.release()
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleReminder(context: Context, intent: Intent) {
        val planId = intent.getIntExtra("PLAN_ID", -1)
        val planType = intent.getStringExtra("PLAN_TYPE") ?: "Unknown"
        val planName = intent.getStringExtra("PLAN_NAME") ?: "Unnamed"
        val planHour = intent.getIntExtra("PLAN_HOUR", 0)
        val planMinute = intent.getIntExtra("PLAN_MINUTE", 0)

        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val soundEnabled = prefs.getBoolean("sound_alert", true)
        val vibrationEnabled = prefs.getBoolean("vibration", true)

        val soundUri = Uri.parse("android.resource://${context.packageName}/raw/reminder_sound")

        // Show notification with custom sound
        NotificationHelper.show(
            context = context,
            category = planType,
            name = planName,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled,
            customSoundUri = soundUri
        )

        // Log reminder to database
        if (planId != -1) {
            val db = AppDatabase.getDatabase(context)
            db.todayNotificationDao().insert(
                TodayNotification(
                    userId = 1, // adjust if multi-user
                    category = planType,
                    name = planName,
                    date = LocalDate.now().toString(),
                    time = "%02d:%02d".format(planHour, planMinute)
                )
            )
        }
    }
}
