package com.example.healthcare

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.healthcare.planner.*
import java.util.Locale


open class BaseActivity : AppCompatActivity() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var soundInputEnabled = false
    private val REQUEST_RECORD_AUDIO = 101
    private val handler = Handler(Looper.getMainLooper())

    private var tts: TextToSpeech? = null
    private var isSpeaking = false

    // ---------------- Font scaling ----------------
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", MODE_PRIVATE)
        val scale = prefs.getFloat("font_scale", 1.0f)
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = scale
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    // ---------------- OnCreate ----------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        soundInputEnabled = prefs.getString("sound_input", "No") == "Yes"

        // Initialize TTS
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts?.language = Locale.getDefault()
                tts?.setSpeechRate(1.0f)
            }
        }

        if (soundInputEnabled) {
            requestAudioPermissionAndStart()
        }
    }

    // ---------------- Permissions ----------------
    private fun requestAudioPermissionAndStart() {
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
        } else {
            startListening()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- Voice recognition ----------------
    private fun startListening() {
        if (isListening) return

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
                    val command = it[0].lowercase(Locale.getDefault())
                    handleCommand(command)
                }
                safeRestartListening()
            }

            override fun onError(error: Int) { safeRestartListening() }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
        isListening = true
    }

    /** Safe restart only if voice input enabled and TTS not speaking */
    private fun safeRestartListening() {
        if (!soundInputEnabled || isSpeaking) return
        handler.postDelayed({
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                })
            } catch (e: Exception) { e.printStackTrace() }
        }, 500)
    }

    private fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) { e.printStackTrace() }
        speechRecognizer = null
        isListening = false
    }

    // ---------------- TTS helper ----------------
    private fun speak(message: String) {
        runOnUiThread {
            isSpeaking = true
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "tts1")
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    isSpeaking = false
                    safeRestartListening()
                }
                override fun onError(utteranceId: String?) {
                    isSpeaking = false
                    safeRestartListening()
                }
            })
        }
    }

    // ---------------- Command handler ----------------
    private fun handleCommand(command: String) {
        val text = command.lowercase(Locale.getDefault()).trim()

        // Back command
        if ("back" in text) {
            runOnUiThread {
                speak("Going back")
                try {
                    onBackPressedDispatcher.onBackPressed()
                } catch (e: Exception) { finish() }
            }
            return
        }

        // Map keywords to activity classes
        val keywordActivityMap: Map<String, Class<*>> = mapOf(
            "home" to MainActivity::class.java,
            "exercise" to ExerciseActivity::class.java,
            "workout" to ExerciseActivity::class.java,
            "training" to ExerciseActivity::class.java,
            "medication" to MedicationActivity::class.java,
            "medicine" to MedicationActivity::class.java,
            "med" to MedicationActivity::class.java,
            "meal" to MealActivity::class.java,
            "food" to MealActivity::class.java,
            "feedback" to FeedbackActivity::class.java,
            "review" to FeedbackActivity::class.java,
            "notification" to NotificationActivity::class.java,
            "alerts" to NotificationActivity::class.java,
            "setting" to SettingActivity::class.java,
            "settings" to SettingActivity::class.java,
            "profile" to ProfileActivity::class.java,
            "chat" to ChatActivity::class.java,
            "assistance" to ChatActivity::class.java,
            "report" to ReportActivity::class.java,
            "reports" to ReportActivity::class.java,
            "caregiver" to CaregiverActivity::class.java,
            "carer" to CaregiverActivity::class.java,
            "register" to RegisterActivity::class.java,
            "signup" to RegisterActivity::class.java,
            "edit profile" to EditProfileActivity::class.java,
            "verify otp" to VerifyOtpActivity::class.java,
            "otp" to VerifyOtpActivity::class.java,
            "pin setup" to PinSetupActivity::class.java,
            "add medication plan" to AddMedicationActivity::class.java,
            "add exercise plan" to AddExercisePlanActivity::class.java,
            "exercise timer" to ExerciseTimerActivity::class.java,
            "meal plans" to MealPlansListActivity::class.java,
            "meal plan list" to MealPlansListActivity::class.java,
            "add edit meal plan" to AddEditMealPlanActivity::class.java,
            "meal report" to MealReportActivity::class.java,
            "medication report" to MedicationReportActivity::class.java,
            "exercise report" to ExerciseReportActivity::class.java,
            "feedback details" to FeedbackDetailsActivity::class.java,
            "reset pin" to ResetPinActivity::class.java,
            "change pin" to ChangePinActivity::class.java,
            "medication plans list" to MedicationPlansListActivity::class.java,
            "exercise plan list" to ExercisePlanListActivity::class.java,
            "add edit caregiver" to AddEditCaregiverActivity::class.java,
            "verify email" to VerifyEmailLinkActivity::class.java,
            "login" to LoginActivity::class.java
        )

        for ((keyword, activity) in keywordActivityMap) {
            if (keyword in text) {
                runOnUiThread {
                    if (this::class.java != activity) {
                        speak("Opening ${activity.simpleName.replace("Activity", "")}")
                        val intent = Intent(this@BaseActivity, activity)
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(intent)
                    }
                }
                return
            }
        }

        runOnUiThread {
            speak("Command not recognized")
            Toast.makeText(this@BaseActivity, "Command not recognized: $command", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- Cleanup ----------------
    override fun onDestroy() {
        super.onDestroy()
        stopListening()
        tts?.stop()
        tts?.shutdown()
    }
}
