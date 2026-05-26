package com.example.healthcare

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RatingBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.R
import com.example.healthcare.database.entities.Feedback
import com.google.android.material.tabs.TabLayout
import com.example.healthcare.planner.*

class FeedbackActivity : BaseActivity() {

    private lateinit var viewModel: FeedbackViewModel
    private lateinit var adapter: FeedbackAdapter
    private val userId = 1

    private var fullFeedbackList: List<Feedback> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feedback_page)

        // Toolbar setup
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // ViewModel
        viewModel = ViewModelProvider(this).get(FeedbackViewModel::class.java)

        // RecyclerView setup
        val rvFeedback: RecyclerView = findViewById(R.id.rvFeedbackList)
        adapter = FeedbackAdapter(mutableListOf()) { feedback, rating ->
            viewModel.rateFeedback(feedback.id, rating)
        }
        rvFeedback.layoutManager = LinearLayoutManager(this)
        rvFeedback.adapter = adapter

        // Tabs: All / Open / Closed
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText("All"))
        tabLayout.addTab(tabLayout.newTab().setText("Open"))
        tabLayout.addTab(tabLayout.newTab().setText("Closed"))


        // Observe LiveData only once
        viewModel.getUserFeedback(userId).observe(this) { list ->
            fullFeedbackList = list
            applyTabFilter(tabLayout.selectedTabPosition, fullFeedbackList)
        }

        // Tab selection listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                applyTabFilter(tab.position, fullFeedbackList)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Add Feedback button
        findViewById<Button>(R.id.btnAddFeedback).setOnClickListener {
            val dialog = AddFeedbackDialog(userId)
            dialog.show(supportFragmentManager, "AddFeedback")
        }
    }

    private fun applyTabFilter(tabPosition: Int, list: List<Feedback>) {
        val filtered = when (tabPosition) {
            1 -> list.filter { it.status == "Open" }
            2 -> list.filter { it.status == "Closed" }
            else -> list
        }

        // Update adapter list
        adapter.feedbackList.apply {
            clear()
            addAll(filtered)
        }
        adapter.notifyDataSetChanged()
    }

    // Optional: Rating dialog for swipe-to-rate
    private fun showRatingDialog(feedback: Feedback) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.rating_dialog, null)
        val ratingBar: RatingBar = dialogView.findViewById(R.id.rbDialogRating)
        val btnSubmit: Button = dialogView.findViewById(R.id.btnSubmitRatingDialog)

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        btnSubmit.setOnClickListener {
            viewModel.rateFeedback(feedback.id, ratingBar.rating.toInt())
            dialog.dismiss()
        }
        dialog.show()
    }
}
