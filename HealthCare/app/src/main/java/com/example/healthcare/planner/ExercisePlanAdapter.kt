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
import com.example.healthcare.database.entities.ExercisePlan

class ExercisePlanAdapter(
    private val onEditClick: (ExercisePlan) -> Unit,
    private val onDeleteClick: (ExercisePlan) -> Unit
) : ListAdapter<ExercisePlan, ExercisePlanAdapter.PlanViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ExercisePlan>() {
            override fun areItemsTheSame(
                oldItem: ExercisePlan,
                newItem: ExercisePlan
            ) = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: ExercisePlan,
                newItem: ExercisePlan
            ) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_plan, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tv_exercise_name)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_exercise_time)
        private val tvDateRange: TextView =
            itemView.findViewById(R.id.tv_exercise_date_range)

        private val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        private val btnDelete: Button = itemView.findViewById(R.id.btn_delete)

        fun bind(plan: ExercisePlan) {
            tvName.text = plan.exerciseName
            tvTime.text = plan.plannedTime
            tvDateRange.text = "${plan.startDate} → ${plan.endDate}"

            btnEdit.setOnClickListener { onEditClick(plan) }
            btnDelete.setOnClickListener { onDeleteClick(plan) }
        }
    }
}
