package com.example.healthcare.planner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.R
import com.example.healthcare.database.entities.MedicinePlan

class MedicationPlansAdapter(
    private val onEditClick: (MedicinePlan) -> Unit,
    private val onDeleteClick: (MedicinePlan) -> Unit
) : ListAdapter<MedicinePlan, MedicationPlansAdapter.PlanViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MedicinePlan>() {
            override fun areItemsTheSame(oldItem: MedicinePlan, newItem: MedicinePlan) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MedicinePlan, newItem: MedicinePlan) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medication_plan_buttons, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvMedicineName)
        private val tvTime: TextView = itemView.findViewById(R.id.tvMedicineTimes)
        private val tvDateRange: TextView = itemView.findViewById(R.id.tvMedicineDateRange)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(plan: MedicinePlan) {
            tvName.text = plan.medicineName
            tvTime.text = plan.doseTime ?: "No time specified"
            tvDateRange.text = "${plan.startDate} → ${plan.endDate}"

            btnEdit.setOnClickListener { onEditClick(plan) }
            btnDelete.setOnClickListener { onDeleteClick(plan) }
        }
    }
}
