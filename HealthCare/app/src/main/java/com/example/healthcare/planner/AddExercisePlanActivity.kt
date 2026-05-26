package com.example.healthcare.planner

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.healthcare.BaseActivity
import com.example.healthcare.R
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.entities.ExercisePlan
import com.example.healthcare.reminder.AlarmHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class AddExercisePlanActivity : BaseActivity() {

    private lateinit var etExerciseName: EditText
    private lateinit var etPlannedTime: EditText
    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var btnSave: Button
    private lateinit var toolbar: Toolbar

    private var userId: Int = -1
    private var editingPlanId: Int? = null
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_exercise_plan)

        // ---- Toolbar ----
        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { finish() }

        // ---- Initialize UI ----
        etExerciseName = findViewById(R.id.etExerciseName)
        etPlannedTime = findViewById(R.id.etPlannedTime)
        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        btnSave = findViewById(R.id.btnSave)

        // ---- Get logged-in user ID ----
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getInt("logged_in_user_id", -1)

        // ---- Date picker listeners ----
        etStartDate.setOnClickListener { showDatePicker(etStartDate) }
        etEndDate.setOnClickListener { showDatePicker(etEndDate) }

        // ---- Check if editing an existing plan ----
        editingPlanId = intent.getIntExtra("PLAN_ID", -1).takeIf { it != -1 }

        // Fetch existing plan and populate fields
        editingPlanId?.let { planId ->
            lifecycleScope.launch(Dispatchers.IO) {
                val plan = db.exercisePlanDao().getPlanById(planId)
                plan?.let {
                    runOnUiThread {
                        etExerciseName.setText(it.exerciseName)
                        etPlannedTime.setText(it.plannedTime)
                        etStartDate.setText(it.startDate)
                        etEndDate.setText(it.endDate)
                    }
                }
            }
        }

        // ---- Save button click ----
        btnSave.setOnClickListener {
            val name = etExerciseName.text.toString().trim()
            val time = etPlannedTime.text.toString().trim()
            val start = etStartDate.text.toString().trim()
            val end = etEndDate.text.toString().trim()

            if (name.isEmpty() || time.isEmpty() || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            savePlan(name, time, start, end)
        }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                editText.setText(sdf.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun savePlan(name: String, time: String, start: String, end: String) {
        val plan = ExercisePlan(
            id = editingPlanId ?: 0, // 0 = new plan, existing ID = update
            userId = userId,
            exerciseName = name,
            plannedTime = time,
            startDate = start,
            endDate = end
        )

        lifecycleScope.launch(Dispatchers.IO) {
            // Cancel old alarm if updating
            editingPlanId?.let { oldId ->
                AlarmHelper.cancelPlanAlarm(this@AddExercisePlanActivity, oldId)
            }

            // Insert or update the plan
            db.exercisePlanDao().insertExercisePlan(plan)

            // Schedule alarm for this plan
            try {
                val today = java.time.LocalDate.now()
                val localTime = LocalTime.parse(time)
                AlarmHelper.schedulePlanAlarm(
                    context = this@AddExercisePlanActivity,
                    planId = plan.id,
                    planType = "Exercise",
                    planName = name,
                    year = today.year,
                    month = today.monthValue - 1, // Calendar months are 0-based
                    day = today.dayOfMonth,
                    hour = localTime.hour,
                    minute = localTime.minute
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            runOnUiThread {
                Toast.makeText(this@AddExercisePlanActivity, "Exercise Plan Saved", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK) // Notify caller to refresh list
                finish()
            }
        }
    }
}
