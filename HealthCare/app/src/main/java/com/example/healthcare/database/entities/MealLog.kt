package com.example.healthcare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val userId: Int,
    val mealPlanId: Int,

    val mealName: String,
    val description: String,
    val mealTime: String,

    val logDate: String,        // YYYY-MM-DD
    val status: String,         // "eaten" or "missed"

    val timestamp: Long
)
