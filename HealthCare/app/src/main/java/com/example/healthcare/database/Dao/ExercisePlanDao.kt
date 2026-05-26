package com.example.healthcare.database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.healthcare.database.entities.ExercisePlan

@Dao
interface ExercisePlanDao {

    // Get today's active exercise plans
    @Query("""
        SELECT * FROM exercise_plan
        WHERE userId = :userId
        AND :date BETWEEN startDate AND endDate
        ORDER BY plannedTime ASC
    """)
    suspend fun getTodayPlans(userId: Int, date: String): List<ExercisePlan>

    // Get all exercise plans for a user
    @Query("""
        SELECT * FROM exercise_plan
        WHERE userId = :userId
        ORDER BY startDate DESC
    """)
    suspend fun getAllPlans(userId: Int): List<ExercisePlan>

    // Get single plan by ID
    @Query("""
        SELECT * FROM exercise_plan
        WHERE id = :planId
        LIMIT 1
    """)
    suspend fun getPlanById(planId: Int): ExercisePlan?

    // Insert or update exercise plan
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercisePlan(plan: ExercisePlan)

    // Delete exercise plan
    @Delete
    suspend fun deleteExercisePlan(plan: ExercisePlan)

    @Query("""
    SELECT * FROM exercise_plan
    WHERE userId = :userId
      AND :date BETWEEN startDate AND endDate
    ORDER BY plannedTime ASC""")
    suspend fun getTodayAndFuturePlans(userId: Int, date: String): List<ExercisePlan>
}
