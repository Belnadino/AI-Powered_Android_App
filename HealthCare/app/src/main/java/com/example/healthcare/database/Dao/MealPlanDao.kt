package com.example.healthcare.database.Dao

import androidx.room.*
import com.example.healthcare.database.entities.MealPlan

@Dao
interface MealPlanDao {

    // Insert or replace
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlan): Long

    //  Update
    @Update
    suspend fun updateMealPlan(mealPlan: MealPlan)

    //  Delete
    @Delete
    suspend fun deleteMealPlan(mealPlan: MealPlan)

    // Get ALL plans for a user (ordered by meal time)
    @Query("""
        SELECT * FROM meal_plans
        WHERE userId = :userId
        ORDER BY mealTime ASC
    """)
    suspend fun getAllPlans(
        userId: Int
    ): List<MealPlan>

    // Get TODAY'S active plans (ordered by meal time)
    @Query("""
        SELECT * FROM meal_plans
        WHERE userId = :userId
          AND :today BETWEEN startDate AND endDate
        ORDER BY mealTime ASC
    """)
    suspend fun getTodayPlans(
        userId: Int,
        today: String
    ): List<MealPlan>

    // Get single plan by ID
    @Query("""
        SELECT * FROM meal_plans
        WHERE id = :planId
        LIMIT 1
    """)
    suspend fun getPlanById(
        planId: Int
    ): MealPlan?

    // Get TODAY & FUTURE plans (ordered by meal time)
    @Query("""
        SELECT * FROM meal_plans
        WHERE userId = :userId
          AND endDate >= :date
        ORDER BY mealTime ASC
    """)
    suspend fun getTodayAndFuturePlans(
        userId: Int,
        date: String
    ): List<MealPlan>
}
