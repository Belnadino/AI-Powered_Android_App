package com.example.healthcare

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.entities.TodayNotification
import com.example.healthcare.planner.NotificationAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationActivity : BaseActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private var userId: Int = -1

    private lateinit var startNotification: LinearLayout
    private lateinit var startSetting: LinearLayout
    private lateinit var startProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        initViews()
        setupToolbar()
        setupRecyclerView()
        loadUserId()
        loadNotifications()
        setupNavigationClicks()
    }

    private fun initViews() {
        recycler = findViewById(R.id.recycler_notifications)
        startNotification = findViewById(R.id.btnNotification)
        startSetting = findViewById(R.id.btnSetting)
        startProfile = findViewById(R.id.btnProfile)
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter()
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)
    }

    private fun loadUserId() {
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getInt("logged_in_user_id", 1) // default to 1
        }
    }

    private fun loadNotifications() {
        val db = AppDatabase.getDatabase(applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            val notifications: List<TodayNotification> =
                db.todayNotificationDao().getAllNotifications(userId)
            withContext(Dispatchers.Main) {
                adapter.submitList(notifications)
            }
        }
    }

    private fun setupNavigationClicks() {
        // Avoid reopening the same activity
        startNotification.setOnClickListener { /* maybe show a toast instead */ }
        startSetting.setOnClickListener { startActivity(Intent(this, SettingActivity::class.java)) }
        startProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
    }
}
