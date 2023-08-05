package com.example.moviemania

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth


class ForgotPassword : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var videoView: VideoView
    private lateinit var frameLayout: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password)
        auth = FirebaseAuth.getInstance()
        videoView = findViewById(R.id.videoViewLoadingCircleFP)
        frameLayout = findViewById(R.id.frameLayoutFP)
        val submitEmailButton = findViewById<Button>(R.id.submitemailforgetpass)
        submitEmailButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.emailforgetpass).text.toString().trim()
            if (validateForm(email)) {
                frameLayout.visibility = View.VISIBLE
                videoView.setOnPreparedListener {
                    it.isLooping = true
                }
                val videoPath = "android.resource://" + packageName + "/" + R.raw.circle_loading

                videoView.setVideoURI(Uri.parse(videoPath))
                videoView.setZOrderOnTop(true)

                videoView.start()
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        frameLayout.visibility = View.GONE
                        videoView.stopPlayback()
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this,"Email not Found, Sign up if you are new!", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        val goBackText = findViewById<TextView>(R.id.backtologin)
        goBackText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun validateForm(email: String): Boolean {
        val icon = ContextCompat.getDrawable(this, R.drawable.ic_custom_error)
        icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)

        if (email.isEmpty()) {
            findViewById<EditText>(R.id.emailforgetpass).setError("Email is required.", icon)
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            findViewById<EditText>(R.id.emailforgetpass).setError("Invalid email address.", icon)
            return false
        }
        return true
    }
}