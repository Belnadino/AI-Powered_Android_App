package com.example.healthcare

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.Dao.UserDao
import com.example.healthcare.database.entities.UserEntity
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.actionCodeSettings
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import com.google.firebase.FirebaseException
import android.util.Patterns

class RegisterActivity : BaseActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailOrPhoneEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var backToLoginButton: Button

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private var guestUserId: Int = -1 // store guest user ID if exists

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        nameEditText = findViewById(R.id.nameEditText)
        emailOrPhoneEditText = findViewById(R.id.emailOrPhoneEditText)
        registerButton = findViewById(R.id.registerButton)
        backToLoginButton = findViewById(R.id.backToLoginButton)

        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()
        auth = FirebaseAuth.getInstance()

        // Check if a guest is logged in
        val sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        guestUserId = sharedPrefs.getInt("logged_in_user_id", -1)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val emailOrPhone = emailOrPhoneEditText.text.toString().trim()

            if (validateInputs(name, emailOrPhone)) {
                handleRegistration(name, emailOrPhone)
            }
        }

        backToLoginButton.setOnClickListener { finish() }
    }

    private fun validateInputs(name: String, emailOrPhone: String): Boolean {
        if (TextUtils.isEmpty(name)) {
            nameEditText.error = "Please enter your name"
            return false
        }
        if (TextUtils.isEmpty(emailOrPhone)) {
            emailOrPhoneEditText.error = "Please enter email or phone"
            return false
        }
        if (emailOrPhone.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(emailOrPhone).matches()) {
            emailOrPhoneEditText.error = "Invalid email"
            return false
        }
        if (!emailOrPhone.contains("@") && !Patterns.PHONE.matcher(emailOrPhone).matches()) {
            emailOrPhoneEditText.error = "Invalid phone number"
            return false
        }
        return true
    }

    private fun handleRegistration(name: String, emailOrPhone: String) {
        val isEmail = emailOrPhone.contains("@")

        lifecycleScope.launch {
            // Check if email/phone already exists
            val existing = if (isEmail) userDao.getUserByEmail(emailOrPhone)
            else userDao.getUserByPhone(emailOrPhone)

            if (existing != null) {
                Toast.makeText(this@RegisterActivity, "Email or phone already registered", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Proceed to verification first (email or SMS)
            if (isEmail) sendEmailLinkVerification(name, emailOrPhone)
            else sendSmsVerification(name, emailOrPhone)
        }
    }

    // Saving logged user
    private fun saveLoggedInUser(userId: Int) {
        val sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        sharedPrefs.edit().putInt("logged_in_user_id", userId).apply()
    }


    /** EMAIL LINK VERIFICATION **/
    private fun sendEmailLinkVerification(name: String, email: String) {
        val actionCodeSettings = actionCodeSettings {
            url = "https://healthcare-d92b4.firebaseapp.com/finishSignUp"
            handleCodeInApp = true
            setAndroidPackageName(
                "com.example.healthcare",
                true,
                null
            )
        }

        auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verification email sent. Check your inbox.", Toast.LENGTH_LONG).show()
                    // Save email and guest info for post-verification upgrade
                    getSharedPreferences("emailPrefs", MODE_PRIVATE)
                        .edit()
                        .putString("email", email)
                        .putString("name", name)
                        .putInt("guestUserId", guestUserId)
                        .apply()
                } else {
                    Toast.makeText(this, "Failed to send email link: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    /** SMS VERIFICATION **/
    private fun sendSmsVerification(name: String, phone: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneCredential(name, phone, credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@RegisterActivity, "SMS verification failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@RegisterActivity.verificationId = verificationId
                    resendToken = token
                    val intent = Intent(this@RegisterActivity, VerifyOtpActivity::class.java)
                    intent.putExtra("name", name)
                    intent.putExtra("phone", phone)
                    intent.putExtra("isGuest", guestUserId != -1)
                    intent.putExtra("guestUserId", guestUserId)
                    intent.putExtra("verificationId", verificationId)
                    intent.putExtra("resendToken", token)
                    startActivity(intent)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneCredential(name: String, phone: String, credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                // After verification, create/upgrade account
                upgradeOrCreateUser(name, phone, isEmail = false)
            }
            .addOnFailureListener {
                Toast.makeText(this@RegisterActivity, "OTP verification failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    /** POST-VERIFICATION: CREATE OR UPGRADE USER **/
    fun upgradeOrCreateUser(name: String, idOrPhone: String, isEmail: Boolean) {
        lifecycleScope.launch {
            if (guestUserId != -1) {
                // Upgrade guest user
                val guestUser = userDao.getUserById(guestUserId)
                val updatedUser = guestUser!!.copy(
                    name = name,
                    email = if (isEmail) idOrPhone else "",
                    phoneNumber = if (!isEmail) idOrPhone else "",
                    isGuest = false
                )
                userDao.updateUser(updatedUser)
                saveLoggedInUser(updatedUser.userId)
            } else {
                // Create new user normally
                val newUser = if (isEmail) {
                    UserEntity(
                        name = name,
                        email = idOrPhone,
                        isGuest = false
                    )
                } else {
                    UserEntity(
                        name = name,
                        phoneNumber = idOrPhone,
                        isGuest = false
                    )
                }
                val userId = userDao.insertUser(newUser).toInt()
                saveLoggedInUser(userId)
            }

            startPinSetupActivity(name, idOrPhone, isEmail)
        }
    }

    private fun startPinSetupActivity(name: String, idOrPhone: String, isEmail: Boolean) {
        val intent = Intent(this, PinSetupActivity::class.java)
        intent.putExtra("name", name)
        intent.putExtra("idOrPhone", idOrPhone)
        intent.putExtra("isEmail", isEmail)
        startActivity(intent)
        finish()
    }
}
