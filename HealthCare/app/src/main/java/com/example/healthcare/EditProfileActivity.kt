package com.example.healthcare

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.Dao.UserDao
import com.example.healthcare.database.entities.UserEntity
import kotlinx.coroutines.launch

class EditProfileActivity : BaseActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAge: EditText
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var etBloodType: EditText
    private lateinit var etMedicalConditions: EditText

    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    private var userId: Int = -1
    private var currentUser: UserEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Views
        etName = findViewById(R.id.et_name)
        etEmail = findViewById(R.id.et_email)
        etPhone = findViewById(R.id.et_phone)
        etAge = findViewById(R.id.et_age)
        etHeight = findViewById(R.id.et_height)
        etWeight = findViewById(R.id.et_weight)
        etBloodType = findViewById(R.id.et_blood_type)
        etMedicalConditions = findViewById(R.id.et_medical_conditions)

        btnSave = findViewById(R.id.btn_save)
        btnCancel = findViewById(R.id.btn_cancel)

        // Database
        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        // Get current user
        userId = intent.getIntExtra("userId", -1)
        if (userId == -1) {
            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
            userId = prefs.getInt("logged_in_user_id", -1)
        }

        lifecycleScope.launch { loadUserData() }

        btnSave.setOnClickListener { lifecycleScope.launch { saveUserData() } }
        btnCancel.setOnClickListener { finish() }
    }

    private suspend fun loadUserData() {
        if (userId == -1) return
        currentUser = userDao.getUserById(userId)
        currentUser?.let { u ->
            etName.setText(u.name)
            etEmail.setText(u.email)
            etPhone.setText(u.phoneNumber)
            etAge.setText(u.age?.toString() ?: "")
            etHeight.setText(u.height?.toString() ?: "")
            etWeight.setText(u.weight?.toString() ?: "")
            etBloodType.setText(u.bloodType ?: "")
            etMedicalConditions.setText(u.medicalConditions ?: "")
        }
    }

    private suspend fun saveUserData() {
        if (userId == -1) return
        val updated = (currentUser ?: userDao.getUserById(userId) ?: return).copy(
            name = etName.text.toString(),
            email = etEmail.text.toString(),
            phoneNumber = etPhone.text.toString(),
            age = etAge.text.toString().toIntOrNull(),
            height = etHeight.text.toString().toFloatOrNull(),
            weight = etWeight.text.toString().toFloatOrNull(),
            bloodType = etBloodType.text.toString().ifEmpty { null },
            medicalConditions = etMedicalConditions.text.toString().ifEmpty { null }
        )

        userDao.updateUser(updated)

        Toast.makeText(this@EditProfileActivity, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
