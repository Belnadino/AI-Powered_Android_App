package com.example.healthcare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "caregiver_table")
data class CaregiverEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uid: String = "",  // Global unique ID
    val name: String,
    val email: String,
    val phoneNumber: String? = null,

    // Report sending preference
    val reportFrequency: Int = ReportFrequency.NONE,
    val lastReportSentAt: Long? = null
)
