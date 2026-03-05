package com.example.AquaBalance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.AquaBalance.databinding.ConsumerdataBinding
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class DataCollectionActivity : AppCompatActivity() {

    private lateinit var binding: ConsumerdataBinding
    private lateinit var apiService: ApiService
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ConsumerdataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize API service
        apiService = ApiClient.getClient().create(ApiService::class.java)

        // Get the passed data from the intent
        val regionName = intent.getStringExtra("regionName")

        // Autofill the region name if available
        binding.RegionName.setText(regionName)

        binding.btnPost.setOnClickListener {
            postDataToDashboard()
        }

        binding.btnResults.setOnClickListener {
            calculateThreshold()
        }

        // Example call to fetch additional data, if needed
        fetchAdditionalData()
    }

    private fun fetchAdditionalData() {
        val call = apiService.getData() // Adjust as necessary for your API

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responseData = response.body()?.string()
                        // Handle the response data as needed
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@DataCollectionActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@DataCollectionActivity, "Failure: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun saveDataToPreferences(key: String, value: String) {
        val sharedPreferences = getSharedPreferences("com.example.maji.PREFERENCES", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun calculateThreshold() {
        val regionName = binding.RegionName.text.toString().trim()
        val populationStr = binding.Population.text.toString().trim()
        val storageCapacityStr = binding.StorageCapacity.text.toString().trim()
        val currentCapacityStr = binding.CurrentCapacity.text.toString().trim()
        val flowRateStr = binding.FlowRateMin.text.toString().trim()

        // Validation
        if (regionName.isEmpty() || populationStr.isEmpty() || storageCapacityStr.isEmpty() ||
            currentCapacityStr.isEmpty() || flowRateStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert inputs to appropriate types
        val population = populationStr.toIntOrNull()
        val storageCapacity = storageCapacityStr.toIntOrNull()
        val currentCapacity = currentCapacityStr.toIntOrNull()
        val flowRate = flowRateStr.toDoubleOrNull()

        // Check for valid numeric values
        if (population == null || storageCapacity == null ||
            currentCapacity == null || flowRate == null) {
            Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculate total daily usage in liters per minute
        val dailyTotalUsage = population * flowRate

        // Calculate remaining capacity
        val remainingCapacity = storageCapacity - currentCapacity

        // Calculate expected time to fill total capacity in minutes
        val timeToFillTotalCapacity = storageCapacity / dailyTotalUsage.toDouble()
        val formattedTimeToFillTotalCapacity = formatTime(timeToFillTotalCapacity)

        // Calculate expected time to fill remaining capacity in minutes
        val timeToFillRemainingCapacity = remainingCapacity / dailyTotalUsage.toDouble()
        val formattedTimeToFillRemainingCapacity = formatTime(timeToFillRemainingCapacity)

        // Determine capacity threshold message
        val capacityThreshold = if (storageCapacity >= dailyTotalUsage) {
            "Sufficient storage capacity"
        } else {
            "Insufficient storage capacity"
        }

        // Display result in a TextView
        val result = """
            Region Name: $regionName
            Population: $population
            Storage Capacity: $storageCapacity liters
            Current Capacity: $currentCapacity liters
            Daily Total Usage: $dailyTotalUsage liters/min
            
            Expected Time to Fill Total Capacity: $formattedTimeToFillTotalCapacity
            Expected Time to Fill Remaining Capacity: $formattedTimeToFillRemainingCapacity
            
            Capacity Threshold: $capacityThreshold
        """.trimIndent()

        // Set the result in the TextView
        binding.tvResults.text = result
    }

    private fun postDataToDashboard() {
        val regionName = binding.RegionName.text.toString().trim()
        val currentCapacity = binding.CurrentCapacity.text.toString().trim().toIntOrNull()
        val storageCapacity = binding.StorageCapacity.text.toString().trim().toIntOrNull()

        if (currentCapacity != null && storageCapacity != null && regionName.isNotEmpty()) {
            // Save current capacity and storage capacity to Firestore
            val waterLevelData = hashMapOf(
                "currentCapacity" to currentCapacity,
                "storageCapacity" to storageCapacity
            )

            db.collection("waterLevels").document(regionName)
                .set(waterLevelData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Water level updated successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Dashboard::class.java).apply {
                        putExtra("regionName", regionName)
                        // Pass the total storage capacity if needed in the Dashboard activity
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update water level: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Invalid region name, current capacity, or storage capacity", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatTime(hours: Double): String {
        val minutes = (hours * 60).toInt() % 60
        val seconds = (hours * 3600).toInt() % 60
        return String.format("%02d:%02d:%02d", hours.toInt(), minutes, seconds)
    }
}
