package com.example.healthcare

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.Dao.UserDao
import kotlinx.coroutines.launch

class ResetPinActivity : BaseActivity() {

    private lateinit var emailOrPhoneEditText: EditText
    private lateinit var newPinEditText: EditText
    private lateinit var confirmPinEditText: EditText
    private lateinit var resetButton: Button

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_pin)

        // Initialize views
        emailOrPhoneEditText = findViewById(R.id.emailOrPhone)
        newPinEditText = findViewById(R.id.newPin) // still named newPassword in XML
        confirmPinEditText = findViewById(R.id.confirmPin)
        resetButton = findViewById(R.id.resetButton)

        // Initialize Room database
        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        // Toolbar back button
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Reset button click
        resetButton.setOnClickListener {
            val emailOrPhone = emailOrPhoneEditText.text.toString().trim()
            val newPin = newPinEditText.text.toString().trim()
            val confirmPin = confirmPinEditText.text.toString().trim()

            if (validateInputs(emailOrPhone, newPin, confirmPin)) {
                resetPin(emailOrPhone, newPin)
            }
        }
    }

    private fun validateInputs(emailOrPhone: String, newPin: String, confirmPin: String): Boolean {
        if (TextUtils.isEmpty(emailOrPhone)) {
            emailOrPhoneEditText.error = "Enter email or phone"
            return false
        }

        if (TextUtils.isEmpty(newPin)) {
            newPinEditText.error = "Enter new PIN"
            return false
        }

        if (newPin.length != 6 || !newPin.all { it.isDigit() }) {
            newPinEditText.error = "PIN must be 6 digits"
            return false
        }

        if (newPin != confirmPin) {
            confirmPinEditText.error = "PINs do not match"
            return false
        }

        return true
    }

    private fun resetPin(emailOrPhone: String, newPin: String) {
        lifecycleScope.launch {
            val user = userDao.getUserByEmailOrPhone(emailOrPhone)
            if (user != null) {
                val updatedUser = user.copy(pin = newPin)
                userDao.updateUser(updatedUser)
                Toast.makeText(this@ResetPinActivity, "PIN reset successful", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@ResetPinActivity, "User not found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
