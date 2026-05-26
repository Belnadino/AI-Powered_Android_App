package com.example.healthcare.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Schedule a daily "sync" alarm at 0:00
                AlarmHelper.scheduleDailyAlarm(context, 0, 0)

                // Sync TODAY’s alarms after boot
                ReminderWorker(context).syncAlarmsForTodayAndFuture()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
