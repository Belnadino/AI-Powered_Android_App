package com.example.healthcare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,

    val uid: String = "", // Global unique ID
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val pin: String = "",  // Offline login PIN
    val age: Int? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val bloodType: String? = null,
    val medicalConditions: String? = null,

    val careManagerUid: String? = null, // Link to caregiver UID
    val isGuest: Boolean = false
)
