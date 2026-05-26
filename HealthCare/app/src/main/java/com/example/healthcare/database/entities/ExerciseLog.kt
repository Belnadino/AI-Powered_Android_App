package com.example.healthcare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Storing daily execution status of an exercise plan.
 *
 * status: "Completed" or "Missed"
 * timeUsed: formatted as "HH:mm:ss" (e.g. "00:18:42")
 */
@Entity(tableName = "exercise_log")
data class ExerciseLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val planId: Int,
    val exerciseName: String,
    val plannedTime: String?,       // "HH:mm"
    val date: String,               // "yyyy-MM-dd"
    val status: String,             // "Completed" or "Missed"
    val timeUsed: String            // "HH:mm:ss" (00:00:00 for Missed)
)
