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
//    private lateinit var sharedPreferences: SharedPreferences

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

//        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//        val firstTime = sharedPreferences.getBoolean("firstTime", true)

//        if(firstTime) {
//            sharedPreferences.edit().putBoolean("firstTime", false).apply()
            setContentView(R.layout.activity_main)

            videoView = findViewById(R.id.videoView)

            // Set the video path from the raw folder
            val videoPath = "android.resource://" + packageName + "/" + R.raw.start
            videoView.setVideoURI(Uri.parse(videoPath))

            // Start playing the video
            videoView.setZOrderOnTop(true)
            videoView.start()

            // Set a listener to navigate to the next screen after video playback
            videoView.setOnCompletionListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
//        } else {
//            setContentView(R.layout.login)
//        }
    }

//    override fun onRestart() {
//        super.onRestart()
//        sharedPreferences.edit().putBoolean("firstTime", true).apply()
//    }
}