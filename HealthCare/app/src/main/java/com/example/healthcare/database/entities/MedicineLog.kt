package com.example.healthcare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medication_log")
data class MedicineLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val planId: Int,
    val medicineName: String,
    val scheduledTime: String, // HH:mm
    val actualTimeTaken: String?, // HH:mm
    val date: String, // yyyy-MM-dd
    val status: String // "Taken", "Missed"
)