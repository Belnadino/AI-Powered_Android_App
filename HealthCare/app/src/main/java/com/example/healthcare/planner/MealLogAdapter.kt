package com.example.healthcare.planner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.R
import com.example.healthcare.database.entities.MealLog

class MealLogAdapter : ListAdapter<MealLog, MealLogAdapter.LogViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MealLog>() {
            override fun areItemsTheSame(oldItem: MealLog, newItem: MealLog) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: MealLog, newItem: MealLog) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_meal_name)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_meal_description)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_meal_time)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_meal_status)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_meal_date)

        fun bind(log: MealLog) {
            tvName.text = log.mealName
            tvDescription.text = log.description
            tvTime.text = log.mealTime
            tvDate.text = log.logDate
            tvStatus.text = log.status.replaceFirstChar { it.uppercase() }

            // Color coding with hardcoded ARGB values
            when (log.status) {
                "missed" -> tvStatus.setTextColor(0xFFFF0000.toInt()) // red
                "eaten" -> tvStatus.setTextColor(0xFF00AA00.toInt()) // green
                else -> tvStatus.setTextColor(0xFF000000.toInt()) // black for other statuses
            }
        }
    }

    /** Optional: update logs list explicitly */
    fun updateLogs(logs: List<MealLog>) {
        submitList(logs)
    }
}
