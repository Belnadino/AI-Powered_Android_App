package com.example.healthcare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "today_notifications")
data class TodayNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val category: String,
    val name: String,
    val date: String, // yyyy-MM-dd
    val time: String  // HH:mm
)
