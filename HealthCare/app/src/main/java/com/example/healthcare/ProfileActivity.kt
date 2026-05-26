package com.example.healthcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.Dao.UserDao
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvBloodType: TextView
    private lateinit var tvMedicalConditions: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button
    private lateinit var changePinButton: Button

    // Bottom navigation
    private lateinit var startNotification: LinearLayout
    private lateinit var startSetting: LinearLayout
    private lateinit var startProfile: LinearLayout

    private var loggedInUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        initViews()
        setupToolbar()
        setupBottomNavigation()
        setupButtons()

        // Load logged in user ID
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        loggedInUserId = prefs.getInt("logged_in_user_id", -1)
    }

    private fun initViews() {
        tvName = findViewById(R.id.tv_name)
        tvEmail = findViewById(R.id.tv_email)
        tvPhone = findViewById(R.id.tv_phone)
        tvAge = findViewById(R.id.tv_age)
        tvHeight = findViewById(R.id.tv_height)
        tvWeight = findViewById(R.id.tv_weight)
        tvBloodType = findViewById(R.id.tv_blood_type)
        tvMedicalConditions = findViewById(R.id.tv_medical_conditions)
        btnEditProfile = findViewById(R.id.btn_edit_profile)
        btnLogout = findViewById(R.id.btn_logout)
        changePinButton = findViewById(R.id.changePinButton)

        startNotification = findViewById(R.id.btnNotification)
        startSetting = findViewById(R.id.btnSetting)
        startProfile = findViewById(R.id.btnProfile)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupBottomNavigation() {
        startNotification.setOnClickListener { startActivity(Intent(this, NotificationActivity::class.java)) }
        startSetting.setOnClickListener { startActivity(Intent(this, SettingActivity::class.java)) }
        // No action for current tab
        startProfile.setOnClickListener { /* Do nothing */ }
    }

    private fun setupButtons() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("userId", loggedInUserId)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            prefs.edit().remove("logged_in_user_id").apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        changePinButton.setOnClickListener {
            startActivity(Intent(this, ChangePinActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (loggedInUserId != -1) {
            lifecycleScope.launch {
                val user = userDao.getUserById(loggedInUserId)
                user?.let {
                    tvName.text = it.name.ifEmpty { "Unknown" }
                    tvEmail.text = it.email.ifEmpty { "Not set" }
                    tvPhone.text = it.phoneNumber.ifEmpty { "Not set" }
                    tvAge.text = "Age: ${it.age ?: ""}"
                    tvHeight.text = "Height: ${it.height ?: ""} cm"
                    tvWeight.text = "Weight: ${it.weight ?: ""} kg"
                    tvBloodType.text = "Blood Type: ${it.bloodType ?: ""}"
                    tvMedicalConditions.text = "Medical Conditions: ${it.medicalConditions ?: ""}"
                }
            }
        }
    }
}
