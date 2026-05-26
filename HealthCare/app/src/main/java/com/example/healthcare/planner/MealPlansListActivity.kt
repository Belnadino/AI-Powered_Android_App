package com.example.healthcare.planner

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.planner.MealPlanAdapter
import com.example.healthcare.planner.MealRepository
import com.example.healthcare.planner.MealViewModel
import com.example.healthcare.R
import com.example.healthcare.AddEditMealPlanActivity
import com.example.healthcare.BaseActivity

class MealPlansListActivity : BaseActivity() {

    private var userId: Int = -1 // now Int
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MealPlanAdapter

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_plans_list)

        // Get userId from intent or SharedPreferences
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getInt("logged_in_user_id", 1) // default 1
        }

        setupToolbar()
        setupRecyclerView()
        observeData()

        // load plans
        viewModel.loadAllPlans(userId)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_meal_plans)
        adapter = MealPlanAdapter(
            onEditClick = { plan ->
                val intent = Intent(this, AddEditMealPlanActivity::class.java)
                intent.putExtra("planId", plan.id)
                startActivity(intent)
            },
            onDeleteClick = { plan ->
                viewModel.deleteMealPlan(userId, plan.id)
                Toast.makeText(this, "${plan.mealName} deleted", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun observeData() {
        viewModel.allPlans.observe(this) { plans ->
            adapter.submitList(plans)
        }
    }
}
