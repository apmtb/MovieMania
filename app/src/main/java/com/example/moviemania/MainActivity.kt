package com.example.moviemania

import android.net.Uri
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.widget.FrameLayout
import com.google.firebase.firestore.FirebaseFirestore

//import android.content.SharedPreferences
//import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private var userId: String = ""
    private var isLoggedIn: Boolean = false
    private lateinit var fireStore: FirebaseFirestore
    private var email: String = ""
    private lateinit var preferences: SharedPreferences
    private lateinit var userTypeChecker: UserTypeChecker

    override fun onStart() {
        super.onStart()
        setContentView(R.layout.activity_main)
        preferences = getSharedPreferences("MovieMania", MODE_PRIVATE)
        isLoggedIn = preferences.getBoolean("isLoggedIn", false)
        fireStore = FirebaseFirestore.getInstance()
        userId = preferences.getString("userUid", null) ?: ""
        userTypeChecker = UserTypeChecker(this)
        val frameLayout = findViewById<FrameLayout>(R.id.videobg)

        if (!isLoggedIn) {
            preferences.edit().putBoolean("isNotPlayed", true).apply()
            videoView = findViewById(R.id.videoView)

            // Set the video path from the raw folder
            val videoPath = "android.resource://" + packageName + "/" + R.raw.start_white
            videoView.setVideoURI(Uri.parse(videoPath))

            // Start playing the video
            videoView.setZOrderOnTop(true)
            videoView.start()

            videoView.setOnCompletionListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            preferences.edit().putBoolean("isNotPlayed", false).apply()
            frameLayout.visibility = View.VISIBLE
            val videoView = findViewById<VideoView>(R.id.videoViewLoading)
            val videoView1 = findViewById<VideoView>(R.id.videoViewLoading1)

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

            videoView.setOnCompletionListener {
                userTypeChecker.checkUserTypeAndNavigate(email)
            }
            if (userId != "") {
                val userData = fireStore.collection("Users").document(userId)
                userData.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            email = document.getString("email") ?: ""
                        }
                    }
            }
        }
    }
}