package com.example.healthcare.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import java.util.Calendar

object AlarmHelper {

    /**
     * Schedule an exact alarm for a specific plan (meal, medication, exercise) on a specific date.
     */
    fun schedulePlanAlarm(
        context: Context,
        planId: Int,
        planType: String,
        planName: String,
        year: Int,
        month: Int, // 0-based (Calendar)
        day: Int,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Android 12+ exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(
                context,
                "Please allow exact alarms for reminders to work.",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        }

        // Intent for ReminderReceiver
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("PLAN_ID", planId)
            putExtra("PLAN_TYPE", planType)
            putExtra("PLAN_NAME", planName)
            putExtra("PLAN_HOUR", hour)
            putExtra("PLAN_MINUTE", minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            planId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(year, month, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Only schedule if the alarm is in the future
        if (calendar.timeInMillis <= System.currentTimeMillis()) return

        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
                else -> {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(context, "Cannot schedule exact alarms", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelPlanAlarm(context: Context, planId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            planId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleDailyAlarm(context: Context, hour: Int = 0, minute: Int = 0) {
        // Schedule a fixed daily sync alarm at 0:00 by default
        schedulePlanAlarm(
            context,
            planId = 0,
            planType = "Sync",
            planName = "Daily Sync",
            year = Calendar.getInstance().get(Calendar.YEAR),
            month = Calendar.getInstance().get(Calendar.MONTH),
            day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
            hour = hour,
            minute = minute
        )
    }

}
