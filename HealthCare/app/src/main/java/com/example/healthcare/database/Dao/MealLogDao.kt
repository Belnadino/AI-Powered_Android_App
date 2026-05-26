package com.example.healthcare.database.Dao

import androidx.room.*
import com.example.healthcare.database.entities.MealLog

@Dao
interface MealLogDao {

    // Insert new log
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMealLog(mealLog: MealLog): Long

    @Update
    suspend fun updateMealLog(mealLog: MealLog)

    @Delete
    suspend fun deleteMealLog(mealLog: MealLog)


    // ------------------
    // MAIN QUERIES
    // ------------------

    // All logs sorted newest → oldest
    @Query("""
        SELECT * FROM meal_logs 
        WHERE userId = :userId 
        ORDER BY logDate DESC, mealTime ASC
    """)
    suspend fun getLogsByUser(userId: Int): List<MealLog>

    @Query("SELECT * FROM meal_logs WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAllLogs(userId: Int): List<MealLog>

    // Get logs for a specific date
    @Query("""
        SELECT * FROM meal_logs 
        WHERE userId = :userId 
          AND logDate = :date
        ORDER BY mealTime ASC
    """)
    suspend fun getLogsByDate(userId: Int, date: String): List<MealLog>

    @Query("SELECT * FROM meal_logs WHERE userId = :userId")
    suspend fun getMealLogsForUser(userId: Int): List<MealLog>

    // Check if a meal was logged today
    @Query("""
        SELECT * FROM meal_logs 
        WHERE userId = :userId
          AND mealPlanId = :planId
          AND logDate = :date
        LIMIT 1
    """)
    suspend fun getLogForMeal(userId: Int, planId: Int, date: String): MealLog?

    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND mealPlanId = :planId LIMIT 1")
    suspend fun getLogForPlan(userId: Int, planId: Int): MealLog?

    @Query("SELECT * FROM meal_logs WHERE userId = :userId AND mealPlanId = :planId AND logDate = :today LIMIT 1")
    suspend fun getLogForPlanToday(userId: Int, planId: Int, today: String): MealLog?
}
