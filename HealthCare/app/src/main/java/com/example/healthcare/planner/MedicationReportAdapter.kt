package com.example.healthcare.planner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.database.entities.MedicineLog
import com.example.healthcare.R

private const val TYPE_DATE_HEADER = 0
private const val TYPE_LOG_ITEM = 1

class MedicationReportAdapter(logs: List<MedicineLog>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: List<ReportItem> = groupLogsByDate(logs)

    companion object {
        fun groupLogsByDate(logs: List<MedicineLog>): List<ReportItem> {
            // Sort descending by date, then ascending by scheduled time
            val sortedLogs = logs.sortedWith(
                compareByDescending<MedicineLog> { it.date }.thenBy { it.scheduledTime }
            )
            val grouped = sortedLogs.groupBy { it.date }
            val result = mutableListOf<ReportItem>()
            grouped.forEach { (date, logsForDate) ->
                result.add(ReportItem.DateHeader(date))
                logsForDate.forEach { log ->
                    result.add(ReportItem.MedicineEntry(log))
                }
            }
            return result
        }
    }

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDateHeader)
    }

    inner class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvMedicineName)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ReportItem.DateHeader -> TYPE_DATE_HEADER
            is ReportItem.MedicineEntry -> TYPE_LOG_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_DATE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_medicine_date_header, parent, false)
            DateViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_medicine_log, parent, false)
            LogViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ReportItem.DateHeader -> (holder as DateViewHolder).tvDate.text = "Date: ${item.date}"
            is ReportItem.MedicineEntry -> {
                val logHolder = holder as LogViewHolder
                logHolder.tvName.text = item.log.medicineName
                logHolder.tvTime.text = item.log.scheduledTime
                logHolder.tvStatus.text = item.log.status

                // Optional: color code
                if (item.log.status == "Missed") {
                    logHolder.tvStatus.setTextColor(0xFFFF0000.toInt()) // red
                } else if (item.log.status == "Taken") {
                    logHolder.tvStatus.setTextColor(0xFF00AA00.toInt()) // green
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
