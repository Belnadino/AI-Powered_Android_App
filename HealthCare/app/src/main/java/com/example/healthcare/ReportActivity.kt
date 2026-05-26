package com.example.healthcare

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.healthcare.planner.ExerciseReportActivity
import com.example.healthcare.planner.MealReportActivity
import com.example.healthcare.planner.MedicationReportActivity
import com.google.android.material.card.MaterialCardView

class ReportActivity : BaseActivity() {

    // Report cards
    private lateinit var btnMealReport: MaterialCardView
    private lateinit var btnMedicationReport: MaterialCardView
    private lateinit var btnExerciseReport: MaterialCardView

    // Bottom navigation
    private lateinit var startNotification: LinearLayout
    private lateinit var startSetting: LinearLayout
    private lateinit var startProfile: LinearLayout

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Resolve user ID
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getInt("logged_in_user_id", -1)
        }

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Bind bottom navigation (SAFE)
        startNotification = findViewById(R.id.btnNotification)
        startSetting = findViewById(R.id.btnSetting)
        startProfile = findViewById(R.id.btnProfile)

        startNotification.setOnClickListener { openNotificationActivity() }
        startSetting.setOnClickListener { openSettingActivity() }
        startProfile.setOnClickListener { openProfileActivity() }

        // Bind report cards
        btnMealReport = findViewById(R.id.btnMealreport)
        btnMedicationReport = findViewById(R.id.btnMedicationreport)
        btnExerciseReport = findViewById(R.id.btnExercisereport)

        // Report card clicks
        btnMealReport.setOnClickListener { openReport("meal") }
        btnMedicationReport.setOnClickListener { openReport("medication") }
        btnExerciseReport.setOnClickListener { openReport("exercise") }
    }

    private fun openReport(type: String) {
        val intent = when (type) {
            "meal" -> Intent(this, MealReportActivity::class.java)
            "medication" -> Intent(this, MedicationReportActivity::class.java)
            "exercise" -> Intent(this, ExerciseReportActivity::class.java)
            else -> {
                Toast.makeText(this, "Unknown report type", Toast.LENGTH_SHORT).show()
                return
            }
        }

        intent.putExtra("USER_ID", userId)
        startActivity(intent)
    }

    private fun openNotificationActivity() {
        startActivity(Intent(this, NotificationActivity::class.java))
    }

    private fun openSettingActivity() {
        startActivity(Intent(this, SettingActivity::class.java))
    }

    private fun openProfileActivity() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }
}
