package com.example.healthcare.planner

import androidx.lifecycle.*
import com.example.healthcare.database.entities.MealLog
import com.example.healthcare.database.entities.MealPlan
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MealWithLog(
    val plan: MealPlan,
    val log: MealLog? = null
)

class MealViewModel(
    private val repo: MealRepository
) : ViewModel() {

    private val _todayMeals = MutableLiveData<List<MealWithLog>>()
    val todayMeals: LiveData<List<MealWithLog>> = _todayMeals

    private val _allPlans = MutableLiveData<List<MealPlan>>()
    val allPlans: LiveData<List<MealPlan>> = _allPlans

    // ------------------------------
    // Load today’s meals + logs
    // ------------------------------
    fun loadTodayMeals(userId: Int) {
        viewModelScope.launch {
            val plans = repo.getTodayPlans(userId)
            val logs = repo.getTodayLogs(userId)

            _todayMeals.postValue(
                plans.map { plan ->
                    MealWithLog(
                        plan = plan,
                        log = logs.firstOrNull { it.mealPlanId == plan.id }
                    )
                }
            )
        }
    }

    // ------------------------------
    // Load all plans
    // ------------------------------
    fun loadAllPlans(userId: Int) {
        viewModelScope.launch {
            _allPlans.postValue(repo.getAllPlans(userId))
        }
    }

    // ------------------------------
    // Delete plan
    // ------------------------------
    fun deleteMealPlan(userId: Int, planId: Int) {
        viewModelScope.launch {
            repo.getPlanById(planId)?.let {
                repo.deletePlan(it)
                loadTodayMeals(userId)
                loadAllPlans(userId)
            }
        }
    }

    // ------------------------------
    // Mark meal as eaten
    // ------------------------------
    fun markMealEaten(userId: Int, planId: Int) {
        viewModelScope.launch {
            repo.getPlanById(planId)?.let {
                repo.markMealStatus(userId, it, "eaten")
                loadTodayMeals(userId)
            }
        }
    }

    suspend fun getPlanById(planId: Int): MealPlan? =
        repo.getPlanById(planId)

    // ------------------------------
    // Auto-mark missed meals (>2h)
    // ------------------------------
    fun autoMarkMissedMeals(userId: Int) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val threshold = 2 * 60 * 60 * 1000L // 2 hours

            repo.getTodayPlans(userId).forEach { plan ->
                // Skip if log already exists for today
                if (repo.getLogForPlanToday(userId, plan.id) != null) return@forEach

                val mealTime = getPlanTimestamp(plan) ?: return@forEach
                if (now > mealTime + threshold) {
                    repo.markMealStatus(userId, plan, "missed")
                }
            }

            loadTodayMeals(userId)
        }
    }

    // ------------------------------
    // Helper: date + time → millis
    // ------------------------------
    private fun getPlanTimestamp(plan: MealPlan): Long? {
        return try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            ) ?: return null

            val start = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(plan.startDate)
            val end = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(plan.endDate)

            if (today.before(start) || today.after(end)) return null

            val parsedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(plan.mealTime) ?: return null

            val calendar = Calendar.getInstance().apply { time = today }
            val timeCal = Calendar.getInstance().apply { time = parsedTime }

            calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            calendar.timeInMillis
        } catch (_: Exception) {
            null
        }
    }


}
