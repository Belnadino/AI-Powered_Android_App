package com.example.healthcare.planner

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.database.entities.Feedback
import com.example.healthcare.R

class FeedbackAdapter(
    val feedbackList: MutableList<Feedback>,
    private val onRate: (Feedback, Int) -> Unit
) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvSupportFeedback: TextView = itemView.findViewById(R.id.tvSupportFeedback)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        FeedbackViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_feedback, parent, false))

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]
        holder.tvSubject.text = feedback.subject
        holder.tvDescription.text = feedback.description
        holder.tvStatus.text = feedback.status
        holder.tvSupportFeedback.text = feedback.supportFeedback ?: ""
        holder.tvSupportFeedback.visibility = if (feedback.supportFeedback != null) View.VISIBLE else View.GONE

        holder.ratingBar.setOnRatingBarChangeListener(null)

        holder.ratingBar.rating = feedback.rating?.toFloat() ?: 0f


        val canRate = feedback.canUserRate()
        holder.ratingBar.setIsIndicator(!canRate)

        if (canRate) {
            holder.ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
                if (fromUser) {
                    feedback.rating = rating.toInt() // update local model
                    onRate(feedback, rating.toInt()) // update DB
                    notifyItemChanged(position)      // refresh this row
                }
            }
        }

        // Item click listener stays the same
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, FeedbackDetailsActivity::class.java)
            intent.putExtra("FEEDBACK_ID", feedback.id)
            intent.putExtra("USER_ID", feedback.userId)
            context.startActivity(intent)
        }
    }

    fun Feedback.canUserRate(): Boolean {
        return category == "Complement" ||
                (category == "Problem" && status == "Closed")
    }

    override fun getItemCount() = feedbackList.size
}
