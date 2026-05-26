package com.example.healthcare.database.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.healthcare.database.entities.ProfileEntity

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profile_table WHERE userId = :userId LIMIT 1")
    suspend fun getProfileByUserId(userId: Int): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)
}