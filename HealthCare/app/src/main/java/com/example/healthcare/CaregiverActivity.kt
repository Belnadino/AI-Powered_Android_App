package com.example.healthcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.entities.CaregiverEntity
import com.example.healthcare.planner.CaregiverAdapter
import com.example.healthcare.planner.CaregiverViewModel
import com.example.healthcare.planner.CaregiverViewModelFactory
import com.example.healthcare.utils.ReportUtils
import com.example.healthcare.utils.EmailSender
import com.example.healthcare.workers.scheduleDailyReportWorker

class CaregiverActivity : BaseActivity() {

    private lateinit var viewModel: CaregiverViewModel
    private lateinit var adapter: CaregiverAdapter

    // Views
    private lateinit var recyclerCaregivers: RecyclerView
    private lateinit var btnAddCaregiver: Button
    private lateinit var btnSendTestReport: Button
    private lateinit var startNotification: LinearLayout
    private lateinit var startSetting: LinearLayout
    private lateinit var startProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_caregiver_list)

        initViews()
        setupRecyclerView()
        setupViewModel()
        setupToolbar()
        setupClickListeners()

        // Schedule daily report
        scheduleDailyReportWorker(this)
    }

    private fun initViews() {
        recyclerCaregivers = findViewById(R.id.recyclerCaregivers)
        btnAddCaregiver = findViewById(R.id.btnAddCaregiver)
        btnSendTestReport = findViewById(R.id.btnSendTestReport)
        startNotification = findViewById(R.id.btnNotification)
        startSetting = findViewById(R.id.btnSetting)
        startProfile = findViewById(R.id.btnProfile)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupClickListeners() {
        btnAddCaregiver.setOnClickListener {
            startActivity(Intent(this, AddEditCaregiverActivity::class.java))
        }

        btnSendTestReport.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val db = AppDatabase.getDatabase(this@CaregiverActivity)

                    // Fetch dynamic older adult ID
                    val olderAdultId = db.userDao().getAllUsers().firstOrNull()?.userId ?: 1

                    // Generate fresh report (old reports cleared automatically)
                    val reportFile = ReportUtils.generateExcelReport(this@CaregiverActivity, olderAdultId)

                    // Check file
                    if (!reportFile.exists() || reportFile.length() == 0L) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@CaregiverActivity,
                                "Generated report is empty. Cannot send.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    }

                    // Fetch all caregivers
                    val caregivers = db.caregiverDao().getAllCaregiversOnce()
                    if (caregivers.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@CaregiverActivity,
                                "No caregivers found to send report.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    }

                    // Send report to each caregiver
                    for (caregiver in caregivers) {

                        val email = caregiver.email
                        val name = caregiver.name ?: "Caregiver"
                        if (email.isNullOrBlank()) continue // skip if no email

                        EmailSender.sendEmail(
                            smtpHost = "smtp.gmail.com",
                            smtpPort = "587",
                            senderEmail = "healthcarep499@gmail.com",
                            senderPassword = "yxyicpdgrdnmmhxk",
                            recipientEmail = caregiver.email,
                            subject = "Healthcare Report",
                            body = "Hello ${caregiver.name}, This is a manual test report for older adult.",
                            attachmentFilePath = reportFile.absolutePath
                        )
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@CaregiverActivity,
                            "Reports sent successfully to all caregivers",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@CaregiverActivity,
                            "Failed to send reports: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        startNotification.setOnClickListener { openNotificationActivity() }
        startSetting.setOnClickListener { openSettingActivity() }
        startProfile.setOnClickListener { openProfileActivity() }
    }

    private fun setupRecyclerView() {
        adapter = CaregiverAdapter(
            onEditClick = { caregiver ->
                val intent = Intent(this, AddEditCaregiverActivity::class.java)
                intent.putExtra("caregiver_id", caregiver.id)
                startActivity(intent)
            },
            onDeleteClick = { caregiver ->
                showDeleteDialog(caregiver)
            }
        )

        recyclerCaregivers.layoutManager = LinearLayoutManager(this)
        recyclerCaregivers.adapter = adapter
    }

    private fun setupViewModel() {
        val dao = AppDatabase.getDatabase(this).caregiverDao()
        val factory = CaregiverViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[CaregiverViewModel::class.java]

        viewModel.caregivers.observe(this) { caregivers ->
            adapter.submitList(caregivers)
        }
    }

    private fun showDeleteDialog(caregiver: CaregiverEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete Caregiver")
            .setMessage("Are you sure you want to delete this caregiver?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCaregiver(caregiver)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openNotificationActivity() {
        startActivity(Intent(this, NotificationActivity::class.java))
    }

    private fun openSettingActivity() {
        startActivity(Intent(this, SettingActivity::class.java))
    }

    private fun openProfileActivity() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }
}
