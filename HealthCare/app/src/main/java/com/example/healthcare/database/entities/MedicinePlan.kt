package com.example.healthcare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medication_plan")
data class MedicinePlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val medicineName: String,
    val doseTime: String, // HH:mm
    val startDate: String, // yyyy-MM-dd
    val endDate: String, // yyyy-MM-dd
    val status: String = "ACTIVE" // "ACTIVE" or "MISSED"
)
