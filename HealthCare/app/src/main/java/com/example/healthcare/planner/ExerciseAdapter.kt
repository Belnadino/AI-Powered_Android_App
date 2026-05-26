package com.example.healthcare.planner

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.R
import com.example.healthcare.database.entities.ExerciseLog
import com.example.healthcare.database.entities.ExercisePlan
import java.text.SimpleDateFormat
import java.util.*

class ExerciseAdapter(
    private val plans: List<ExercisePlan>,
    private val logs: List<ExerciseLog>,
    private val onStart: (ExercisePlan) -> Unit,
    private val onViewCompleted: () -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvExerciseName)
        val tvTime: TextView = view.findViewById(R.id.tvPlannedTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvTimeUsed: TextView = view.findViewById(R.id.tvTimeUsed)
        val btnStart: Button = view.findViewById(R.id.btnStart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = plans.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val plan = plans[position]
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val now = System.currentTimeMillis()

        holder.tvName.text = plan.exerciseName
        holder.tvTime.text = plan.plannedTime ?: "--"

        val todayLog = logs.find { it.planId == plan.id }

        if (todayLog != null) {
            // Completed / Missed
            holder.tvStatus.text = todayLog.status
            holder.tvTimeUsed.text = "Time Used: ${todayLog.timeUsed}"
            holder.btnStart.isEnabled = false
            holder.tvStatus.setTextColor(
                if (todayLog.status == "Completed") Color.parseColor("#4CAF50") // green
                else Color.parseColor("#F44336") // red for missed
            )
        } else {
            // Pending
            holder.tvStatus.text = "Pending"
            holder.tvTimeUsed.text = "Time Used: --"
            holder.btnStart.isEnabled = true

            // Highlight overdue exercises in red
            plan.plannedTime?.let {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val plannedDateTime = sdf.parse("$today ${plan.plannedTime}")
                    if (plannedDateTime != null && plannedDateTime.time < now) {
                        holder.tvStatus.text = "Overdue"
                        holder.tvStatus.setTextColor(Color.RED)
                    } else {
                        holder.tvStatus.setTextColor(Color.BLACK)
                    }
                } catch (e: Exception) {
                    holder.tvStatus.setTextColor(Color.BLACK)
                }
            }
        }

        holder.btnStart.setOnClickListener {
            onStart(plan)
        }

        // Optional: click on the item to view all completed exercises
        holder.itemView.setOnLongClickListener {
            onViewCompleted()
            true
        }
    }
}
