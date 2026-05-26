package com.example.healthcare

import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar

class SettingActivity : BaseActivity() {

    private lateinit var fontSizeGroup: RadioGroup
    private lateinit var soundInputGroup: RadioGroup
    private lateinit var switchSound: SwitchCompat
    private lateinit var switchVibration: SwitchCompat
    private lateinit var btnSave: Button

    private lateinit var textViewsForFont: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // -----------------------
        // Toolbar
        // -----------------------
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // -----------------------
        // Views
        // -----------------------
        fontSizeGroup = findViewById(R.id.font_size_group)
        soundInputGroup = findViewById(R.id.sound_input_group)
        switchSound = findViewById(R.id.switch_sound)
        switchVibration = findViewById(R.id.switch_vibration)
        btnSave = findViewById(R.id.btn_save_settings)

        textViewsForFont = listOf(
            findViewById(R.id.font_label),
            findViewById(R.id.sound_input_label),
            findViewById(R.id.sound_label),
            findViewById(R.id.vibration_label)
        )

        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        // -----------------------
        // Load saved preferences
        // -----------------------
        // Font size
        when (prefs.getString("font_size", "Medium")) {
            "Small" -> fontSizeGroup.check(R.id.font_small)
            "Medium" -> fontSizeGroup.check(R.id.font_medium)
            "Large" -> fontSizeGroup.check(R.id.font_large)
        }
        fontSizeGroup.tag = prefs.getString("font_size", "Medium")
        updateFontSize(fontSizeGroup.tag as String)

        // Sound input
        soundInputGroup.check(
            if (prefs.getString("sound_input", "No") == "Yes") R.id.sound_input_yes else R.id.sound_input_no
        )
        soundInputGroup.tag = prefs.getString("sound_input", "No")

        // Switches
        switchSound.isChecked = prefs.getBoolean("sound_alert", true)
        switchSound.tag = switchSound.isChecked

        switchVibration.isChecked = prefs.getBoolean("vibration", true)
        switchVibration.tag = switchVibration.isChecked

        // -----------------------
        // Listeners
        // -----------------------
        fontSizeGroup.setOnCheckedChangeListener { _, checkedId ->
            val size = when (checkedId) {
                R.id.font_small -> "Small"
                R.id.font_medium -> "Medium"
                R.id.font_large -> "Large"
                else -> "Medium"
            }
            fontSizeGroup.tag = size
            updateFontSize(size)
        }

        soundInputGroup.setOnCheckedChangeListener { _, checkedId ->
            soundInputGroup.tag = if (checkedId == R.id.sound_input_yes) "Yes" else "No"
            // No need to start speech recognizer here — handled by BaseActivity
        }

        switchSound.setOnCheckedChangeListener { _, isChecked ->
            switchSound.tag = isChecked
        }

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            switchVibration.tag = isChecked
        }

        // -----------------------
        // Save button
        // -----------------------
        btnSave.setOnClickListener {
            val selectedFont = fontSizeGroup.tag as? String ?: "Medium"
            val scale = when (selectedFont) {
                "Small" -> 0.9f
                "Medium" -> 1.0f
                "Large" -> 1.2f
                else -> 1.0f
            }

            with(prefs.edit()) {
                putString("font_size", selectedFont)
                putFloat("font_scale", scale)
                putString("sound_input", soundInputGroup.tag as? String ?: "No")
                putBoolean("sound_alert", switchSound.tag as? Boolean ?: true)
                putBoolean("vibration", switchVibration.tag as? Boolean ?: true)
                apply()
            }
            setResult(RESULT_OK)
            finish()

            Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show()
        }
    }

    // -----------------------
    // Font size update
    // -----------------------
    private fun updateFontSize(size: String) {
        val textSize = when (size) {
            "Small" -> 16f
            "Medium" -> 20f
            "Large" -> 24f
            else -> 20f
        }
        textViewsForFont.forEach { it.textSize = textSize }
    }
}
