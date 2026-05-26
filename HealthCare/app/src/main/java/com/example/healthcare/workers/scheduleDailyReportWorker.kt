package com.example.healthcare.workers

import android.content.Context
import androidx.work.*

fun scheduleDailyReportWorker(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<ReportWorker>(1, java.util.concurrent.TimeUnit.DAYS)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "DailyReportWorker",
        ExistingPeriodicWorkPolicy.UPDATE,
        workRequest
    )
}
