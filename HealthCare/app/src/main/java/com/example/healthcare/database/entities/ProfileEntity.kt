package com.example.healthcare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_table")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,

    val fullName: String,
    val email: String,
    val phone: String,
    val age: Int,
    val heightCm: Int,
    val weightKg: Int,
    val bloodType: String,
    val medicalConditions: String,
    val careManagerName: String,
    val careManagerEmail: String
)