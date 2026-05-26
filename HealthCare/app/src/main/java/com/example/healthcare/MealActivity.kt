package com.example.healthcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.planner.*

class MealActivity : BaseActivity() {

    private var userId: Int = -1
    private lateinit var adapter: TodayMealAdapter

    // Bottom navigation
    private lateinit var startNotification: LinearLayout
    private lateinit var startSetting: LinearLayout
    private lateinit var startProfile: LinearLayout

    private val viewModel: MealViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.getDatabase(applicationContext)
                val repo = MealRepository(db.mealPlanDao(), db.mealLogDao())
                @Suppress("UNCHECKED_CAST")
                return MealViewModel(repo) as T
            }
        }
    }

    private val addMealLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.loadTodayMeals(userId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal)

        // Resolve userId
        userId = intent.getIntExtra("USER_ID", -1).takeIf { it != -1 }
            ?: getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getInt("logged_in_user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Bottom navigation
        startNotification = findViewById(R.id.btnNotification)
        startSetting = findViewById(R.id.btnSetting)
        startProfile = findViewById(R.id.btnProfile)

        setupToolbar()
        setupRecyclerView()
        setupAddButton()
        setupExistingPlansButton()
        observeLiveData()

        // Auto-mark missed meals only once on create
        //viewModel.autoMarkMissedMeals(userId)
        viewModel.loadTodayMeals(userId)

        // Bottom navigation listeners
        startNotification.setOnClickListener { openNotificationActivity() }
        startSetting.setOnClickListener { openSettingActivity() }
        startProfile.setOnClickListener { openProfileActivity() }
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        val recycler: RecyclerView = findViewById(R.id.recycler_today_meals)
        adapter = TodayMealAdapter(
            onMarkEatenClick = { meal ->
                viewModel.markMealEaten(userId, meal.plan.id)
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun setupAddButton() {
        val btnAdd: Button = findViewById(R.id.btn_add_plan)
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddEditMealPlanActivity::class.java)
            intent.putExtra("USER_ID", userId)
            addMealLauncher.launch(intent)
        }
    }

    private fun setupExistingPlansButton() {
        val btnPlans: Button = findViewById(R.id.btn_view_plans)
        btnPlans.setOnClickListener {
            val intent = Intent(this, MealPlansListActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
    }

    private fun observeLiveData() {
        viewModel.todayMeals.observe(this) { meals ->
            adapter.submitList(meals)
            if (meals.isEmpty()) {
                Toast.makeText(this, "No meals scheduled today", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openNotificationActivity() = startActivity(Intent(this, NotificationActivity::class.java))
    private fun openSettingActivity() = startActivity(Intent(this, SettingActivity::class.java))
    private fun openProfileActivity() = startActivity(Intent(this, ProfileActivity::class.java))

    override fun onResume() {
        super.onResume()
        // Only reload today's meals; do NOT auto-mark again
        viewModel.autoMarkMissedMeals(userId)
        viewModel.loadTodayMeals(userId)
    }
}
