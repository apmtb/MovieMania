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
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
class ShowProfile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_profile)

        // Retrieve the photoUrl from the intent extras
        val photoUrl = intent.getStringExtra("photoUrl")

        // Load the user's profile image in the ImageView
        loadProfileImage(photoUrl ?: "")
    }
    private fun loadProfileImage(photoUrl: String) {
        // Perform image loading in a background thread to avoid blocking the main UI thread
        Thread {
            val bitmap = getBitmapFromUrl(photoUrl)

            runOnUiThread {
                val profileImageView =findViewById<ImageView>(R.id.profileImageView)
                if (bitmap != null) {
                    // Update the ImageView with the loaded bitmap
                    profileImageView.setImageBitmap(bitmap)
                } else {
                    // Use a default image if loading fails or photoUrl is invalid
                    profileImageView.setImageResource(R.drawable.usericon)
                }
            }
        }.start()
        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

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
        val intent = Intent(this, LoginActivity::class.java)
        // Clear the back stack so that the user cannot go back to the ProfileActivity after logout
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    private fun getBitmapFromUrl(photoUrl: String): Bitmap? {
        var inputStream: InputStream? = null
        try {
            val url = URL(photoUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            inputStream = connection.inputStream
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return null
    }
}