package com.example.healthcare.planner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.R
import java.util.*
import com.example.healthcare.database.entities.*
import com.example.healthcare.planner.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.button.MaterialButton

class CaregiverAdapter(
    private val onEditClick: (CaregiverEntity) -> Unit,
    private val onDeleteClick: (CaregiverEntity) -> Unit
) : ListAdapter<CaregiverEntity, CaregiverAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val email: TextView = view.findViewById(R.id.tvEmail)
        val frequency: TextView = view.findViewById(R.id.tvFrequency)
        val edit: MaterialButton = view.findViewById(R.id.btnEdit)
        val delete: MaterialButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_caregiver, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val caregiver = getItem(position)

        holder.name.text = caregiver.name
        holder.email.text = caregiver.email
        holder.frequency.text = "Reports: ${formatFrequency(caregiver.reportFrequency)}"

        holder.edit.setOnClickListener { onEditClick(caregiver) }
        holder.delete.setOnClickListener { onDeleteClick(caregiver) }
    }

    private fun formatFrequency(value: Int): String = when (value) {
        1 -> "Daily"
        3 -> "Every 3 days"
        7 -> "Weekly"
        else -> "Not sent"
    }

    class DiffCallback : DiffUtil.ItemCallback<CaregiverEntity>() {
        override fun areItemsTheSame(a: CaregiverEntity, b: CaregiverEntity) = a.id == b.id
        override fun areContentsTheSame(a: CaregiverEntity, b: CaregiverEntity) = a == b
    }
}
