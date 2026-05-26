package com.example.healthcare.planner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.R
import com.example.healthcare.database.entities.TodayNotification

class NotificationAdapter :
    ListAdapter<TodayNotification, NotificationAdapter.NotificationViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TodayNotification>() {
            override fun areItemsTheSame(oldItem: TodayNotification, newItem: TodayNotification) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: TodayNotification, newItem: TodayNotification) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_card, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_category)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvDatetime: TextView = itemView.findViewById(R.id.tv_datetime)

        fun bind(notification: TodayNotification) {
            tvCategory.text = notification.category
            tvName.text = notification.name
            tvDatetime.text = "${notification.date} ${notification.time}"
        }
    }
}
