package com.example.healthcare.database.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.healthcare.database.entities.UserEntity

@Dao
interface UserDao {

    @Insert
    suspend fun registerUser(user: UserEntity): Long

    @Query("SELECT * FROM user_table")
    suspend fun getAllUsers(): List<UserEntity>

    @Update
    suspend fun updateUser(user: UserEntity)

    // Fetch by email
    @Query("SELECT * FROM user_table WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    // Fetch by phone number
    @Query("SELECT * FROM user_table WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    // Fetch by UID (global unique ID)
    @Query("SELECT * FROM user_table WHERE uid = :uid LIMIT 1")
    suspend fun getUserByUid(uid: String): UserEntity?

    // Offline login using PIN (instead of password)
    @Query("""
        SELECT * FROM user_table
        WHERE (email = :emailOrPhone OR phoneNumber = :emailOrPhone)
        AND pin = :pin
        LIMIT 1
    """)
    suspend fun loginWithPin(emailOrPhone: String, pin: String): UserEntity?

    // Fetch by numeric ID
    @Query("SELECT * FROM user_table WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): UserEntity?


    // Fetch by either email or phone (for validation/lookup)
    @Query("SELECT * FROM user_table WHERE email = :emailOrPhone OR phoneNumber = :emailOrPhone LIMIT 1")
    suspend fun getUserByEmailOrPhone(emailOrPhone: String): UserEntity?

    // Fetch the single guest user (if exists)
    @Query("SELECT * FROM user_table WHERE isGuest = 1 LIMIT 1")
    suspend fun getGuestUser(): UserEntity?

    // Insert guest (reuse registerUser internally)
    @Insert
    suspend fun insertUser(user: UserEntity): Long

        @Query("""
    DELETE FROM user_table
    WHERE isGuest = 1
    AND userId NOT IN (
        SELECT MIN(userId) FROM user_table WHERE isGuest = 1
    )
    """)
        suspend fun deleteExtraGuests()
}
