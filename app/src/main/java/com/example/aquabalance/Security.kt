package com.example.AquaBalance

import android.content.Context
import android.content.SharedPreferences

object Security {

    private const val PREFS_NAME = "user_credentials"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"

    fun storeUserCredentials(context: Context, email: String, password: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            apply()
        }
    }

    fun getUserCredentials(context: Context): Pair<String?, String?> {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val email = sharedPreferences.getString(KEY_EMAIL, null)
        val password = sharedPreferences.getString(KEY_PASSWORD, null)
        return Pair(email, password)
    }
}
