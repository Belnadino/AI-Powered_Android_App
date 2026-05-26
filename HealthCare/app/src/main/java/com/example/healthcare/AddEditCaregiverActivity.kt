package com.example.healthcare

import android.os.Bundle
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.entities.CaregiverEntity
import com.example.healthcare.planner.CaregiverViewModel
import com.example.healthcare.planner.CaregiverViewModelFactory
import kotlinx.coroutines.launch

class AddEditCaregiverActivity : BaseActivity() {

    private lateinit var viewModel: CaregiverViewModel
    private var caregiverId = 0

    // Views
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var rbDaily: RadioButton
    private lateinit var rbThree: RadioButton
    private lateinit var rbWeekly: RadioButton
    private lateinit var rbNone: RadioButton
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_caregiver)

        initViews()
        setupViewModel()
        loadCaregiverIfEditing()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        btnSave.setOnClickListener {
            saveCaregiver()
        }
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        radioGroup = findViewById(R.id.radioGroup)
        rbDaily = findViewById(R.id.rbDaily)
        rbThree = findViewById(R.id.rbThree)
        rbWeekly = findViewById(R.id.rbWeekly)
        rbNone = findViewById(R.id.rbNone)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setupViewModel() {
        val dao = AppDatabase.getDatabase(this).caregiverDao()
        val factory = CaregiverViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[CaregiverViewModel::class.java]
    }

    private fun loadCaregiverIfEditing() {
        caregiverId = intent.getIntExtra("caregiver_id", 0)

        if (caregiverId != 0) {
            lifecycleScope.launch {
                val caregiver = viewModel.getCaregiver(caregiverId)
                caregiver?.let { fillForm(it) }
            }
        }
    }

    private fun fillForm(c: CaregiverEntity) {
        etName.setText(c.name)
        etEmail.setText(c.email)

        when (c.reportFrequency) {
            1 -> rbDaily.isChecked = true
            3 -> rbThree.isChecked = true
            7 -> rbWeekly.isChecked = true
            else -> rbNone.isChecked = true
        }
    }

    private fun saveCaregiver() {
        val caregiver = CaregiverEntity(
            id = caregiverId,
            name = etName.text.toString().trim(),
            email = etEmail.text.toString().trim(),
            reportFrequency = when (radioGroup.checkedRadioButtonId) {
                R.id.rbDaily -> 1
                R.id.rbThree -> 3
                R.id.rbWeekly -> 7
                else -> 0
            }
        )

        viewModel.saveCaregiver(caregiver)
        finish()
    }
}
