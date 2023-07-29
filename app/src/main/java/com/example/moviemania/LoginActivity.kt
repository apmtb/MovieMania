package com.example.moviemania

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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

class LoginActivity : AppCompatActivity() {

    private lateinit var callbackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    var TAG=""
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
                    // Sign-in successful, store user data in Firestore
                    storeUserDataInFirestore(account)
                    val intent = Intent(this, ShowProfile::class.java)
                    intent.putExtra("displayName", account?.displayName)
                    intent.putExtra("email", account?.email)
                    intent.putExtra("profileImageUrl", account?.photoUrl.toString())
                    startActivity(intent)
                } catch (e: ApiException) {
                    // Handle sign-in failure (e.g., show an error message)
                    Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Initialize Firebase Authentication and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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

        callbackManager = CallbackManager.Factory.create()
        val buttonFacebookLogin = findViewById<ImageView>(R.id.facebookImg)
        buttonFacebookLogin.setOnClickListener{
            if(LoggedIn()){
                auth.signOut()
            } else {
                signInWithFacebook()
            }
        }
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken)
            }

            override fun onCancel() {
                Toast.makeText(this@LoginActivity, "Sign-in Canceled!", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(this@LoginActivity, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        })
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
                "photoUrl" to it.photoUrl.toString() // Store the user's photo URL as a string
                // Add other user information as needed
            )

            // Store user data in Firestore
            firestore.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener {
                    // User data stored in Firestore successfully, login is successful
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Error occurred while storing user data
                    Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun LoggedIn(): Boolean {
        if (auth.currentUser!=null && AccessToken.getCurrentAccessToken()!!.isExpired) {
            return true
        }
        return false
    }

    private fun signInWithFacebook() {
        // Log out any existing Facebook session to start a fresh login
        LoginManager.getInstance().logOut()

        // Start Facebook Login process
        val permissions = listOf("public_profile", "email")
        LoginManager.getInstance().logInWithReadPermissions(this, permissions)
    }




        private fun handleFacebookAccessToken(token: AccessToken) {
            Log.d(TAG, "handleFacebookAccessToken:$token")

            val credential = FacebookAuthProvider.getCredential(token.token)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        val user = auth.currentUser

                        // Store user data in Firestore if the user is not null
                        user?.let {
                            val userId = it.uid
                            val displayName = it.displayName ?: ""
                            val email = it.email ?: ""
                            val profileImageUrl = it.photoUrl?.toString() ?: ""

                            val userData = hashMapOf(
                                "displayName" to displayName,
                                "email" to email,
                                "profileImageUrl" to profileImageUrl
                                // Add other user information as needed
                            )

                            // Store user data in Firestore
                            firestore.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener {
                                    // User data stored in Firestore successfully, login is successful
                                    Toast.makeText(this, "Facebook login successful!", Toast.LENGTH_SHORT).show()

                                    // Proceed to the next activity (e.g., ShowProfile activity)
                                    val intent = Intent(this, ShowProfile::class.java)
                                    intent.putExtra("displayName", displayName)
                                    intent.putExtra("email", email)
                                    intent.putExtra("profileImageUrl", profileImageUrl)
                                    startActivity(intent)
                                }
                                .addOnFailureListener {
                                    // Error occurred while storing user data
                                    Toast.makeText(this, "Failed to store user data. Please try again.", Toast.LENGTH_SHORT).show()
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
}