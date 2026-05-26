package com.example.healthcare.database.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.healthcare.database.entities.MedicineLog

@Dao
interface MedicineLogDao {

    @Insert
    suspend fun insert(log: MedicineLog)

    @Query("SELECT * FROM medication_log WHERE userId = :userId ORDER BY date DESC, scheduledTime ASC")
    suspend fun getAllLogsForUser(userId: Int): List<MedicineLog>

    @Query("SELECT * FROM medication_log WHERE userId = :userId")
    suspend fun getMedicationLogsForUser(userId: Int): List<MedicineLog>

    @Query("SELECT * FROM medication_log WHERE userId = :userId AND date = :date")
    suspend fun getLogsForUserAndDate(userId: Int, date: String): List<MedicineLog>

    // check if log exists for a specific plan + time + date
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM medication_log
            WHERE userId = :userId
            AND planId = :planId
            AND scheduledTime = :scheduledTime
            AND date = :date
        )
    """)
    suspend fun exists(userId: Int, planId: Int, scheduledTime: String, date: String): Boolean
}
