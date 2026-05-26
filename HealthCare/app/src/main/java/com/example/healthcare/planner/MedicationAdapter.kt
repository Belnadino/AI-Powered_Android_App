package com.example.healthcare.planner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.R
import com.example.healthcare.database.Dao.MedicineLogDao
import com.example.healthcare.database.entities.MedicineLog
import com.example.healthcare.database.entities.MedicinePlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MedicationAdapter(
    private val planTimes: MutableList<PlanTimeItem>,
    private val userId: Int,
    private val logDao: MedicineLogDao?,
    private val scope: CoroutineScope,
    private val onEditClick: ((MedicinePlan) -> Unit)? = null,
    private val onDeleteClick: ((MedicinePlan) -> Unit)? = null
) : RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder>() {

    inner class MedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvMedicineName)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val btnTaken: Button = itemView.findViewById(R.id.btnTaken)
        val btnEdit: Button? = itemView.findViewById(R.id.btnEditPlan)
        val btnDelete: Button? = itemView.findViewById(R.id.btnDeletePlan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicine, parent, false)
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val item = planTimes[position]
        val plan = item.plan
        val scheduledTime = item.time
        val today = LocalDate.now().toString()

        holder.tvName.text = plan.medicineName
        holder.tvTime.text = "Time: $scheduledTime"

        scope.launch(Dispatchers.IO) {
            val existingLog = logDao?.getLogsForUserAndDate(plan.userId, today)
                ?.filter { it.planId == plan.id && it.scheduledTime == scheduledTime }
                ?.maxByOrNull { it.id }

            holder.btnTaken.post {
                if (existingLog != null) {
                    holder.btnTaken.text = existingLog.status
                    holder.btnTaken.isEnabled = false
                } else {
                    holder.btnTaken.text = "Take"
                    holder.btnTaken.isEnabled = true
                    holder.btnTaken.setOnClickListener {
                        scope.launch(Dispatchers.IO) {
                            val takenLog = MedicineLog(
                                userId = plan.userId,
                                planId = plan.id,
                                medicineName = plan.medicineName,
                                scheduledTime = scheduledTime,
                                actualTimeTaken = LocalTime.now().toString(),
                                date = today,
                                status = "Taken"
                            )
                            logDao?.insert(takenLog)

                            holder.btnTaken.post {
                                holder.btnTaken.text = "Taken"
                                holder.btnTaken.isEnabled = false
                            }
                        }
                    }
                }
            }
        }

        holder.btnEdit?.setOnClickListener { onEditClick?.invoke(plan) }
        holder.btnDelete?.setOnClickListener { onDeleteClick?.invoke(plan) }
    }

    override fun getItemCount(): Int = planTimes.size

    fun updatePlanTimes(newPlanTimes: List<PlanTimeItem>, newLogs: List<MedicineLog>) {
        planTimes.clear()
        planTimes.addAll(newPlanTimes)
        notifyDataSetChanged()
    }
}
