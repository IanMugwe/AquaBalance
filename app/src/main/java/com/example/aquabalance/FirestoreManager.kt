package com.example.AquaBalance

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {
    private val db = FirebaseFirestore.getInstance()

    fun getUserRole(userId: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role") ?: "user"
                    onSuccess(role)
                } else {
                    onSuccess("user")
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Add other Firestore operations here
}
