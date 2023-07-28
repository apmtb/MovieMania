package com.example.moviemania

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // Set up the ActivityResultLauncher
    private val googleSignInResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            data?.let {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it)
                try {
                    val account = task.getResult(ApiException::class.java)
                    // Sign-in successful, store user data in Firebase
                    storeUserDataInFirebase(account)
                    val intent = Intent(this, ShowProfile::class.java)
                    intent.putExtra("displayName", account?.displayName)
                    intent.putExtra("email", account?.email)
                    intent.putExtra("photoUrl", account?.photoUrl.toString())
                    startActivity(intent)
                } catch (e: Exception) {
                    // Handle sign-in failure (e.g., show an error message)
                    Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Initialize Firebase Authentication and Realtime Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Initialize the GoogleSignInClient
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set onClickListener for the Google Sign-In button
        val btnGoogleSignIn = findViewById<ImageView>(R.id.googleImg)
        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInResultLauncher.launch(signInIntent)
    }


    private fun storeUserDataInFirebase(account: GoogleSignInAccount?) {
        account?.let {
            val userId = it.id ?: ""
            val userData = hashMapOf(
                "displayName" to it.displayName,
                "email" to it.email,
                "photoUrl" to it.photoUrl.toString() // Store the user's photo URL as a string
                // Add other user information as needed
            )

            // Store user data in Firebase Realtime Database
            database.child("users").child(userId).setValue(userData)
                .addOnSuccessListener {
                    // User data stored in Firebase successfully, login is successful
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Error occurred while storing user data
                    Toast.makeText(this, "Failed to store user data. Please try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}