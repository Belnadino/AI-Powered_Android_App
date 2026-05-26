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
import com.example.healthcare.database.entities.MealLog

class TodayMealAdapter(
    private val onMarkEatenClick: (MealWithLog) -> Unit
) : ListAdapter<MealWithLog, TodayMealAdapter.MealViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MealWithLog>() {
            override fun areItemsTheSame(oldItem: MealWithLog, newItem: MealWithLog) =
                oldItem.plan.id == newItem.plan.id

            override fun areContentsTheSame(oldItem: MealWithLog, newItem: MealWithLog) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_today_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_meal_name)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_meal_description)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_meal_time)
        private val btnMarkEaten: Button = itemView.findViewById(R.id.btn_mark_eaten)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_eaten_status)

        fun bind(mealWithLog: MealWithLog) {
            val plan = mealWithLog.plan
            val log = mealWithLog.log

            tvName.text = plan.mealName
            tvDescription.text = plan.description
            tvTime.text = plan.mealTime

            updateUI(log)

            btnMarkEaten.setOnClickListener {
                val eatenLog = log?.copy(
                    status = "eaten",
                    timestamp = System.currentTimeMillis()
                ) ?: MealLog(
                    userId = plan.userId,
                    mealPlanId = plan.id,
                    mealName = plan.mealName,
                    description = plan.description,
                    mealTime = plan.mealTime,
                    logDate = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date()),
                    status = "eaten",
                    timestamp = System.currentTimeMillis()
                )

                onMarkEatenClick(MealWithLog(plan, eatenLog))
            }
        }

        private fun updateUI(log: MealLog?) {
            if (log != null) {
                when (log.status.lowercase()) {
                    "eaten" -> {
                        btnMarkEaten.visibility = View.GONE
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = "Eaten"
                    }
                    "missed" -> {
                        btnMarkEaten.visibility = View.GONE
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = "Missed"
                    }
                    else -> {
                        btnMarkEaten.visibility = View.VISIBLE
                        tvStatus.visibility = View.GONE
                    }
                }
            } else {
                btnMarkEaten.visibility = View.VISIBLE
                tvStatus.visibility = View.GONE
            }
        }
    }
}
