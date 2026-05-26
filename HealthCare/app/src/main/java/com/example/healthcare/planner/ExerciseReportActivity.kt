package com.example.healthcare.planner

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.healthcare.BaseActivity
import com.example.healthcare.R
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.entities.ExerciseLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExerciseReportActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_report)

        // -------- Toolbar Setup --------
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // -------- Views --------
        recyclerView = findViewById(R.id.recyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // -------- Get User ID --------
        val userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            showEmpty("User not found")
            return
        }

        // -------- Load Logs from Database --------
        val db = AppDatabase.getDatabase(this)
        val logDao = db.exerciseLogDao()

        lifecycleScope.launch {
            val allLogs: List<ExerciseLog> = withContext(Dispatchers.IO) {
                logDao.getLogsByUser(userId)
            }

            if (allLogs.isEmpty()) {
                showEmpty("No exercise logs found")
                return@launch
            }

            // Group logs by date (latest first)
            val grouped = allLogs.groupBy { it.date }
                .map { ExerciseDay(it.key, it.value) }
                .sortedByDescending { it.date }

            // Set adapter
            recyclerView.adapter = ExerciseReportAdapter(grouped)
            tvEmpty.visibility = View.GONE
        }
    }

    // Helper to show empty message
    private fun showEmpty(message: String) {
        tvEmpty.visibility = View.VISIBLE
        tvEmpty.text = message
        recyclerView.visibility = View.GONE
    }
}
