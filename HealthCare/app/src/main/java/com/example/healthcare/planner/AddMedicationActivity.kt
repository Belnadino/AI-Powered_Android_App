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
import com.example.healthcare.database.entities.MedicineLog
import com.example.healthcare.database.entities.MedicinePlan
import com.example.healthcare.reminder.AlarmHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class AddMedicationActivity : BaseActivity() {

    private lateinit var etMedicineName: EditText
    private lateinit var etTime: EditText
    private lateinit var etStartDate: EditText
    private lateinit var etEndDate: EditText
    private lateinit var btnSave: Button
    private lateinit var toolbar: Toolbar

    private var planId: Int? = null
    private var userId: Int = -1
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medication_plan)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        etMedicineName = findViewById(R.id.etMedicineName)
        etTime = findViewById(R.id.etTime1)
        etStartDate = findViewById(R.id.etStartDate)
        etEndDate = findViewById(R.id.etEndDate)
        btnSave = findViewById(R.id.btnSavePlan)

        userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getInt("logged_in_user_id", -1)

        planId = intent.getIntExtra("PLAN_ID", -1).takeIf { it != -1 }
        planId?.let { loadExistingPlan(it) }

        etStartDate.setOnClickListener { showDatePicker(etStartDate) }
        etEndDate.setOnClickListener {
            if (etStartDate.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please select start date first", Toast.LENGTH_SHORT).show()
            } else {
                val start = dateFormat.parse(etStartDate.text.toString())
                showDatePicker(etEndDate, start?.time)
            }
        }

        btnSave.setOnClickListener { savePlan() }
    }

    private fun loadExistingPlan(id: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val plan = db.medicinePlanDao().getPlanById(id)
            plan?.let {
                runOnUiThread {
                    etMedicineName.setText(it.medicineName)
                    etTime.setText(it.doseTime)
                    etStartDate.setText(it.startDate)
                    etEndDate.setText(it.endDate)
                }
            }
        }
    }

    private fun savePlan() {
        val name = etMedicineName.text.toString().trim()
        val time = etTime.text.toString().trim()
        val startDate = etStartDate.text.toString()
        val endDate = etEndDate.text.toString()

        if (name.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || time.isBlank()) {
            Toast.makeText(this, "Please fill required fields and time", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val plan = MedicinePlan(
                id = planId ?: 0,
                userId = userId,
                medicineName = name,
                doseTime = time,
                startDate = startDate,
                endDate = endDate
            )

            val finalPlan = if (planId != null) {
                db.medicinePlanDao().update(plan)
                db.medicinePlanDao().getPlanById(planId!!)
            } else {
                val newId = db.medicinePlanDao().insert(plan).toInt()
                db.medicinePlanDao().getPlanById(newId)
            }

            finalPlan?.let { planSaved ->
                val today = java.time.LocalDate.now().toString()
                val schedTime = try { LocalTime.parse(planSaved.doseTime) } catch (_: Exception) { null }

                schedTime?.let { st ->
                    val now = LocalTime.now()
                    // If overdue > 1h, mark as MISSED
                    if (now.isAfter(st.plusMinutes(60))) {
                        val exists = db.medicineLogDao().exists(planSaved.userId, planSaved.id, planSaved.doseTime, today)
                        if (!exists) {
                            db.medicineLogDao().insert(
                                MedicineLog(
                                    userId = planSaved.userId,
                                    planId = planSaved.id,
                                    medicineName = planSaved.medicineName,
                                    scheduledTime = planSaved.doseTime,
                                    actualTimeTaken = null,
                                    date = today,
                                    status = "Missed"
                                )
                            )
                        }
                    } else {
                        // Schedule alarm only if not missed
                        val today = java.time.LocalDate.now()
                        AlarmHelper.schedulePlanAlarm(
                            context = this@AddMedicationActivity,
                            planId = planSaved.id,
                            planType = "Medication",
                            planName = planSaved.medicineName,
                            year = today.year,
                            month = today.monthValue - 1, // Calendar months are 0-based
                            day = today.dayOfMonth,
                            hour = st.hour,
                            minute = st.minute
                        )
                    }
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddMedicationActivity, "Medication plan saved", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun showDatePicker(editText: EditText, minDate: Long? = null) {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
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
