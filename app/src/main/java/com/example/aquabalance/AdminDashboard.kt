package com.example.AquaBalance

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.AquaBalance.databinding.AdmindashboardBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.*
import java.io.IOException

class AdminDashboard : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: AdmindashboardBinding
    private lateinit var mMap: GoogleMap
    private lateinit var feedbackRecyclerView: RecyclerView
    private lateinit var feedbackAdapter: FeedbackAdapter
    private val db = FirebaseFirestore.getInstance()
    private lateinit var feedbackListener: ListenerRegistration
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdmindashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize NotificationManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        // Set click listener for the search button
        binding.btnSearch.setOnClickListener {
            val location = binding.searchEditText.text.toString().trim()
            if (location.isNotEmpty()) {
                searchInMap(location)
            } else {
                Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show()
            }
        }

        // Set click listeners for other buttons
        binding.btnConsumer.setOnClickListener {
            val location = binding.searchEditText.text.toString().trim()
            if (location.isNotEmpty()) {
                searchAndPassLocation(location)
            } else {
                Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize feedback RecyclerView
        feedbackRecyclerView = binding.feedbackRecyclerView
        feedbackAdapter = FeedbackAdapter()
        feedbackRecyclerView.layoutManager = LinearLayoutManager(this)
        feedbackRecyclerView.adapter = feedbackAdapter

        // Setup feedback listener
        listenForFeedbackUpdates()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set default location and zoom level
        val defaultLocation = LatLng(-1.286389, 36.817223)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))

        // Example marker (replace with actual search result)
        mMap.addMarker(MarkerOptions().position(defaultLocation).title("Default Location"))
    }

    private fun searchInMap(location: String) {
        val geocoder = Geocoder(this)
        try {
            val addressList: List<Address>? = geocoder.getFromLocationName(location, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)

                // Clear existing markers
                mMap.clear()

                // Update map with marker
                mMap.addMarker(MarkerOptions().position(latLng).title(location))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error searching for location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchAndPassLocation(location: String) {
        val geocoder = Geocoder(this)
        try {
            val addressList: List<Address>? = geocoder.getFromLocationName(location, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0]
                val regionName = address.locality ?: "Unknown"
                val geographicalArea = "${address.latitude}, ${address.longitude}"

                val intent = Intent(this, DataCollectionActivity::class.java).apply {
                    putExtra("regionName", regionName)
                    putExtra("geographicalArea", geographicalArea)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error searching for location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listenForFeedbackUpdates() {
        feedbackListener = db.collection("feedback")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Failed to listen for feedback: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val feedbackList = snapshots.documents.mapNotNull { it.toObject(FeedbackAdapter.Feedback::class.java) }
                    feedbackAdapter.updateData(feedbackList)

                    val newFeedback = feedbackList.firstOrNull()
                    if (newFeedback != null) {
                        showNewFeedbackNotification(newFeedback.feedback)
                    }
                }
            }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Admin Channel"
            val descriptionText = "Channel for admin notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("admin_channel", name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNewFeedbackNotification(feedback: String) {
        val builder = NotificationCompat.Builder(this, "admin_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New Feedback")
            .setContentText(feedback)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(2, builder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        feedbackListener.remove()
    }
}
