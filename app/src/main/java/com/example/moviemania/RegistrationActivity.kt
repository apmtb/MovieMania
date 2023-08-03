package com.example.moviemania

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.VideoView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationActivity : AppCompatActivity() {
    private lateinit var preferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var videoView: VideoView
    private lateinit var frameLayout: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = getSharedPreferences("MovieMania", MODE_PRIVATE)
        setContentView(R.layout.registration)

        // Initialize Firebase Authentication instance
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        usernameEditText = findViewById<EditText>(R.id.username)
        emailEditText = findViewById<EditText>(R.id.email)
        passwordEditText = findViewById<EditText>(R.id.password1)
        confirmPasswordEditText = findViewById<EditText>(R.id.password2)
        videoView = findViewById(R.id.videoViewLoadingCircle)
        frameLayout = findViewById<View>(R.id.frameLayout)

        val signUpButton = findViewById<Button>(R.id.signupbtn)
        signUpButton.setOnClickListener {
            clearErrorMessages()
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (validateForm(username, email, password, confirmPassword)) {
                // If form is valid, create user account
                frameLayout.visibility = View.VISIBLE
                videoView.setOnPreparedListener{
                    it.isLooping = true
                }
                // Set the video path from the raw folder
                val videoPath = "android.resource://" + packageName + "/" + R.raw.circle_loading

                videoView.setVideoURI(Uri.parse(videoPath))
                // Start playing the video
                videoView.setZOrderOnTop(true)

                videoView.start()
                createUserAccount(email, password)
            }
        }
        val button = findViewById<Button>(R.id.signinbtn)
        button.setOnClickListener {
            navigateToMainActivity()
            finish()
        }
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

    private fun clearErrorMessages() {
        usernameEditText.error = null
        emailEditText.error = null
        passwordEditText.error = null
        confirmPasswordEditText.error = null
    }

    private fun validateForm(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        val icon = ContextCompat.getDrawable(this, R.drawable.ic_custom_error)
        icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
        if (username.isEmpty()) {
            usernameEditText.setError("Username is required.", icon)
            return false
        }

        if (email.isEmpty()) {
            emailEditText.setError("Email is required.", icon)
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email address.", icon)
            return false
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required.", icon)
            return false
        }

        if (!password.any { it.isUpperCase() }) {
            passwordEditText.setError("Password must contain at least one uppercase letter.", icon)
            return false
        }

        if (!password.any { it.isLowerCase() }) {
            passwordEditText.setError("Password must contain at least one lowercase letter.", icon)
            return false
        }

        if (!password.any { it.isDigit() }) {
            passwordEditText.setError("Password must contain at least one digit.", icon)
            return false
        }

        val specialCharPattern = "[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]"
        if (!password.any { it.toString().matches(specialCharPattern.toRegex()) }) {
            passwordEditText.setError(
                "Password must contain at least one special character (e.g., @, #, $).",
                icon
            )
            return false
        }

        if (password.length < 8) {
            passwordEditText.setError("Password must be at least 8 characters long.", icon)
            return false
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("Confirm password is required.", icon)
            return false
        }

        if (password != confirmPassword) {
            confirmPasswordEditText.setError("Passwords do not match.", icon)
            return false
        }

        return true
    }

    private fun createUserAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val displayName = usernameEditText.text?.toString() ?: ""
                    user?.let {
                        // Create a map of user data to be stored in Firestore
                        val userData = hashMapOf(
                            "displayName" to displayName,
                            "email" to user.email,
                            "profileImageUrl" to user.photoUrl?.toString()
                            // Add other user information as needed
                        )

                        // Store user data in Firestore
                        firestore.collection("Users").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                // User data stored in Firestore successfully, login is successful
                                Toast.makeText(this, "Sign up and login successful!", Toast.LENGTH_SHORT).show()
                                preferences.edit().putString("userUid", user.uid).apply()
                                preferences.edit().putBoolean("isLoggedIn", true).apply()
                                val intent = Intent(this, ShowProfile::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Sign up successful, but user data storage failed.", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    val errorCode = task.exception?.message
                    frameLayout.visibility = View.GONE
                    Toast.makeText(this, "Error : $errorCode", Toast.LENGTH_SHORT).show()
                }
            }
    }
}