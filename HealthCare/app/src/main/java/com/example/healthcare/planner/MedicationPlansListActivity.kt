package com.example.healthcare.planner

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.BaseActivity
import com.example.healthcare.R
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.entities.MedicineLog
import com.example.healthcare.database.entities.MedicinePlan
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

class MedicationPlansListActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MedicationPlansAdapter
    private var userId: Int = -1

    companion object {
        const val ADD_MEDICATION_REQUEST = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication_plans_list)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // RecyclerView
        recyclerView = findViewById(R.id.recycler_medication_plans)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MedicationPlansAdapter(
            onEditClick = { plan -> openEditPlan(plan) },
            onDeleteClick = { plan -> confirmDeletePlan(plan) }
        )
        recyclerView.adapter = adapter

        // User ID
        userId = intent.getIntExtra("USER_ID", -1)
            .takeIf { it != -1 }
            ?: getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getInt("logged_in_user_id", -1)

        if (userId != -1) loadPlans()
        else Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
    }

    private fun loadPlans() {
        val today = LocalDate.now().toString()
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            // Fetch all plans for the user
            val plans: List<MedicinePlan> = withContext(Dispatchers.IO) {
                db.medicinePlanDao().getPlansForUser(userId)
            }

            // Evaluate MISSED for plans whose time has passed
            plans.forEach { plan ->
                evaluateMissedStatus(plan, today)
            }

            //  Update adapter
            withContext(Dispatchers.Main) {
                adapter.submitList(plans)
                if (plans.isEmpty()) {
                    Toast.makeText(
                        this@MedicationPlansListActivity,
                        "No medication plans found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Helper to mark MISSED doses
    private fun evaluateMissedStatus(plan: MedicinePlan, today: String) {
        val now = LocalTime.now()
        val schedTime = try { LocalTime.parse(plan.doseTime) } catch (e: Exception) { null } ?: return

        if (now.isAfter(schedTime.plusMinutes(60))) {
            lifecycleScope.launch(Dispatchers.IO) {
                val exists = AppDatabase.getDatabase(this@MedicationPlansListActivity)
                    .medicineLogDao().exists(plan.userId, plan.id, plan.doseTime, today)

                if (!exists) {
                    AppDatabase.getDatabase(this@MedicationPlansListActivity)
                        .medicineLogDao().insert(
                            MedicineLog(
                                userId = plan.userId,
                                planId = plan.id,
                                medicineName = plan.medicineName,
                                scheduledTime = plan.doseTime,
                                actualTimeTaken = null,
                                date = today,
                                status = "Missed"
                            )
                        )
                }
            }
        }
    }

    private fun openEditPlan(plan: MedicinePlan) {
        val intent = Intent(this, AddMedicationActivity::class.java)
        intent.putExtra("PLAN_ID", plan.id)
        startActivityForResult(intent, ADD_MEDICATION_REQUEST)
    }

    private fun confirmDeletePlan(plan: MedicinePlan) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Plan")
            .setMessage("Are you sure you want to delete '${plan.medicineName}'?")
            .setPositiveButton("Delete") { _, _ -> deletePlan(plan) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePlan(plan: MedicinePlan) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            db.medicinePlanDao().delete(plan)
            withContext(Dispatchers.Main) {
                loadPlans()
                Toast.makeText(
                    this@MedicationPlansListActivity,
                    "${plan.medicineName} deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Refresh list when returning from Add/Edit
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_MEDICATION_REQUEST && resultCode == RESULT_OK) {
            loadPlans()
        }
    }
}
