package com.example.healthcare.planner

import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.healthcare.BaseActivity
import com.example.healthcare.R
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.entities.ExerciseLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExerciseTimerActivity : BaseActivity() {

    private lateinit var chronometer: Chronometer
    private lateinit var btnFinish: Button
    private lateinit var btnCancel: Button
    private lateinit var tvExerciseName: TextView

    private var startTime: Long = 0L
    private var running = false

    private var exercisePlanId: Int = -1
    private var exerciseName: String = ""
    private var userId: Int = -1
    private var plannedTime: String? = null

    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_timer)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Exercise Timer"
        toolbar.setNavigationOnClickListener { finish() }

        chronometer = findViewById(R.id.chronometer)
        btnFinish = findViewById(R.id.btnFinish)
        btnCancel = findViewById(R.id.btnCancel)
        tvExerciseName = findViewById(R.id.tvExerciseName)

        // Get data from intent
        exercisePlanId = intent.getIntExtra("planId", -1)
        exerciseName = intent.getStringExtra("exerciseName") ?: ""
        plannedTime = intent.getStringExtra("plannedTime")
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getInt("logged_in_user_id", -1)

        tvExerciseName.text = exerciseName

        // Start timer immediately
        startChronometer()

        // Finish Button
        btnFinish.setOnClickListener {
            stopChronometer()
            saveLog("Completed")
        }

        // Cancel Button
        btnCancel.setOnClickListener {
            stopChronometer()
            Toast.makeText(this, "Exercise cancelled", Toast.LENGTH_SHORT).show()
            finish() // Go back to planner
        }
    }

    private fun startChronometer() {
        startTime = SystemClock.elapsedRealtime()
        chronometer.base = startTime
        chronometer.start()
        running = true
    }

    private fun stopChronometer() {
        if (running) {
            chronometer.stop()
            running = false
        }
    }

    private fun saveLog(status: String) {
        val elapsedMillis = SystemClock.elapsedRealtime() - startTime
        val seconds = elapsedMillis / 1000 % 60
        val minutes = elapsedMillis / 1000 / 60 % 60
        val hours = elapsedMillis / 1000 / 3600

        val timeUsed = if (status == "Completed") {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            "00:00:00" // For missed exercises
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        lifecycleScope.launch(Dispatchers.IO) {
            val log = ExerciseLog(
                userId = userId,
                planId = exercisePlanId,
                exerciseName = exerciseName,
                plannedTime = plannedTime,
                date = today,
                status = status,
                timeUsed = timeUsed
            )
            db.exerciseLogDao().insertExerciseLog(log)

            runOnUiThread {
                Toast.makeText(
                    this@ExerciseTimerActivity,
                    "Exercise $status! Time used: $timeUsed",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }
}
