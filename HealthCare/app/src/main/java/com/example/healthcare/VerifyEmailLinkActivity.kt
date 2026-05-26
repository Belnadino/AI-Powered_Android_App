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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class VerifyEmailLinkActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    private lateinit var pinEditText: EditText
    private lateinit var confirmButton: Button

    private var email: String? = null
    private var emailLink: String? = null
    private var userName: String? = null // optional name passed from registration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_email_link)

        pinEditText = findViewById(R.id.pinEditText)
        confirmButton = findViewById(R.id.confirmButton)

        auth = FirebaseAuth.getInstance()
        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        emailLink = intent.data?.toString()
        email = intent.getStringExtra("email")
        userName = intent.getStringExtra("name")

        // Fallback: retrieve email from shared prefs if app was cold-started
        if (email == null) {
            email = getSharedPreferences("emailPrefs", MODE_PRIVATE).getString("email", null)
        }

        if (emailLink == null || email == null) {
            Toast.makeText(this, "Invalid link", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (auth.isSignInWithEmailLink(emailLink!!)) {
            confirmButton.setOnClickListener {
                val pin = pinEditText.text.toString().trim()
                if (pin.isEmpty()) {
                    pinEditText.error = "Enter PIN"
                    return@setOnClickListener
                }
                signInWithEmailLink(email!!, emailLink!!, pin)
            }
        }
    }

    private fun signInWithEmailLink(email: String, link: String, pin: String) {
        auth.signInWithEmailLink(email, link)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        lifecycleScope.launch {
                            // Use name from intent first, fallback to displayName
                            val finalName = userName ?: firebaseUser.displayName ?: ""

                            val user = UserEntity(
                                uid = email,
                                name = finalName,
                                pin = pin,
                                isGuest = false
                            )
                            userDao.registerUser(user)

                            // Clear stored email
                            getSharedPreferences("emailPrefs", MODE_PRIVATE)
                                .edit()
                                .remove("email")
                                .apply()

                            Toast.makeText(
                                this@VerifyEmailLinkActivity,
                                "Email verified and PIN set!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Navigate to main app
                            val intent = Intent(this@VerifyEmailLinkActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@VerifyEmailLinkActivity,
                        "Verification failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
