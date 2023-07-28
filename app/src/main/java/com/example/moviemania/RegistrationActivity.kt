package com.example.moviemania

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)
        val button = findViewById<Button>(R.id.signinbtn)
        button.setOnClickListener{
            finish()
        }
    }
}