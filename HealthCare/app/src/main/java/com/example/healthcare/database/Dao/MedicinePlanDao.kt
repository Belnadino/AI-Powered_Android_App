package com.example.healthcare.database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.healthcare.database.entities.MedicinePlan

@Dao
interface MedicinePlanDao {

    // Get all plans for a specific user on a given date, ordered by doseTime
    @Query("""
        SELECT * FROM medication_plan
        WHERE userId = :userId
          AND :date BETWEEN startDate AND endDate
        ORDER BY doseTime ASC
    """)
    suspend fun getPlansForUserAndDate(
        userId: Int,
        date: String
    ): List<MedicinePlan>

    // Get all plans for a user, ordered by doseTime
    @Query("""
        SELECT * FROM medication_plan
        WHERE userId = :userId
        ORDER BY doseTime ASC
    """)
    suspend fun getPlansForUser(
        userId: Int
    ): List<MedicinePlan>

    // Get today and future plans only, ordered by doseTime
    @Query("""
        SELECT * FROM medication_plan
        WHERE userId = :userId
          AND endDate >= :date
        ORDER BY doseTime ASC
    """)
    suspend fun getTodayAndFuturePlans(
        userId: Int,
        date: String
    ): List<MedicinePlan>

    // Get a single plan by ID
    @Query("""
        SELECT * FROM medication_plan
        WHERE id = :planId
        LIMIT 1
    """)
    suspend fun getPlanById(
        planId: Int
    ): MedicinePlan?

    // Insert new plan (auto-generates ID, replaces on conflict)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(
        plan: MedicinePlan
    ): Long

    // Update existing plan
    @Update
    suspend fun update(
        plan: MedicinePlan
    )

    // Delete plan
    @Delete
    suspend fun delete(
        plan: MedicinePlan
    )
}
