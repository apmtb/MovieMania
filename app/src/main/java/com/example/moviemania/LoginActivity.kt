package com.example.moviemania

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login) // This sets the login.xml layout as the content view
        val signUpText = findViewById<TextView>(R.id.SignUp)
        signUpText.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    // Add the login logic here
    // For example, handling login button clicks, authentication, etc.
}