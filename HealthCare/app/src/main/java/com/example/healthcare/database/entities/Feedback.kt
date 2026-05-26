package com.example.healthcare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Feedback(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val name: String,
    val subject: String,
    val description: String,
    val category: String,          // "Problem" or "Complement"
    var rating: Int? = null,       // nullable
    val status: String = "Open",   // "Open" or "Closed"
    val supportFeedback: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
{
    // helper function to check if user can rate
    fun canUserRate(): Boolean {
        return category == "Complement" || (category == "Problem" && status == "Closed")
    }
}
