package com.example.healthcare.planner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.entities.Feedback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FeedbackViewModel(application: Application) : AndroidViewModel(application) {

    private val feedbackDao = AppDatabase.getDatabase(application).feedbackDao()

    // Expose LiveData from Room directly
    fun getUserFeedback(userId: Int): LiveData<List<Feedback>> {
        return feedbackDao.getUserFeedback(userId)
    }

    // Add feedback in background
    fun addFeedback(feedback: Feedback) {
        viewModelScope.launch(Dispatchers.IO) {
            feedbackDao.addFeedback(feedback)
        }
    }

    // Rate feedback in background
    fun rateFeedback(feedbackId: Int, rating: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            feedbackDao.rateAndCloseFeedback(feedbackId, rating)
        }
    }
}
