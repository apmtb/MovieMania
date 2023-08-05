package com.example.moviemania

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

class UserActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var preferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user)
        auth = FirebaseAuth.getInstance()
        preferences = getSharedPreferences("MovieMania", MODE_PRIVATE)
        val button = findViewById<Button>(R.id.button2)
        button.setOnClickListener{
            signOut()
        }
    }
    private fun signOut() {
        auth.signOut()
        navigateToMainActivity()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        preferences.edit().putBoolean("isLoggedIn",false).apply()
        preferences.edit().putString("userUid", "").apply()
        // Clear the back stack so that the user cannot go back to the ProfileActivity after logout
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}