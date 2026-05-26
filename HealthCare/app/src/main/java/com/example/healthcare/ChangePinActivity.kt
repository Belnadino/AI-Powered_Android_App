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

class ChangePinActivity : BaseActivity() {

    private lateinit var oldPinEditText: EditText
    private lateinit var newPinEditText: EditText
    private lateinit var confirmPinEditText: EditText
    private lateinit var changeButton: Button
    private lateinit var btnCancel: Button

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pin)

        // Initialize views
        oldPinEditText = findViewById(R.id.oldPin)
        newPinEditText = findViewById(R.id.newPin)
        confirmPinEditText = findViewById(R.id.confirmPin)
        changeButton = findViewById(R.id.changeButton)
        btnCancel = findViewById(R.id.btnCancel)

        // Initialize database
        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Get logged-in user ID from SharedPreferences
        val sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPrefs.getInt("logged_in_user_id", -1)

        btnCancel.setOnClickListener { finish() }

        changeButton.setOnClickListener {
            val oldPin = oldPinEditText.text.toString().trim()
            val newPin = newPinEditText.text.toString().trim()
            val confirmPin = confirmPinEditText.text.toString().trim()

            if (validateInputs(oldPin, newPin, confirmPin)) {
                lifecycleScope.launch {
                    val user = userDao.getUserById(userId)
                    if (user != null) {
                        if (user.pin != oldPin) {
                            oldPinEditText.error = "Old PIN is incorrect"
                        } else {
                            val updatedUser = user.copy(pin = newPin)
                            userDao.updateUser(updatedUser)
                            Toast.makeText(
                                this@ChangePinActivity,
                                "PIN changed successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    } else {
                        Toast.makeText(this@ChangePinActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun validateInputs(oldPin: String, newPin: String, confirmPin: String): Boolean {
        if (TextUtils.isEmpty(oldPin)) {
            oldPinEditText.error = "Please enter old PIN"
            return false
        }
        if (oldPin.length != 6) {
            oldPinEditText.error = "PIN must be 6 digits"
            return false
        }
        if (TextUtils.isEmpty(newPin)) {
            newPinEditText.error = "Please enter new PIN"
            return false
        }
        if (newPin.length != 6) {
            newPinEditText.error = "PIN must be 6 digits"
            return false
        }
        if (newPin != confirmPin) {
            confirmPinEditText.error = "PINs do not match"
            return false
        }
        return true
    }
}
