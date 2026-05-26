package com.example.healthcare.database.Dao

import androidx.room.*
import com.example.healthcare.database.entities.CaregiverEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaregiverDao {

    /* -------------------- INSERT -------------------- */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaregiver(caregiver: CaregiverEntity)

    /* -------------------- UPDATE -------------------- */

    @Update
    suspend fun updateCaregiver(caregiver: CaregiverEntity)

    /* -------------------- DELETE -------------------- */

    @Delete
    suspend fun deleteCaregiver(caregiver: CaregiverEntity)

    /* -------------------- QUERIES -------------------- */

    @Query("SELECT * FROM caregiver_table ORDER BY name ASC")
    fun getAllCaregivers(): Flow<List<CaregiverEntity>>

    @Query("SELECT * FROM caregiver_table WHERE id = :id LIMIT 1")
    suspend fun getCaregiverById(id: Int): CaregiverEntity?

    @Query("SELECT * FROM caregiver_table WHERE email = :email LIMIT 1")
    suspend fun getCaregiverByEmail(email: String): CaregiverEntity?

    @Query("SELECT * FROM caregiver_table")
    suspend fun getAllCaregiversOnce(): List<CaregiverEntity>

    @Query("UPDATE caregiver_table SET lastReportSentAt = :timestamp WHERE id = :id")
    suspend fun updateLastSent(id: Int, timestamp: Long)
}
