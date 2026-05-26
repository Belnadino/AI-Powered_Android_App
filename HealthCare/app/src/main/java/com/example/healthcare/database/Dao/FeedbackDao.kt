package com.example.healthcare.database.Dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.healthcare.database.entities.Feedback

@Dao
interface FeedbackDao {

    // Return LiveData for automatic updates
    @Query("""
        SELECT * FROM Feedback 
        WHERE userId = :userId 
        ORDER BY 
            CASE WHEN status = 'Open' THEN 0 ELSE 1 END,
            createdAt DESC
    """)
    fun getUserFeedback(userId: Int): LiveData<List<Feedback>>

    @Insert
    fun addFeedback(feedback: Feedback)

    @Update
    fun updateFeedback(feedback: Feedback)

    @Query("UPDATE Feedback SET rating = :rating, status = 'Closed' WHERE id = :id")
    fun rateAndCloseFeedback(id: Int, rating: Int)
}
