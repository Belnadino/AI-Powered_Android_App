package com.example.healthcare.planner

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.healthcare.database.entities.Feedback
import com.example.healthcare.R

class AddFeedbackDialog(private val userId: Int) : DialogFragment() {
    private lateinit var viewModel: FeedbackViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = ViewModelProvider(requireActivity()).get(FeedbackViewModel::class.java)
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_feedback, null)

        val etSubject: EditText = view.findViewById(R.id.etSubject)
        val etDescription: EditText = view.findViewById(R.id.etDescription)
        val rgCategory: RadioGroup = view.findViewById(R.id.rgCategory)
        val rbRating: RatingBar = view.findViewById(R.id.rbRating)
        val btnSubmit: Button = view.findViewById(R.id.btnSubmitFeedback)

        rbRating.rating = 0f
        rbRating.isEnabled = false

        rgCategory.setOnCheckedChangeListener { _, checkedId ->
            rbRating.isEnabled = checkedId == R.id.rbComplement
        }

        btnSubmit.setOnClickListener {
            val subject = etSubject.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val category = if (rgCategory.checkedRadioButtonId == R.id.rbProblem) "Problem" else "Complement"
            val rating = if (category == "Complement") rbRating.rating.toInt() else null
            val status = if (category == "Complement") "Closed" else "Open"

            if (subject.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val feedback = Feedback(
                userId = userId,
                name = "User",
                subject = subject,
                description = description,
                category = category,
                rating = rating,
                status = status
            )
            viewModel.addFeedback(feedback)
            Toast.makeText(requireContext(), "Feedback submitted", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        return AlertDialog.Builder(requireContext()).setView(view).create()
    }
}
