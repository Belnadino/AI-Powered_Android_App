package com.example.healthcare.planner

import com.example.healthcare.database.Dao.ExerciseLogDao
import com.example.healthcare.database.Dao.ExercisePlanDao
import com.example.healthcare.database.entities.ExerciseLog
import com.example.healthcare.database.entities.ExercisePlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExerciseRepository(
    private val exercisePlanDao: ExercisePlanDao,
    private val exerciseLogDao: ExerciseLogDao
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /** Get today's active exercise plans */
    suspend fun getTodayPlans(userId: Int): List<ExercisePlan> =
        withContext(Dispatchers.IO) {
            val today = dateFormat.format(Date())
            exercisePlanDao.getTodayPlans(userId, today)
        }

    /** Get today's exercise logs */
    suspend fun getTodayLogs(userId: Int): List<ExerciseLog> =
        withContext(Dispatchers.IO) {
            val today = dateFormat.format(Date())
            exerciseLogDao.getLogsForUserAndDate(userId, today)
        }

    /** Mark exercise as completed */
    suspend fun markExerciseCompleted(
        userId: Int,
        plan: ExercisePlan,
        timeUsed: String
    ) = withContext(Dispatchers.IO) {

        val today = dateFormat.format(Date())
        val existing = exerciseLogDao.getLogForExercise(userId, plan.id, today)

        val log = ExerciseLog(
            id = existing?.id ?: 0,
            userId = userId,
            planId = plan.id,
            exerciseName = plan.exerciseName,
            plannedTime = plan.plannedTime,
            date = today,
            status = "completed",
            timeUsed = timeUsed
        )

        exerciseLogDao.insertExerciseLog(log)
    }

    /** Get all exercise logs for user */
    suspend fun getAllLogs(userId: Int): List<ExerciseLog> =
        exerciseLogDao.getLogsByUser(userId)

    /** Get all exercise plans for user */
    suspend fun getAllPlans(userId: Int): List<ExercisePlan> =
        exercisePlanDao.getAllPlans(userId)

    /** Get plan by ID */
    suspend fun getPlanById(planId: Int): ExercisePlan? =
        exercisePlanDao.getPlanById(planId)

    /** Insert or update exercise plan */
    suspend fun insertOrUpdatePlan(plan: ExercisePlan) =
        exercisePlanDao.insertExercisePlan(plan)

    /** Delete exercise plan */
    suspend fun deletePlan(plan: ExercisePlan) =
        exercisePlanDao.deleteExercisePlan(plan)

    /** Auto-mark missed exercises (end of day) */
    suspend fun markMissedExercises(userId: Int) =
        withContext(Dispatchers.IO) {

            val today = dateFormat.format(Date())
            val plans = exercisePlanDao.getTodayPlans(userId, today)

            for (plan in plans) {
                val existing =
                    exerciseLogDao.getLogForExercise(userId, plan.id, today)

                if (existing == null) {
                    val log = ExerciseLog(
                        userId = userId,
                        planId = plan.id,
                        exerciseName = plan.exerciseName,
                        plannedTime = plan.plannedTime,
                        date = today,
                        status = "missed",
                        timeUsed = "00:00:00"
                    )
                    exerciseLogDao.insertExerciseLog(log)
                }
            }
        }
}
