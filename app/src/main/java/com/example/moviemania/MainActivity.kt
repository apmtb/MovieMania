package com.example.moviemania

import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

//import android.content.SharedPreferences
//import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView

    override fun onStart() {
        super.onStart()
        val preferences = getSharedPreferences("MovieMania", MODE_PRIVATE)
        val isLoggedIn = preferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            val intent = Intent(this, ShowProfile::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            setContentView(R.layout.activity_main)

            videoView = findViewById(R.id.videoView)

            // Set the video path from the raw folder
            val videoPath = "android.resource://" + packageName + "/" + R.raw.start
            videoView.setVideoURI(Uri.parse(videoPath))

            // Start playing the video
            videoView.setZOrderOnTop(true)
            videoView.start()

            videoView.setOnCompletionListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
    }
}