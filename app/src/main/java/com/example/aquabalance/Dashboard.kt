package com.example.AquaBalance

import RationingScheduleAdapter
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.AquaBalance.databinding.DashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class Dashboard : AppCompatActivity() {

    private lateinit var binding: DashboardBinding
    private lateinit var notificationsAdapter: NotificationsAdapter
    private lateinit var rationingTitle: TextView
    private lateinit var regionSpinner: Spinner
    private lateinit var rationingScheduleRecyclerView: RecyclerView
    private lateinit var rationingScheduleAdapter: RationingScheduleAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var currentCapacity: Int = 0
    private var storageCapacity: Int = 0
    private lateinit var notificationManager: NotificationManager
    private var selectedRegion: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI components
        rationingTitle = binding.rationingTitle
        regionSpinner = binding.regionSpinner
        rationingScheduleRecyclerView = binding.rationingScheduleRecyclerView

        setupConsumptionSection()
        setupRegionSpinner()
        setupRationingScheduleRecyclerView()
        setupFeedbackSection()

        val logOutButton: Button = binding.LogOut
        val adminButton: Button = binding.Admin

        logOutButton.setOnClickListener {
            logOut()
        }

        adminButton.setOnClickListener {
            val intent = Intent(this, AdminDashboard::class.java)
            startActivity(intent)
        }

        // Initialize NotificationManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        // Check for intent data to update water level display
        intent.getStringExtra("regionName")?.let { regionName ->
            selectedRegion = regionName
            displayWaterLevel(regionName)
        }
    }

    private fun logOut() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun displayWaterLevel(regionName: String) {
        db.collection("waterLevels").document(regionName)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentCapacity = document.getLong("currentCapacity")?.toInt() ?: 0
                    storageCapacity = document.getLong("storageCapacity")?.toInt() ?: 0

                    val percentage = if (storageCapacity > 0) {
                        (currentCapacity.toFloat() / storageCapacity * 100)
                    } else {
                        0f
                    }

                    binding.circularWaveView.setProgress(percentage)
                    showWaterLevelNotification(regionName, percentage)
                } else {
                    Toast.makeText(this, "Water level data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch water level data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRegionSpinner() {
        db.collection("waterLevels")
            .get()
            .addOnSuccessListener { result ->
                val regions = result.map { it.id }.toTypedArray()
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, regions)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                regionSpinner.adapter = adapter

                regionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        val selectedRegion = regions[position]
                        this@Dashboard.selectedRegion = selectedRegion
                        updateRationingSchedule(selectedRegion)
                        displayWaterLevel(selectedRegion)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        rationingScheduleAdapter.updateData(emptyList())
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch regions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("water_level_channel", name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)

            // Admin notification channel
            val adminChannel = NotificationChannel("admin_channel", "Admin Channel", importance).apply {
                description = "Channel for admin notifications"
            }
            notificationManager.createNotificationChannel(adminChannel)
        }
    }

    private fun showWaterLevelNotification(region: String, percentage: Float) {
        val builder = NotificationCompat.Builder(this, "water_level_channel")
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("Water Level Update")
            .setContentText("$region water level: ${percentage.toInt()}%")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }

    private fun setupConsumptionSection() {
        binding.refreshButton.setOnClickListener {
            refreshConsumptionData()
        }
    }

    private fun refreshConsumptionData() {
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        binding.lastUpdatedTextView.text = "Last updated: $currentTime"
    }

    private fun setupRationingScheduleRecyclerView() {
        rationingScheduleAdapter = RationingScheduleAdapter()
        rationingScheduleRecyclerView.layoutManager = LinearLayoutManager(this)
        rationingScheduleRecyclerView.adapter = rationingScheduleAdapter
    }

    private fun updateRationingSchedule(region: String) {
        // Implement logic to fetch and update rationing schedule for the selected region
        // This might involve a Firestore query or some other data source
        // For now, we'll use a placeholder empty list
        rationingScheduleAdapter.updateData(emptyList())
    }

    private fun setupFeedbackSection() {
        binding.submitFeedbackButton.setOnClickListener {
            if (selectedRegion != null) {
                showFeedbackDialog(selectedRegion!!)
            } else {
                Toast.makeText(this, "Please select a region first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showFeedbackDialog(region: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_feedback, null)
        val feedbackEditText = dialogView.findViewById<EditText>(R.id.feedbackEditText)
        val submitButton = dialogView.findViewById<Button>(R.id.submitFeedbackButton)

        val feedbackDialog = AlertDialog.Builder(this)
            .setTitle("Submit Feedback")
            .setView(dialogView)
            .create()

        submitButton.setOnClickListener {
            val feedback = feedbackEditText.text.toString()
            if (feedback.isNotEmpty()) {
                submitFeedback(region, feedback)
                feedbackDialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show()
            }
        }

        feedbackDialog.show()
    }

    private fun submitFeedback(region: String, feedback: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val feedbackData = hashMapOf(
                "userId" to userId,
                "region" to region,
                "feedback" to feedback,
                "timestamp" to Date()
            )
            db.collection("feedback").add(feedbackData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()

                    // Send notification to AdminDashboard
                    sendNotificationToAdmin(feedback)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to submit feedback: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendNotificationToAdmin(feedback: String) {
        val builder = NotificationCompat.Builder(this, "admin_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New Feedback")
            .setContentText(feedback)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(2, builder.build())
    }
}
