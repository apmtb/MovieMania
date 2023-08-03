package com.example.moviemania

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.facebook.AccessToken
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.OAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var callbackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var preferences: SharedPreferences
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

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
                    preferences.edit().putBoolean("isLoggedIn", true).apply()
                    preferences.edit().putString("userUid", account.id).apply()
                    // Sign-in successful, store user data in Firestore
                    storeUserDataInFirestore(account)
                    val intent = Intent(this, ShowProfile::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: ApiException) {
                    // Handle sign-in failure (e.g., show an error message)
                    Toast.makeText(
                        this,
                        "Google Sign-in failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = getSharedPreferences("MovieMania", MODE_PRIVATE)
        firestore = FirebaseFirestore.getInstance()
        // Initialize Firebase Authentication and Firestore
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.login)


        emailEditText = findViewById<EditText>(R.id.emailEditText)
        passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set onClickListener for the Google Sign-In button
        val btnGoogleSignIn = findViewById<ImageView>(R.id.googleImg)
        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        val signUpButton = findViewById<TextView>(R.id.SignUp)
        signUpButton.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }

        val signInButton = findViewById<Button>(R.id.loginbtn)
        signInButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (validateForm(email, password)) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            preferences.edit().putBoolean("isLoggedIn", true).apply()
                            preferences.edit().putString("userUid", auth.currentUser?.uid).apply()
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, ShowProfile::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorCode = task.exception?.message
                            Toast.makeText(this, "Error : $errorCode", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        var forgetPassText = findViewById<TextView>(R.id.ForgotPassTextView)
        forgetPassText.setOnClickListener{
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
            finish()
        }

        callbackManager = CallbackManager.Factory.create()
        val buttonFacebookLogin = findViewById<ImageView>(R.id.facebookImg)
        buttonFacebookLogin.setOnClickListener {
            signInWithFacebook()
        }
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    Toast.makeText(
                        this@LoginActivity,
                        "Facebook Sign-in Canceled!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Facebook Sign-in failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        val twitterLoginButton = findViewById<ImageView>(R.id.twitterImg)
        twitterLoginButton.setOnClickListener {
            signInWithTwitter()
        }
    }

    private fun validateForm(
        email: String,
        password: String
    ): Boolean {
        val icon = ContextCompat.getDrawable(this, R.drawable.ic_custom_error)
        icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)

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

        if (password.length < 8) {
            passwordEditText.setError("Password must be at least 8 characters long.", icon)
            return false
        }

        return true
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInResultLauncher.launch(signInIntent)
    }

    private fun storeUserDataInFirestore(account: GoogleSignInAccount?) {
        account?.let {
            val userId = it.id ?: ""
            val userData = hashMapOf(
                "displayName" to it.displayName,
                "email" to it.email,
                "profileImageUrl" to it.photoUrl.toString() // Store the user's photo URL as a string
                // Add other user information as needed
            )

            // Store user data in Firestore
            firestore.collection("Users").document(userId)
                .set(userData)
                .addOnSuccessListener {
                    // User data stored in Firestore successfully, login is successful
                    Toast.makeText(this, "Google Login successful!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Error occurred while storing user data
                    Toast.makeText(
                        this,
                        "Google Sign-in failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun signInWithFacebook() {
        // Log out any existing Facebook session to start a fresh login
        LoginManager.getInstance().logOut()

        // Start Facebook Login process
        val permissions = listOf("public_profile", "email")
        LoginManager.getInstance().logInWithReadPermissions(this, permissions)
    }


    private fun handleFacebookAccessToken(token: AccessToken) {

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    // Store user data in Firestore if the user is not null
                    user?.let {
                        val userId = it.uid
                        val displayName = it.displayName ?: ""
                        val email = it.email ?: ""
                        val profileImageUrl =
                            it.photoUrl?.toString() + "?access_token=" + token.token + "&type=large"

                        val userData = hashMapOf(
                            "displayName" to displayName,
                            "email" to email,
                            "profileImageUrl" to profileImageUrl
                            // Add other user information as needed
                        )

                        // Store user data in Firestore
                        firestore.collection("Users").document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                // User data stored in Firestore successfully, login is successful
                                Toast.makeText(
                                    this,
                                    "Facebook login successful!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Proceed to the next activity (e.g., ShowProfile activity)
                                preferences.edit().putBoolean("isLoggedIn", true).apply()
                                preferences.edit().putString("userUid", userId).apply()
                                val intent = Intent(this, ShowProfile::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                // Error occurred while storing user data
                                Toast.makeText(
                                    this,
                                    "Facebook Sign-in failed. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }

                } else {
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private val twitterSignInResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultCode = result.resultCode
        val data = result.data
        if (data != null) {
            handleTwitterSignInResult(resultCode, data)
        }
    }

    private fun signInWithTwitter() {
        // Create a Twitter OAuth provider
        val provider = OAuthProvider.newBuilder("twitter.com")

        // Start the sign-in flow using a Custom Chrome Tab
        auth.startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener { authResult ->
                // Handle successful sign-in
                val user = authResult.user
                val userId = user?.uid ?: ""
                val displayName = user?.displayName ?: ""
                val email = user?.email ?: ""
                val profileImageUrl = user?.photoUrl?.toString()?.replace("_normal", "") ?: ""

                val userData = hashMapOf(
                    "displayName" to displayName,
                    "email" to email,
                    "profileImageUrl" to profileImageUrl
                    // Add other user information as needed
                )

                firestore.collection("Users").document(userId)
                    .set(userData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Twitter login successful!", Toast.LENGTH_SHORT).show()
                        // User data stored in Firestore successfully, login is successful
                        // Proceed to the next activity (e.g., ShowProfile activity)
                        preferences.edit().putString("userUid", userId).apply()
                        preferences.edit().putBoolean("isLoggedIn", true).apply()
                        val intent = Intent(this, ShowProfile::class.java)
                        startActivity(intent)
                        finish() // Optional: Finish the LoginActivity to prevent going back
                    }
                    .addOnFailureListener { e ->
                        // Handle sign-in failure
                        Toast.makeText(
                            this,
                            "Twitter sign-in failed. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
    }

    private fun handleTwitterSignInResult(resultCode: Int, data: Intent) {
        // Handle the result of the Twitter sign-in
        // This method is called from the twitterSignInResultLauncher
        auth.getPendingAuthResult()?.let { pendingResultTask ->
            pendingResultTask
                .addOnSuccessListener { authResult ->
                    // Handle successful sign-in
                    Toast.makeText(this, "Twitter login successful!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    // Handle sign-in failure
                    Toast.makeText(
                        this,
                        "Twitter sign-in failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}