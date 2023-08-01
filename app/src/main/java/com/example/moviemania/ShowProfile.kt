package com.example.moviemania

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
class ShowProfile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var preferences: SharedPreferences
    private var isLoggedin = false
    private lateinit var displayName: String
    private lateinit var email: String
    private lateinit var photoUrl: String
    private lateinit var videoView: VideoView
    private lateinit var videoView1: VideoView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_profile)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        preferences = getSharedPreferences("MovieMania", MODE_PRIVATE)
        isLoggedin = preferences.getBoolean("isLoggedIn",false)
        val imageView = findViewById<ImageView>(R.id.profileImageView)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        if (isLoggedin) {
            videoView = findViewById(R.id.videoViewLoading)
            videoView1 = findViewById(R.id.videoViewLoading1)

            // Set the video path from the raw folder
            val videoPath = "android.resource://" + packageName + "/" + R.raw.loading
            val videoPath1 = "android.resource://" + packageName + "/" + R.raw.loading1

            videoView.setVideoURI(Uri.parse(videoPath))
            videoView1.setVideoURI(Uri.parse(videoPath1))
            // Start playing the video
            videoView.setZOrderOnTop(true)
            videoView1.setZOrderOnTop(true)

            videoView.start()
            videoView1.start()

            imageView.visibility = View.VISIBLE
            btnLogout.visibility = View.VISIBLE

            videoView.setOnCompletionListener {
                val parent = videoView.parent.parent.parent as ViewGroup
                parent.removeView(findViewById(R.id.videobg))
            }

            // User is already logged in, proceed to the profile screen
            val userId = auth.currentUser?.uid
            if (userId!=null) {
                val userData = firestore.collection("Users").document(userId ?: "")
                userData.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            displayName = document.getString("displayName") ?: ""
                            email = document.getString("email") ?: ""
                            photoUrl = document.getString("profileImageUrl") ?: ""
                            // Load the user's profile image in the ImageView
                            if (!photoUrl.isNullOrEmpty()) {
                                Picasso.get()
                                    .load(photoUrl)
                                    .error(R.drawable.usericon) // Set an error image if loading fails
                                    .into(imageView)
                            } else {
                                Picasso.get()
                                    .load(R.drawable.usericon) // Set an error image if loading fails
                                    .into(imageView)
                            }
                            val textUsername = findViewById<TextView>(R.id.usernameShow)
                            val textEmail = findViewById<TextView>(R.id.emailShow)
                            textUsername.text = displayName
                            textEmail.text = email
                        } else {
                            // Null document
                            Toast.makeText(
                                this,
                                "User Not Found, Register First",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener {
                        // Error occurred while fetching user data
                        Toast.makeText(
                            this,
                            "Failed to retrieve user data. Please try again!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(this, "User Not Found Try Again With Different Account!", Toast.LENGTH_SHORT).show()
                signOut()
            }
        } else {
            signOut()
        }
        // Handle the logout button click
        btnLogout.setOnClickListener {
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
        // Clear the back stack so that the user cannot go back to the ProfileActivity after logout
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}