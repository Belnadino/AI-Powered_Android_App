package com.example.healthcare.planner

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.BaseActivity
import com.example.healthcare.R
import com.example.healthcare.database.AppDatabase

class ExercisePlanListActivity : BaseActivity() {

    private var userId: Int = -1
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExercisePlanAdapter

    private val viewModel: ExercisePlanViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.getDatabase(applicationContext)
                val repo = ExerciseRepository(
                    db.exercisePlanDao(),
                    db.exerciseLogDao()
                )
                @Suppress("UNCHECKED_CAST")
                return ExercisePlanViewModel(repo) as T
            }
        }
    }

    companion object {
        private const val ADD_EDIT_PLAN_REQUEST = 1001
        const val EXTRA_PLAN_ID = "PLAN_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_plans_list)

        // Get userId from intent or SharedPreferences
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getInt("logged_in_user_id", 1)
        }

        setupToolbar()
        setupRecyclerView()
        observeData()

        // Load all exercise plans
        viewModel.loadAllPlans(userId)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_exercise_plans)

        adapter = ExercisePlanAdapter(
            onEditClick = { plan ->
                val intent = Intent(this, AddExercisePlanActivity::class.java)
                intent.putExtra(EXTRA_PLAN_ID, plan.id) // pass plan ID for editing
                startActivityForResult(intent, ADD_EDIT_PLAN_REQUEST)
            },
            onDeleteClick = { plan ->
                viewModel.deleteExercisePlan(userId, plan.id)
                Toast.makeText(this, "${plan.exerciseName} deleted", Toast.LENGTH_SHORT).show()
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

    // Refresh the list after returning from Add/Edit
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_EDIT_PLAN_REQUEST && resultCode == RESULT_OK) {
            viewModel.loadAllPlans(userId)
        }
    }
}
