package com.example.AquaBalance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.messaging.RemoteMessage

class NotificationsAdapter(private var notifications: List<RemoteMessage.Notification>) :
    RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.notification_title)
        val messageTextView: TextView = itemView.findViewById(R.id.notification_message)
        val typeTextView: TextView = itemView.findViewById(R.id.notification_type)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.titleTextView.text = notification.title
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<RemoteMessage.Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}
