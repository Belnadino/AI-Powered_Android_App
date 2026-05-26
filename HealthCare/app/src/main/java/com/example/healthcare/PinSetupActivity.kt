package com.example.healthcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.Dao.UserDao
import com.example.healthcare.database.entities.UserEntity
import kotlinx.coroutines.launch

class PinSetupActivity : BaseActivity() {

    private lateinit var pinEditText: EditText
    private lateinit var confirmPinEditText: EditText
    private lateinit var savePinButton: Button

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    private var userId: Int = -1 // Existing user's local Room ID
    private var name: String = ""
    private var isEmail: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_setup)

        pinEditText = findViewById(R.id.pinEditText)
        confirmPinEditText = findViewById(R.id.confirmPinEditText)
        savePinButton = findViewById(R.id.savePinButton)

        // Get extras from previous activity
        userId = intent.getIntExtra("userId", -1)
        name = intent.getStringExtra("name") ?: ""
        isEmail = intent.getBooleanExtra("isEmail", true)

        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        savePinButton.setOnClickListener {
            val pin = pinEditText.text.toString().trim()
            val confirmPin = confirmPinEditText.text.toString().trim()

            if (validatePin(pin, confirmPin)) {
                saveUserPin(pin)
            }
        }
    }

    private fun validatePin(pin: String, confirmPin: String): Boolean {
        if (pin.length != 6) {
            pinEditText.error = "PIN must be 6 digits"
            return false
        }
        if (pin != confirmPin) {
            confirmPinEditText.error = "PIN does not match"
            return false
        }
        return true
    }

    private fun saveUserPin(pin: String) {
        if (userId == -1) {
            Toast.makeText(this, "Invalid user. Cannot set PIN", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            // Fetch user by numeric ID
            val user: UserEntity? = userDao.getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(pin = pin)
                userDao.updateUser(updatedUser)

                // Save logged-in user ID
                val sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                sharedPrefs.edit().putInt("logged_in_user_id", updatedUser.userId).apply()

                Toast.makeText(this@PinSetupActivity, "PIN setup complete", Toast.LENGTH_SHORT).show()

                // Go to MainActivity
                startActivity(Intent(this@PinSetupActivity, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this@PinSetupActivity, "User not found", Toast.LENGTH_LONG).show()
            }
        }
    }
}
