package com.example.healthcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.Dao.MedicinePlanDao
import com.example.healthcare.database.Dao.MedicineLogDao
import com.example.healthcare.database.entities.MedicineLog
import com.example.healthcare.planner.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class MedicationActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var planDao: MedicinePlanDao
    private lateinit var logDao: MedicineLogDao
    private lateinit var adapter: MedicationAdapter

    private var loggedInUserId: Int = -1

    companion object { const val ADD_MEDICATION_REQUEST = 101 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication_page)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        recyclerView = findViewById(R.id.medicationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = AppDatabase.getDatabase(this)
        planDao = db.medicinePlanDao()
        logDao = db.medicineLogDao()

        loggedInUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getInt("logged_in_user_id", -1)
        if (loggedInUserId == -1) Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()

        findViewById<LinearLayout>(R.id.btnNotification).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnSetting).setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btn_add_plan).setOnClickListener {
            val intent = Intent(this, AddMedicationActivity::class.java)
            startActivityForResult(intent, ADD_MEDICATION_REQUEST)
        }

        findViewById<Button>(R.id.btn_view_plans).setOnClickListener {
            val intent = Intent(this, MedicationPlansListActivity::class.java)
            intent.putExtra("USER_ID", loggedInUserId)
            startActivity(intent)
        }

        adapter = MedicationAdapter(
            planTimes = mutableListOf(),
            userId = loggedInUserId,
            logDao = logDao,
            scope = lifecycleScope
        )
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        if (loggedInUserId != -1) loadTodayPlans()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_MEDICATION_REQUEST && resultCode == RESULT_OK) {
            loadTodayPlans() // Refresh immediately after add/edit
        }
    }

    private fun loadTodayPlans() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Calendar.getInstance().time)

        lifecycleScope.launch {
            val todaysPlans = withContext(Dispatchers.IO) {
                planDao.getPlansForUserAndDate(loggedInUserId, today)
            }

            // 🔹 Evaluate MISSED before passing to adapter
            withContext(Dispatchers.IO) {
                val now = LocalTime.now()
                todaysPlans.forEach { plan ->
                    val schedTime = try { LocalTime.parse(plan.doseTime) } catch (_: Exception) { null }
                        ?: return@forEach
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
                    }
                }
            }

            // Fetch updated logs
            val todayLogs = withContext(Dispatchers.IO) {
                logDao.getLogsForUserAndDate(loggedInUserId, today)
            }

            val planTimes = todaysPlans.map { PlanTimeItem(it, it.doseTime) }
            adapter.updatePlanTimes(planTimes, todayLogs)
        }
    }
}
