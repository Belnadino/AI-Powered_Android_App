package com.example.healthcare.utils

import android.content.Context
import android.util.Log
import com.example.healthcare.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ReportUtils {

    /**
     * Generates a full Excel report for the given user.
     * If userId is null, uses the first user in DB (guest-safe).
     */
    suspend fun generateExcelReport(
        context: Context,
        userId: Int? = null
    ): File = withContext(Dispatchers.IO) {

        val db = AppDatabase.getDatabase(context)

        // =========================
        // DELETE OLD REPORTS
        // =========================
        clearOldReports(context)

        // =========================
        // DETERMINE USER ID
        // =========================
        val actualUserId = userId ?: db.userDao().getAllUsers().firstOrNull()?.userId ?: 1

        // =========================
        // CREATE WORKBOOK (Android safe)
        // =========================
        val workbook = XSSFWorkbook()

        // =========================
        // MEAL REPORT
        // =========================
        val meals = db.mealLogDao().getMealLogsForUser(actualUserId)
        val mealSheet = workbook.createSheet("Meal Report")
        val mealHeaders = listOf("Date", "Meal Time", "Meal Name", "Description", "Status")
        createSheetWithData(mealSheet, mealHeaders, meals.asSequence().map {
            listOf(it.logDate, it.mealTime, it.mealName, it.description, it.status)
        })

        // =========================
        // MEDICATION REPORT
        // =========================
        val meds = db.medicineLogDao().getMedicationLogsForUser(actualUserId)
        val medSheet = workbook.createSheet("Medication Report")
        val medHeaders = listOf("Date", "Medicine Name", "Scheduled Time", "Actual Time Taken", "Status")
        createSheetWithData(medSheet, medHeaders, meds.asSequence().map {
            listOf(it.date, it.medicineName, it.scheduledTime, it.actualTimeTaken ?: "", it.status)
        })

        // =========================
        // EXERCISE REPORT
        // =========================
        val exercises = db.exerciseLogDao().getExerciseLogsForUser(actualUserId)
        val exerciseSheet = workbook.createSheet("Exercise Report")
        val exerciseHeaders = listOf("Date", "Planned Time", "Exercise Name", "Status", "Time Used")
        createSheetWithData(exerciseSheet, exerciseHeaders, exercises.asSequence().map {
            listOf(it.date, it.plannedTime ?: "", it.exerciseName, it.status, it.timeUsed ?: "")
        })

        // =========================
        // SAVE REPORT
        // =========================
        val reportsDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
        val file = File(reportsDir, "HealthReport_${System.currentTimeMillis()}_user_$actualUserId.xlsx")

        FileOutputStream(file).use { output ->
            workbook.write(output)
            output.flush()
        }

        workbook.close() // safe close

        Log.d("ReportUtils", "Generated fresh report: ${file.absolutePath}")
        file
    }

    // =========================
    // DELETE OLD REPORTS
    // =========================
    private fun clearOldReports(context: Context) {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) return

        reportsDir.listFiles { file ->
            file.name.startsWith("HealthReport_") && file.name.endsWith(".xlsx")
        }?.forEach { file ->
            if (file.delete()) Log.d("ReportUtils", "Deleted old report: ${file.name}")
            else Log.w("ReportUtils", "Failed to delete old report: ${file.name}")
        }
    }

    // =========================
    // HELPER: CREATE SHEET
    // =========================
    private fun createSheetWithData(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        headers: List<String>,
        data: Sequence<List<String>>
    ) {
        var rowIndex = 0

        // Header row
        val headerRow: Row = sheet.createRow(rowIndex++)
        headers.forEachIndexed { colIndex, value ->
            val cell: Cell = headerRow.createCell(colIndex)
            cell.setCellValue(value)

            sheet.setColumnWidth(colIndex, 20 * 256) // 20 characters wide
        }

        // Data rows
        data.forEach { rowData ->
            val row: Row = sheet.createRow(rowIndex++)
            rowData.forEachIndexed { colIndex, value ->
                val cell: Cell = row.createCell(colIndex)
                cell.setCellValue(value)
            }
        }
    }
}
