package com.example.healthcare

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.Dao.UserDao
import com.example.healthcare.database.entities.UserEntity
import com.google.firebase.auth.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import com.google.firebase.FirebaseException

class VerifyOtpActivity : BaseActivity() {

    private lateinit var otpEditText: EditText
    private lateinit var verifyButton: Button
    private lateinit var resendButton: Button
    private lateinit var cancelButton: Button
    private lateinit var resendTimerText: TextView

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private var userName: String? = null
    private var phoneNumber: String? = null

    private val RESEND_INTERVAL_MS = 60000L // 60 seconds cooldown

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otp)

        otpEditText = findViewById(R.id.otpEditText)
        verifyButton = findViewById(R.id.verifyButton)
        resendButton = findViewById(R.id.resendButton)
        cancelButton = findViewById(R.id.cancelButton)
        resendTimerText = findViewById(R.id.resendTimerText)

        db = AppDatabase.getDatabase(this)
        userDao = db.userDao()
        auth = FirebaseAuth.getInstance()

        verificationId = intent.getStringExtra("verificationId")
        userName = intent.getStringExtra("name")
        phoneNumber = intent.getStringExtra("phone")
        resendToken = intent.getParcelableExtra("resendToken")

        startResendCooldown()

        verifyButton.setOnClickListener {
            val otp = otpEditText.text.toString().trim()
            if (otp.isEmpty()) {
                otpEditText.error = "Enter OTP"
                return@setOnClickListener
            }
            verificationId?.let { id ->
                val credential = PhoneAuthProvider.getCredential(id, otp)
                verifyOtpAndRegisterUser(credential)
            }
        }

        resendButton.setOnClickListener {
            resendToken?.let { token ->
                resendOtp(token)
                startResendCooldown()
            }
        }

        cancelButton.setOnClickListener {
            finish() // Go back to RegisterActivity
        }
    }

    private fun startResendCooldown() {
        resendButton.isEnabled = false
        object : CountDownTimer(RESEND_INTERVAL_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resendTimerText.text = "Resend OTP in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                resendTimerText.text = ""
                resendButton.isEnabled = true
            }
        }.start()
    }

    private fun resendOtp(token: PhoneAuthProvider.ForceResendingToken) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber!!)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    verifyOtpAndRegisterUser(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@VerifyOtpActivity, "Resend failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    resendToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@VerifyOtpActivity.verificationId = verificationId
                    this@VerifyOtpActivity.resendToken = resendToken
                    Toast.makeText(this@VerifyOtpActivity, "OTP resent", Toast.LENGTH_SHORT).show()
                }
            })
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyOtpAndRegisterUser(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                Toast.makeText(this, "Phone verified", Toast.LENGTH_SHORT).show()

                lifecycleScope.launch {
                    // Create user entity
                    val user = UserEntity(
                        uid = phoneNumber!!,
                        name = userName!!,
                        pin = "", // will set in PinSetupActivity
                        isGuest = false
                    )

                    // Insert user and get generated local userId
                    val generatedId = userDao.registerUser(user) // Returns Long

                    // Save logged-in user session
                    saveLoggedInUser(generatedId.toInt())

                    // Pass numeric userId to PinSetupActivity
                    val intent = Intent(this@VerifyOtpActivity, PinSetupActivity::class.java)
                    intent.putExtra("userId", generatedId.toInt())
                    intent.putExtra("name", userName)
                    intent.putExtra("isEmail", false)
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "OTP verification failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    /** SAVE LOGGED IN USER ID **/
    private fun saveLoggedInUser(userId: Int) {
        val sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        sharedPrefs.edit().putInt("logged_in_user_id", userId).apply()
    }
}
