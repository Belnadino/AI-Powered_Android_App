package com.example.healthcare

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.Dao.UserDao
import com.example.healthcare.database.entities.UserEntity
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {

    private lateinit var emailOrPhoneEditText: EditText
    private lateinit var pinEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var guestButton: Button
    private lateinit var forgotPinText: TextView

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Views
        emailOrPhoneEditText = findViewById(R.id.emailOrPhone)
        pinEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        guestButton = findViewById(R.id.guestButton)
        forgotPinText = findViewById(R.id.forgotPassword)

        // Room DB
        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        // PIN login
        loginButton.setOnClickListener {
            val emailOrPhone = emailOrPhoneEditText.text.toString().trim()
            val pin = pinEditText.text.toString().trim()

            if (validatePinLogin(emailOrPhone, pin)) {
                loginWithPin(emailOrPhone, pin)
            }
        }

        // Register new user
        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Guest login
        guestButton.setOnClickListener {
            createGuestUser()
        }

        // Forgot PIN
        forgotPinText.setOnClickListener {
            startActivity(Intent(this, ResetPinActivity::class.java))
        }
    }

    /** VALIDATE PIN LOGIN INPUTS **/
    private fun validatePinLogin(emailOrPhone: String, pin: String): Boolean {
        if (TextUtils.isEmpty(emailOrPhone)) {
            emailOrPhoneEditText.error = "Please enter email or phone"
            return false
        }
        if (TextUtils.isEmpty(pin)) {
            pinEditText.error = "Please enter PIN"
            return false
        }
        if (pin.length < 4) {
            pinEditText.error = "PIN must be at least 4 digits"
            return false
        }
        return true
    }

    /** LOGIN WITH PIN (Offline Friendly) **/
    private fun loginWithPin(emailOrPhone: String, pin: String) {
        lifecycleScope.launch {
            val user = userDao.loginWithPin(emailOrPhone, pin) // use query with PIN
            if (user != null) {
                saveLoggedInUser(user.userId)
                Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this@LoginActivity, "Invalid email/phone or PIN", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** SAVE LOGGED IN USER ID **/
    private fun saveLoggedInUser(userId: Int) {
        val sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        sharedPrefs.edit().putInt("logged_in_user_id", userId).apply()
    }

    /** GUEST LOGIN **/
    private fun createGuestUser() {
        lifecycleScope.launch {
            // 1Check if a guest account already exists
            val existingGuest = userDao.getGuestUser()

            val guestUserId = if (existingGuest != null) {
                // Reuse existing guest
                existingGuest.userId
            } else {
                // Create a new guest account
                val guestUser = UserEntity(
                    uid = "guest_${System.currentTimeMillis()}",
                    name = "Guest User",
                    isGuest = true,
                    pin = "" // no PIN needed
                )
                userDao.registerUser(guestUser).toInt()
            }

            // 2Save logged-in user ID
            saveLoggedInUser(guestUserId)

            //  Notify user and continue to MainActivity
            Toast.makeText(this@LoginActivity, "Logged in as Guest", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }
}
