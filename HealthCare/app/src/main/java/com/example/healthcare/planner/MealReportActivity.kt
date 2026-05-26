package com.example.healthcare.planner

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.BaseActivity
import com.example.healthcare.R
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.entities.MealLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MealReportActivity : BaseActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: MealLogAdapter
    private var userId: Int = -1 // Changed to Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_report)

        // Toolbar setup
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // RecyclerView setup
        recycler = findViewById(R.id.recycler_report)
        adapter = MealLogAdapter()
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)

        // Get user ID from intent or SharedPreferences
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getInt("logged_in_user_id", 1) // default 1 for testing
        }

        // Load logs
        loadMealLogs()
    }

    private fun loadMealLogs() {
        val db = AppDatabase.Companion.getDatabase(applicationContext)

        lifecycleScope.launch(Dispatchers.IO) {
            // Fetch logs using Int userId
            val logs: List<MealLog> = db.mealLogDao().getAllLogs(userId)
            withContext(Dispatchers.Main) {
                adapter.submitList(logs)
            }
        }
    }
}