package com.example.healthcare

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import android.widget.LinearLayout
import com.example.healthcare.reminder.AlarmHelper
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.android.material.card.MaterialCardView
import com.example.healthcare.database.AppDatabase
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Clean up duplicate guest users (KEEP ONLY ONE)
        lifecycleScope.launch {
            AppDatabase.getDatabase(this@MainActivity)
                .userDao()
                .deleteExtraGuests()
        }

        val startChatCard: MaterialCardView = findViewById(R.id.btnChatAssistance)
        val startMedicationCard: MaterialCardView = findViewById(R.id.btnMedication)
        val startExerciseCard: MaterialCardView = findViewById(R.id.btnExercise)
        val startMealCard: MaterialCardView = findViewById(R.id.btnMeal)
        val startFeedbackCard: MaterialCardView = findViewById(R.id.btnFeedback)
        val startReportCard: MaterialCardView = findViewById(R.id.btnReport)
        val startCaregiverCard: MaterialCardView = findViewById(R.id.btnCaregiver)
        val startNotification: LinearLayout = findViewById(R.id.btnNotification)
        val startSetting: LinearLayout = findViewById(R.id.btnSetting)
        val startProfile: LinearLayout = findViewById(R.id.btnProfile)
        // Schedule daily alarm at 08:00



        AlarmHelper.scheduleDailyAlarm(applicationContext, 8, 0)

        startChatCard.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
        startMedicationCard.setOnClickListener {
            val intent = Intent(this, MedicationActivity::class.java)
            startActivity(intent)
        }
        startExerciseCard.setOnClickListener {
            val intent = Intent(this, ExerciseActivity::class.java)
            startActivity(intent)
        }
        startMealCard.setOnClickListener {
            val intent = Intent(this, MealActivity::class.java)
            startActivity(intent)
        }
        startFeedbackCard.setOnClickListener {
            val intent = Intent(this, FeedbackActivity::class.java)
            startActivity(intent)
        }

        startReportCard.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
        }

        startCaregiverCard.setOnClickListener {
            val intent = Intent(this, CaregiverActivity::class.java)
            startActivity(intent)
        }

        startNotification.setOnClickListener {
            openNotificationActivity()
        }

        startSetting.setOnClickListener {
            openSettingActivity()
        }
        startProfile.setOnClickListener {
            openProfileActivity()
        }
    }
    private fun openNotificationActivity() {
        val intent = Intent(this, NotificationActivity::class.java)
        startActivity(intent)
    }

    private val settingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                recreate()
            }
        }

    private fun openSettingActivity() {
        val intent = Intent(this, SettingActivity::class.java)
        settingsLauncher.launch(intent)
    }
    private fun openProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

}