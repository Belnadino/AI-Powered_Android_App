package com.example.healthcare.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.entities.CaregiverEntity
import com.example.healthcare.utils.EmailSender
import com.example.healthcare.utils.ReportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import android.util.Log

class ReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val ctx = context.applicationContext

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(ctx)
            val caregivers: List<CaregiverEntity> = db.caregiverDao().getAllCaregiversOnce()
            if (caregivers.isEmpty()) return@withContext Result.success()

            val now = System.currentTimeMillis()
            val olderAdultId = 1 // fixed older adult ID

            // Generate the report only once
            val reportFile: File = ReportUtils.generateExcelReport(ctx, olderAdultId)

            // Send report to all caregivers
            for (caregiver in caregivers) {
                if (shouldSendReport(caregiver, now)) {
                    val email = caregiver.email
                    if (!email.isNullOrEmpty()) {
                        sendReport(reportFile, caregiver)
                        db.caregiverDao().updateLastSent(caregiver.id, now)
                    } else {
                        // Log instead of crashing or trying to send
                        Log.w("ReportWorker", "Caregiver email not set for care giver/manager, skipping report")
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun shouldSendReport(caregiver: CaregiverEntity, now: Long): Boolean {
        val lastSent = caregiver.lastReportSentAt ?: 0L
        val diffDays = (now - lastSent) / (1000 * 60 * 60 * 24)

        return when (caregiver.reportFrequency) {
            1 -> diffDays >= 1   // DAILY
            3 -> diffDays >= 3   // THREE_DAYS
            7 -> diffDays >= 7   // WEEKLY
            else -> false        // NONE
        }
    }

    private suspend fun sendReport(file: File, caregiver: CaregiverEntity) {
        EmailSender.sendEmail(
            smtpHost = "smtp.gmail.com",
            smtpPort = "587",
            senderEmail = "healthcarep499@gmail.com",
            senderPassword = "yxyicpdgrdnmmhxk", // App password
            recipientEmail = caregiver.email,
            subject = "Healthcare Report",
            body = "Please find the attached healthcare report.",
            attachmentFilePath = file.absolutePath
        )
    }

    companion object {
        fun scheduleDailyReportWorker(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<ReportWorker>(1, TimeUnit.DAYS)
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
    }
}
