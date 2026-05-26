package com.example.healthcare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_plans")
data class MealPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val mealName: String,
    val description: String,
    val mealTime: String,
    val startDate: String,
    val endDate: String
)
