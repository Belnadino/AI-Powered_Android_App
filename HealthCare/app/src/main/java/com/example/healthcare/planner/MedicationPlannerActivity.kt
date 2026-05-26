package com.example.healthcare.planner

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.BaseActivity
import com.example.healthcare.R
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.Dao.MedicinePlanDao
import com.example.healthcare.database.Dao.MedicineLogDao
import com.example.healthcare.database.entities.MedicineLog
import com.example.healthcare.database.entities.MedicinePlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

class MedicationPlannerActivity : BaseActivity() {

    private lateinit var planDao: MedicinePlanDao
    private lateinit var logDao: MedicineLogDao
    private lateinit var recyclerView: RecyclerView
    private var adapter: MedicationAdapter? = null

    private val logs = mutableListOf<MedicineLog>()
    private var loggedInUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication_page)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Medication Planner"
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.medicationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = AppDatabase.getDatabase(this)
        planDao = db.medicinePlanDao()
        logDao = db.medicineLogDao()

        loggedInUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getInt("logged_in_user_id", -1)

        if (loggedInUserId != -1) loadTodayPlans()
        else Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
    }

    private fun loadTodayPlans() {
        val today = LocalDate.now().toString()
        val now = LocalTime.now()

        lifecycleScope.launch(Dispatchers.IO) {
            // 1️⃣ Get today's plans
            val plans = planDao.getPlansForUserAndDate(loggedInUserId, today)

            // 2️⃣ Check MISSED status for all plans
            plans.forEach { plan ->
                evaluateMissedStatus(plan, today, now)
            }

            // 3️⃣ Get today's logs after MISSED update
            val todayLogs = logDao.getLogsForUserAndDate(loggedInUserId, today)
            logs.clear()
            logs.addAll(todayLogs)

            // 4️⃣ Flatten plans into PlanTimeItems
            val planTimes = plans.map { PlanTimeItem(it, it.doseTime) }.toMutableList()

            // 5️⃣ Update UI on main thread
            withContext(Dispatchers.Main) {
                if (adapter == null) {
                    adapter = MedicationAdapter(
                        planTimes = planTimes,
                        userId = loggedInUserId,
                        logDao = logDao,
                        scope = lifecycleScope
                    )
                    recyclerView.adapter = adapter
                } else {
                    adapter?.updatePlanTimes(planTimes, logs)
                }

                if (planTimes.isEmpty()) {
                    Toast.makeText(
                        this@MedicationPlannerActivity,
                        "No medication planned today",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // 🔹 Centralized MISSED evaluation helper
    private suspend fun evaluateMissedStatus(plan: MedicinePlan, today: String, now: LocalTime) {
        if (today !in plan.startDate..plan.endDate || plan.status != "ACTIVE") return

        val schedTime = try { LocalTime.parse(plan.doseTime) } catch (e: Exception) { null } ?: return

        if (now.isAfter(schedTime.plusMinutes(60))) {
            val exists = logDao.exists(plan.userId, plan.id, plan.doseTime, today)
            if (!exists) {
                logDao.insert(
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
            planDao.update(plan.copy(status = "MISSED"))
        }
    }

    override fun onResume() {
        super.onResume()
        if (loggedInUserId != -1) {
            loadTodayPlans() // refresh on return from Add/Edit
        }
    }
}
