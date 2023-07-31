package com.example.moviemania

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
class ShowProfile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_profile)
        auth = FirebaseAuth.getInstance()
        // Retrieve the photoUrl from the intent extras
        val photoUrl = intent.getStringExtra("profileImageUrl")
        val imageView = findViewById<ImageView>(R.id.profileImageView)
        // Load the user's profile image in the ImageView
        if(!photoUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(photoUrl)
                .error(R.drawable.usericon) // Set an error image if loading fails
                .into(imageView)
        } else {
            Picasso.get()
                .load(R.drawable.usericon) // Set an error image if loading fails
                .into(imageView)
        }

    // Handle the logout button click
    val btnLogout = findViewById<Button>(R.id.btnLogout)
    btnLogout.setOnClickListener {
        signOut()
    }

    // Retrieve user data from the previous activity (you should have passed the data from the LoginActivity)
    val displayName = intent.getStringExtra("displayName")
    val email = intent.getStringExtra("email")
    val textUsername = findViewById<TextView>(R.id.usernameShow)
    val textEmail = findViewById<TextView>(R.id.emailShow)
    textUsername.text = displayName
    textEmail.text = email

}

    private fun signOut() {
        auth.signOut()
        navigateToMainActivity()
    }

    private fun navigateToMainActivity() {
        val preferences = getSharedPreferences("MovieMania", MODE_PRIVATE)
        preferences.edit().putBoolean("isLoggedIn",false).apply()
        val intent = Intent(this, LoginActivity::class.java)
        // Clear the back stack so that the user cannot go back to the ProfileActivity after logout
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}