package com.example.healthcare.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.healthcare.R

object NotificationHelper {

    private const val CHANNEL_ID = "reminder_channel_v2"
    private const val CHANNEL_NAME = "Reminders"

    fun show(
        context: Context,
        category: String,
        name: String,
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true,
        customSoundUri: Uri? = null
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = customSoundUri ?: Uri.parse("android.resource://${context.packageName}/raw/reminder_sound")

        // Ensure channel is fresh (delete old one if exists)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.deleteNotificationChannel(CHANNEL_ID)

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(vibrationEnabled)
                vibrationPattern = longArrayOf(0, 500, 500, 500)
                if (soundEnabled) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    setSound(soundUri, audioAttributes)
                } else {
                    setSound(null, null)
                }
            }
            manager.createNotificationChannel(channel)
        }

        //  Build notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("$category Reminder")
            .setContentText(name)
            .setSmallIcon(R.drawable.ic_alerts)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .apply {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && soundEnabled) {
                    setSound(soundUri)
                }
                if (vibrationEnabled) {
                    setVibrate(longArrayOf(0, 500, 500, 500))
                }
            }

        // Show notification
        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        manager.notify(notificationId, builder.build())
    }
}
