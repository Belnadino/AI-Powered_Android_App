package com.example.healthcare.planner

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.R
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.Dao.MedicineLogDao
import com.example.healthcare.database.entities.MedicineLog
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.example.healthcare.BaseActivity

class MedicationReportActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var logDao: MedicineLogDao
    private lateinit var tvEmpty: TextView
    private val logs = mutableListOf<MedicineLog>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication_report)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Medication Report"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // RecyclerView & Empty Text
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvEmpty.visibility = View.GONE

        // Initialize DAO
        val db = AppDatabase.getDatabase(this)
        logDao = db.medicineLogDao()

        val userId = intent.getIntExtra("USER_ID", 1)

        // Load logs
        lifecycleScope.launch {
            val fetchedLogs = logDao.getAllLogsForUser(userId)
            if (fetchedLogs.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
            } else {
                logs.addAll(fetchedLogs)
                recyclerView.adapter = MedicationReportAdapter(logs)
            }
        }
    }
}
