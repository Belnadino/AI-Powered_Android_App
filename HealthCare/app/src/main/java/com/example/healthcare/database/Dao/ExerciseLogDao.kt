package com.example.healthcare.database.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.healthcare.database.entities.ExerciseLog

@Dao
interface ExerciseLogDao {

    // Insert Completed / Missed log (replace if exists)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLog(log: ExerciseLog)

    // Get all logs for a user (ordered most recent first)
    @Query("SELECT * FROM exercise_log WHERE userId = :userId ORDER BY date DESC, planId ASC")
    suspend fun getLogsByUser(userId: Int): List<ExerciseLog>

    // Get all distinct log dates for a user
    @Query("SELECT DISTINCT date FROM exercise_log WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllDatesForUser(userId: Int): List<String>

    @Query("SELECT * FROM exercise_log WHERE userId = :userId")
    suspend fun getExerciseLogsForUser(userId: Int): List<ExerciseLog>

    // Get all logs for a specific user on a specific day
    @Query("""
        SELECT * FROM exercise_log
        WHERE userId = :userId
        AND date = :date
    """)
    suspend fun getLogsForUserAndDate(userId: Int, date: String): List<ExerciseLog>



    // Get log for a single exercise plan on a specific day
    @Query("""
        SELECT * FROM exercise_log
        WHERE userId = :userId
        AND planId = :planId
        AND date = :date
        LIMIT 1
    """)
    suspend fun getLogForExercise(userId: Int, planId: Int, date: String): ExerciseLog?
}
