package com.example.healthcare.reminder

import android.content.Context
import com.example.healthcare.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

class ReminderWorker(private val context: Context) {

    /**
     * Schedule alarms for all plans from today onwards.
     */
    fun syncAlarmsForTodayAndFuture(userId: Int = 1) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val today = LocalDate.now()

            // --------------------
            // Meals
            // --------------------
            db.mealPlanDao().getTodayAndFuturePlans(userId, today.toString()).forEach { meal ->
                meal.mealTime?.let { timeString ->
                    val time = LocalTime.parse(timeString)
                    //val planDate = LocalDate.parse(meal.date) // must exist in Meal entity

                    val planDate = if (today.isBefore(LocalDate.parse(meal.startDate))) {
                        LocalDate.parse(meal.startDate)
                    } else if (today.isAfter(LocalDate.parse(meal.endDate))) {
                        LocalDate.parse(meal.endDate)
                    } else {
                        today
                    }

                    AlarmHelper.schedulePlanAlarm(
                        context,
                        meal.id,
                        "Meal",
                        meal.mealName,
                        planDate.year,
                        planDate.monthValue - 1, // Calendar month is 0-based
                        planDate.dayOfMonth,
                        time.hour,
                        time.minute
                    )
                }
            }

            // --------------------
            // Exercises
            // --------------------
            db.exercisePlanDao().getTodayAndFuturePlans(userId, today.toString()).forEach { ex ->
                ex.plannedTime?.let { timeString ->
                    val time = LocalTime.parse(timeString)
                    //val planDate = LocalDate.parse(ex.date)

                    val planDate = if (today.isBefore(LocalDate.parse(ex.startDate))) {
                        LocalDate.parse(ex.startDate)
                    } else if (today.isAfter(LocalDate.parse(ex.endDate))) {
                        LocalDate.parse(ex.endDate)
                    } else {
                        today
                    }

                    AlarmHelper.schedulePlanAlarm(
                        context,
                        ex.id,
                        "Exercise",
                        ex.exerciseName,
                        planDate.year,
                        planDate.monthValue - 1,
                        planDate.dayOfMonth,
                        time.hour,
                        time.minute
                    )
                }
            }

            // --------------------
            // Medications
            // --------------------
            db.medicinePlanDao().getPlansForUserAndDate(userId, today.toString()).forEach { med ->
                med.doseTime?.let { timeString ->
                    val time = LocalTime.parse(timeString)

                    // Use today if plan’s date has passed or is ongoing
                    val planDate = if (today.isBefore(LocalDate.parse(med.startDate))) {
                        LocalDate.parse(med.startDate)
                    } else if (today.isAfter(LocalDate.parse(med.endDate))) {
                        LocalDate.parse(med.endDate)
                    } else {
                        today
                    }

                    AlarmHelper.schedulePlanAlarm(
                        context,
                        med.id,
                        "Medication",
                        med.medicineName,
                        planDate.year,
                        planDate.monthValue - 1,
                        planDate.dayOfMonth,
                        time.hour,
                        time.minute
                    )
                }
            }
        }
    }
}
