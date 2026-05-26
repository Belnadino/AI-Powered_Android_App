package com.example.healthcare.planner

import android.os.Bundle
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.healthcare.BaseActivity
import com.example.healthcare.R
import com.example.healthcare.database.entities.Feedback

class FeedbackDetailsActivity : BaseActivity() {

    private lateinit var viewModel: FeedbackViewModel
    private var feedbackId: Int = 0
    private var userId: Int = 0
    private lateinit var feedback: Feedback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback_details)

        // Toolbar setup
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, android.R.color.white))
        toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.title = "Details"

        // Retrieve feedbackId and userId from intent
        feedbackId = intent.getIntExtra("FEEDBACK_ID", 0)
        userId = intent.getIntExtra("USER_ID", 0)

        viewModel = ViewModelProvider(this).get(FeedbackViewModel::class.java)

        // Views
        val tvSubject: TextView = findViewById(R.id.tvDetailSubject)
        val tvDescription: TextView = findViewById(R.id.tvDetailDescription)
        val tvStatus: TextView = findViewById(R.id.tvDetailStatus)
        val tvSupportFeedback: TextView = findViewById(R.id.tvDetailSupportFeedback)
        val ratingBar: RatingBar = findViewById(R.id.rbDetailRating)
        val btnSubmitRating: Button = findViewById(R.id.btnSubmitRating)

        // Observe LiveData from ViewModel
        viewModel.getUserFeedback(userId).observe(this) { list ->
            feedback = list.firstOrNull { it.id == feedbackId } ?: return@observe

            // Bind UI
            tvSubject.text = feedback.subject
            tvDescription.text = feedback.description
            tvStatus.text = feedback.status
            tvSupportFeedback.text = feedback.supportFeedback ?: "No support feedback yet"

            ratingBar.rating = feedback.rating?.toFloat() ?: 0f

            // Allow rating only if complement or closed problem
            val canRate = feedback.canUserRate()
            ratingBar.setIsIndicator(!canRate)
            btnSubmitRating.isEnabled = canRate
        }

        // Rating listener
        btnSubmitRating.setOnClickListener {
            val rating = ratingBar.rating.toInt()
            if (rating == 0) {
                Toast.makeText(this, "Please select rating", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.rateFeedback(feedbackId, rating)
                Toast.makeText(this, "Feedback rated $rating stars", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
