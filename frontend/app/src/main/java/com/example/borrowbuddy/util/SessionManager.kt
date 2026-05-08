package com.example.borrowbuddy.util

import android.content.Context
import android.content.SharedPreferences
import com.example.borrowbuddy.model.User
import java.util.UUID

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("BorrowBuddySession", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        val editor = prefs.edit()
        editor.putString("USER_ID", user.id.toString())
        editor.putString("USER_FULL_NAME", user.fullName)
        editor.putFloat("USER_TRUST_SCORE", user.trustScore)
        editor.putInt("USER_ITEMS_LENT", user.itemsLent)
        editor.putInt("USER_ITEMS_BORROWED", user.itemsBorrowed)
        editor.putString("USER_REG_NUMBER", user.registrationNumber)
        editor.putString("USER_EMAIL", user.email)
        editor.apply()
    }

    fun getUser(): User? {
        val idString = prefs.getString("USER_ID", null)
        val fullName = prefs.getString("USER_FULL_NAME", null)
        
        // Safely handle migration from Int to Float for trustScore
        val trustScore = try {
            prefs.getFloat("USER_TRUST_SCORE", 0.0f)
        } catch (e: ClassCastException) {
            // If old data is Int, clear it or convert it
            0.0f
        }

        val itemsLent = prefs.getInt("USER_ITEMS_LENT", 0)
        val itemsBorrowed = prefs.getInt("USER_ITEMS_BORROWED", 0)
        val regNumber = prefs.getString("USER_REG_NUMBER", null)
        val email = prefs.getString("USER_EMAIL", null)

        if (idString != null && fullName != null) {
            return User(
                id = UUID.fromString(idString),
                fullName = fullName,
                trustScore = trustScore,
                itemsLent = itemsLent,
                itemsBorrowed = itemsBorrowed,
                registrationNumber = regNumber,
                email = email
            )
        }
        return null
    }

    fun getBaseUrl(): String {
        return prefs.getString("BASE_URL", "http://10.158.204.45:8000/") ?: "http://10.158.204.45:8000/"
    }

    fun saveBaseUrl(url: String) {
        prefs.edit().putString("BASE_URL", url).apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
