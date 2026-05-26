package com.example.healthcare.planner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcare.database.entities.ExerciseLog
import com.example.healthcare.database.entities.ExercisePlan
import kotlinx.coroutines.launch

class ExercisePlanViewModel(
    private val repo: ExerciseRepository
) : ViewModel() {

    private val _todayPlans = MutableLiveData<List<ExercisePlan>>()
    val todayPlans: LiveData<List<ExercisePlan>> = _todayPlans

    private val _allPlans = MutableLiveData<List<ExercisePlan>>()
    val allPlans: LiveData<List<ExercisePlan>> = _allPlans

    private val _exerciseLogs = MutableLiveData<List<ExerciseLog>>()
    val exerciseLogs: LiveData<List<ExerciseLog>> = _exerciseLogs

    private val _allLogs = MutableLiveData<List<ExerciseLog>>()
    val allLogs: LiveData<List<ExerciseLog>> = _allLogs

    /** Load today's exercise plans and their logs */
    fun loadTodayPlans(userId: Int) {
        viewModelScope.launch {
            val plans = repo.getTodayPlans(userId)
            val logs = repo.getTodayLogs(userId)

            _todayPlans.postValue(plans)
            _exerciseLogs.postValue(logs)
        }
    }

    /** Load all exercise plans for user */
    fun loadAllPlans(userId: Int) {
        viewModelScope.launch {
            val plans = repo.getAllPlans(userId)
            _allPlans.postValue(plans)
        }
    }

    /** Load all exercise logs for user */
    fun loadAllLogs(userId: Int) {
        viewModelScope.launch {
            val logs = repo.getAllLogs(userId)
            _allLogs.postValue(logs)
        }
    }

    /** Delete an exercise plan */
    fun deleteExercisePlan(userId: Int, planId: Int) {
        viewModelScope.launch {
            val plan = _allPlans.value?.find { it.id == planId } ?: return@launch
            repo.deletePlan(plan)

            loadAllPlans(userId)
            loadTodayPlans(userId) // keep today screen in sync
        }
    }

    /** Mark an exercise as completed */
    fun markExerciseCompleted(userId: Int, planId: Int, timeUsed: String) {
        viewModelScope.launch {
            val plan = _todayPlans.value?.find { it.id == planId } ?: return@launch
            repo.markExerciseCompleted(userId, plan, timeUsed)

            // Refresh logs and plans
            val logs = repo.getTodayLogs(userId)
            _exerciseLogs.postValue(logs)

            val plans = repo.getTodayPlans(userId)
            _todayPlans.postValue(plans)
        }
    }

    /** Auto-mark missed exercises */
    fun autoMarkMissed(userId: Int) {
        viewModelScope.launch {
            repo.markMissedExercises(userId)

            val logs = repo.getTodayLogs(userId)
            _exerciseLogs.postValue(logs)

            val plans = repo.getTodayPlans(userId)
            _todayPlans.postValue(plans)
        }
    }

    /** Get plan by ID (suspend) */
    suspend fun getPlanById(planId: Int): ExercisePlan? {
        return repo.getPlanById(planId)
    }

    /** Insert or update an exercise plan */
    fun saveExercisePlan(plan: ExercisePlan) {
        viewModelScope.launch {
            repo.insertOrUpdatePlan(plan)

            // Refresh both lists
            loadAllPlans(plan.userId)
            loadTodayPlans(plan.userId)
        }
    }
}
