package com.example.healthcare.planner

import com.example.healthcare.database.Dao.MealLogDao
import com.example.healthcare.database.Dao.MealPlanDao
import com.example.healthcare.database.entities.MealLog
import com.example.healthcare.database.entities.MealPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MealRepository(
    private val mealPlanDao: MealPlanDao,
    private val mealLogDao: MealLogDao
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ------------------------------
    // Plans
    // ------------------------------
    suspend fun getTodayPlans(userId: Int): List<MealPlan> = withContext(Dispatchers.IO) {
        val today = dateFormat.format(Date())
        mealPlanDao.getTodayPlans(userId, today)
    }

    suspend fun getAllPlans(userId: Int): List<MealPlan> = withContext(Dispatchers.IO) {
        mealPlanDao.getAllPlans(userId)
    }

    suspend fun getPlanById(planId: Int): MealPlan? = withContext(Dispatchers.IO) {
        mealPlanDao.getPlanById(planId)
    }


    suspend fun deletePlan(plan: MealPlan) = withContext(Dispatchers.IO) {
        mealPlanDao.deleteMealPlan(plan)
    }

    // ------------------------------
    // Logs
    // ------------------------------
    suspend fun getTodayLogs(userId: Int): List<MealLog> = withContext(Dispatchers.IO) {
        val today = dateFormat.format(Date())
        mealLogDao.getLogsByDate(userId, today)
    }

    suspend fun getLogForPlanToday(userId: Int, planId: Int): MealLog? = withContext(Dispatchers.IO) {
        val today = dateFormat.format(Date())
        mealLogDao.getLogForPlanToday(userId, planId, today)
    }

    // Generic log retrieval
    suspend fun getAllLogs(userId: Int): List<MealLog> = withContext(Dispatchers.IO) {
        mealLogDao.getLogsByUser(userId)
    }

    suspend fun insertOrUpdatePlan(plan: MealPlan): Long = withContext(Dispatchers.IO) {
        return@withContext if (plan.id != 0) {
            mealPlanDao.updateMealPlan(plan)
            plan.id.toLong() // return the existing ID
        } else {
            mealPlanDao.insertMealPlan(plan) // insert returns new ID
        }
    }

    // ------------------------------
    // Update or insert a log for a meal
    // ------------------------------
    suspend fun markMealStatus(userId: Int, plan: MealPlan, status: String) = withContext(Dispatchers.IO) {
        val today = dateFormat.format(Date())
        val existing = mealLogDao.getLogForMeal(userId, plan.id, today)
        val timestamp = System.currentTimeMillis()

        if (existing != null) {
            mealLogDao.updateMealLog(
                existing.copy(
                    status = status,
                    timestamp = timestamp
                )
            )
        } else {
            val newLog = MealLog(
                userId = userId,
                mealPlanId = plan.id,
                mealName = plan.mealName,
                description = plan.description,
                mealTime = plan.mealTime,
                logDate = today,
                status = status,
                timestamp = timestamp
            )
            mealLogDao.insertMealLog(newLog)
        }
    }
}
