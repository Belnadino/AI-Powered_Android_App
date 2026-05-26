package com.example.healthcare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a scheduled exercise item for a user.
 *
 * plannedTime: "HH:mm" (e.g. "07:00")
 * startDate / endDate: "yyyy-MM-dd"
 */
@Entity(tableName = "exercise_plan")
data class ExercisePlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val exerciseName: String,
    val plannedTime: String?,   // nullable to allow plans without a fixed time
    val startDate: String,      // yyyy-MM-dd
    val endDate: String         // yyyy-MM-dd
)

