package com.example.healthcare

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.entities.MealPlan
import com.example.healthcare.planner.MealRepository
import com.example.healthcare.planner.MealViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.healthcare.database.entities.MealLog
import com.example.healthcare.reminder.AlarmHelper

class AddEditMealPlanActivity : BaseActivity() {

    private var userId: Int = 1
    private var planId: Int? = null

    private lateinit var etName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etTime: EditText
    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var btnSave: Button

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
        setContentView(R.layout.activity_add_edit_meal_plan)

        loadUserId()
        initViews()
        setupToolbar()
        setupDatePickers()
        loadExistingPlanIfEditing()

        btnSave.setOnClickListener { savePlan() }
    }

    private fun loadUserId() {
        userId = intent.getIntExtra("USER_ID", -1).takeIf { it != -1 }
            ?: getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getInt("logged_in_user_id", 1)
    }

    private fun initViews() {
        etName = findViewById(R.id.et_meal_name)
        etDescription = findViewById(R.id.et_description)
        etTime = findViewById(R.id.et_meal_time)
        etStartDate = findViewById(R.id.et_start_date)
        etEndDate = findViewById(R.id.et_end_date)
        btnSave = findViewById(R.id.btn_save_plan)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupDatePickers() {
        etStartDate.setOnClickListener { showDatePicker(etStartDate) }
        etEndDate.setOnClickListener {
            if (etStartDate.text.isNullOrEmpty()) {
                Toast.makeText(this, "Select start date first", Toast.LENGTH_SHORT).show()
            } else {
                val minDate = dateFormat.parse(etStartDate.text.toString())?.time
                showDatePicker(etEndDate, minDate)
            }
        }
    }

    private fun loadExistingPlanIfEditing() {
        val id = intent.getIntExtra("planId", -1)
        if (id == -1) return

        lifecycleScope.launch {
            val plan = viewModel.getPlanById(id)
            plan?.let {
                etName.setText(it.mealName)
                etDescription.setText(it.description)
                etTime.setText(it.mealTime)
                etStartDate.setText(it.startDate)
                etEndDate.setText(it.endDate)
                planId = it.id
            }
        }
    }

    private fun savePlan() {
        val name = etName.text.toString().trim()
        val desc = etDescription.text.toString().trim()
        val time = etTime.text.toString().trim()
        val startDate = etStartDate.text.toString().trim()
        val endDate = etEndDate.text.toString().trim()

        if (name.isEmpty() || desc.isEmpty() || time.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val plan = MealPlan(
                id = planId ?: 0,
                userId = userId,
                mealName = name,
                description = desc,
                mealTime = time,
                startDate = startDate,
                endDate = endDate
            )

            val finalPlan = if (planId != null) {
                db.mealPlanDao().updateMealPlan(plan)
                db.mealPlanDao().getPlanById(planId!!)
            } else {
                val newId = db.mealPlanDao().insertMealPlan(plan).toInt()
                db.mealPlanDao().getPlanById(newId)
            }

            finalPlan?.let { planSaved ->
                val todayStr = java.time.LocalDate.now().toString()
                val schedTime = try { java.time.LocalTime.parse(planSaved.mealTime) } catch (_: Exception) { null }

                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayDate = sdf.parse(todayStr)
                val start = sdf.parse(planSaved.startDate)
                val end = sdf.parse(planSaved.endDate)

                if (todayDate != null && !todayDate.before(start) && !todayDate.after(end) && schedTime != null) {
                    val now = java.time.LocalTime.now()
                    val exists = db.mealLogDao().getLogForMeal(userId, planSaved.id, todayStr) != null

                    if (now.isAfter(schedTime.plusHours(2)) && !exists) {
                        // Insert MISSED log
                        db.mealLogDao().insertMealLog(
                            MealLog(
                                userId = planSaved.userId,
                                mealPlanId = planSaved.id,
                                mealName = planSaved.mealName,
                                description = planSaved.description,
                                mealTime = planSaved.mealTime,
                                logDate = todayStr,
                                status = "missed",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    } else if (!exists) {
                        // Schedule alarm only if not missed
                        val today = java.time.LocalDate.now()
                        AlarmHelper.schedulePlanAlarm(
                            context = this@AddEditMealPlanActivity,
                            planId = planSaved.id,
                            planType = "Meal",
                            planName = planSaved.mealName,
                            year = today.year,
                            month = today.monthValue - 1, // Calendar months are 0-based
                            day = today.dayOfMonth,
                            hour = schedTime.hour,
                            minute = schedTime.minute
                        )
                    }
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddEditMealPlanActivity, "Meal plan saved", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }



    private fun showDatePicker(editText: EditText, minDate: Long? = null) {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                editText.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        minDate?.let { dialog.datePicker.minDate = it }
        dialog.show()
    }
}