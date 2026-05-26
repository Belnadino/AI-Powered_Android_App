package com.example.healthcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.entities.ExerciseLog
import com.example.healthcare.database.Dao.ExercisePlanDao
import com.example.healthcare.database.Dao.ExerciseLogDao
import com.example.healthcare.planner.ExerciseAdapter
import com.example.healthcare.planner.AddExercisePlanActivity
import com.example.healthcare.planner.ExerciseTimerActivity
import com.example.healthcare.planner.ExercisePlanListActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class ExerciseActivity : BaseActivity() {

    // UI components
    private lateinit var recyclerView: RecyclerView
    private lateinit var addPlanButton: Button

    private lateinit var viewPlansButton: Button
    private lateinit var startNotification: LinearLayout
    private lateinit var startSetting: LinearLayout
    private lateinit var startProfile: LinearLayout

    // Database DAOs
    private lateinit var planDao: ExercisePlanDao
    private lateinit var logDao: ExerciseLogDao

    private var loggedInUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Initialize UI components (SAFE)
        addPlanButton = findViewById(R.id.btn_add_plan)
        viewPlansButton = findViewById(R.id.btn_view_plans)
        recyclerView = findViewById(R.id.exerciseRecyclerView)
        startNotification = findViewById(R.id.btnNotification)
        startSetting = findViewById(R.id.btnSetting)
        startProfile = findViewById(R.id.btnProfile)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Bottom navigation listeners
        startNotification.setOnClickListener { openNotificationActivity() }
        startSetting.setOnClickListener { openSettingActivity() }
        startProfile.setOnClickListener { openProfileActivity() }

        // Database DAOs
        val db = AppDatabase.getDatabase(this)
        planDao = db.exercisePlanDao()
        logDao = db.exerciseLogDao()

        // Logged-in user
        loggedInUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getInt("logged_in_user_id", -1)

        if (loggedInUserId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Add new exercise plan
        addPlanButton.setOnClickListener {
            startActivity(Intent(this, AddExercisePlanActivity::class.java))
        }

        viewPlansButton.setOnClickListener {
            startActivity(Intent(this, ExercisePlanListActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadTodayPlans()
    }

    private fun loadTodayPlans() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        lifecycleScope.launch {
            val plans = withContext(Dispatchers.IO) {
                planDao.getTodayPlans(loggedInUserId, today)
            }

            val logs = withContext(Dispatchers.IO) {
                logDao.getLogsForUserAndDate(loggedInUserId, today)
            }

            // Auto-mark missed exercises
            withContext(Dispatchers.IO) {
                val nowMillis = System.currentTimeMillis()
                for (plan in plans) {
                    val exists = logs.any { it.planId == plan.id && it.date == today }
                    if (!exists && !plan.plannedTime.isNullOrBlank()) {
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            val plannedDateTime = sdf.parse("$today ${plan.plannedTime}")
                            val threeHoursLater = plannedDateTime?.time?.plus(3 * 60 * 60 * 1000) // add 4 hours
                            if (threeHoursLater != null && threeHoursLater < nowMillis) {
                                logDao.insertExerciseLog(
                                    ExerciseLog(
                                        id = 0,
                                        userId = loggedInUserId,
                                        planId = plan.id,
                                        exerciseName = plan.exerciseName,
                                        plannedTime = plan.plannedTime,
                                        date = today,
                                        status = "Missed",
                                        timeUsed = "00:00:00"
                                    )
                                )
                            }
                        } catch (_: Exception) {
                            // ignore parse errors
                        }
                    }
                }
            }

            val refreshedLogs = withContext(Dispatchers.IO) {
                logDao.getLogsForUserAndDate(loggedInUserId, today)
            }

            recyclerView.adapter = ExerciseAdapter(
                plans = plans,
                logs = refreshedLogs,
                onStart = { plan ->
                    val intent = Intent(
                        this@ExerciseActivity,
                        ExerciseTimerActivity::class.java
                    ).apply {
                        putExtra("planId", plan.id)
                        putExtra("exerciseName", plan.exerciseName)
                        putExtra("plannedTime", plan.plannedTime)
                    }
                    startActivity(intent)
                },
                onViewCompleted = { /* future feature */ }
            )
        }
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
