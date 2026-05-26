package com.example.healthcare.planner

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.R
import com.example.healthcare.database.entities.ExerciseLog

// Helper data class for grouping by date
data class ExerciseDay(
    val date: String,
    val logs: List<ExerciseLog>,
    var expanded: Boolean = true
)

class ExerciseReportAdapter(
    private val days: List<ExerciseDay>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DATE = 0
        private const val TYPE_LOG = 1
    }

    private val displayList = mutableListOf<Any>()

    init {
        rebuildDisplayList()
    }

    private fun rebuildDisplayList() {
        displayList.clear()
        days.forEach { day ->
            displayList.add(day)
            if (day.expanded) displayList.addAll(day.logs)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayList[position] is ExerciseDay) TYPE_DATE else TYPE_LOG
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_DATE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_exercise_day_header, parent, false)
            DateViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_exercise_log, parent, false)
            LogViewHolder(view)
        }
    }

    override fun getItemCount(): Int = displayList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = displayList[position]
        if (holder is DateViewHolder && item is ExerciseDay) {
            holder.tvDate.text = item.date
            holder.root.setOnClickListener {
                item.expanded = !item.expanded
                rebuildDisplayList()
                notifyDataSetChanged()
            }
        } else if (holder is LogViewHolder && item is ExerciseLog) {
            holder.tvExercise.text = item.exerciseName
            holder.tvPlannedTime.text = "Planned: ${item.plannedTime ?: "-"}"
            holder.tvTimeUsed.text = "Time Used: ${item.timeUsed}"
            holder.tvStatus.text = item.status
            holder.tvStatus.setTextColor(
                if (item.status == "Completed") Color.parseColor("#4CAF50")
                else Color.parseColor("#F44336")
            )
        }
    }

    inner class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: LinearLayout = view.findViewById(R.id.root)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    inner class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvExercise: TextView = view.findViewById(R.id.tvExercise)
        val tvPlannedTime: TextView = view.findViewById(R.id.tvPlannedTime)
        val tvTimeUsed: TextView = view.findViewById(R.id.tvTimeUsed)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }
}
