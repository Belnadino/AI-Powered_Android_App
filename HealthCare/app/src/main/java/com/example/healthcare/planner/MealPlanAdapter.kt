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
import com.example.healthcare.database.entities.MealPlan

class MealPlanAdapter(
    private val onEditClick: (MealPlan) -> Unit,
    private val onDeleteClick: (MealPlan) -> Unit
) : ListAdapter<MealPlan, MealPlanAdapter.PlanViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MealPlan>() {
            override fun areItemsTheSame(oldItem: MealPlan, newItem: MealPlan) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: MealPlan, newItem: MealPlan) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_plan, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_meal_name)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_meal_description)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_meal_time)
        private val tvDateRange: TextView = itemView.findViewById(R.id.tv_meal_date_range)
        private val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        private val btnDelete: Button = itemView.findViewById(R.id.btn_delete)

        fun bind(plan: MealPlan) {
            tvName.text = plan.mealName
            tvDescription.text = plan.description.ifBlank { "No description" }
            tvTime.text = plan.mealTime
            tvDateRange.text = "${plan.startDate} → ${plan.endDate}"
            btnEdit.setOnClickListener { onEditClick(plan) }
            btnDelete.setOnClickListener { onDeleteClick(plan) }
        }
    }
}

