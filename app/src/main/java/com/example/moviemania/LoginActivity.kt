package com.example.moviemania

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private lateinit var userTypeChecker: UserTypeChecker

    private lateinit var callbackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var preferences: SharedPreferences
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var videoView: VideoView
    private lateinit var frameLayout: View

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
                    val authCredential: AuthCredential = GoogleAuthProvider.getCredential(
                        account.idToken, null
                    )
                    auth.signInWithCredential(authCredential)
                    storeUserDataInFireStore(account)
                    userTypeChecker.checkUserTypeAndNavigate(account.email.toString())
                } catch (e: ApiException) {
                    stopCircleLoading()
                    Toast.makeText(
                        this,
                        "Google Sign-in failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(this, "Google Sign-in Canceled!", Toast.LENGTH_SHORT).show()
            stopCircleLoading()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = getSharedPreferences("MovieMania", MODE_PRIVATE)
        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.login)


        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        videoView = findViewById(R.id.videoViewLoadingCircleLP)
        frameLayout = findViewById(R.id.frameLayoutLP)
        userTypeChecker = UserTypeChecker(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("835490974290-65ceodpl60mugbumcnf3kar29e0lne6h.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val btnGoogleSignIn = findViewById<ImageView>(R.id.googleImg)
        btnGoogleSignIn.setOnClickListener {
            showCircleLoading()
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
                showCircleLoading()
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            preferences.edit().putBoolean("isLoggedIn", true).apply()
                            preferences.edit().putString("userUid", auth.currentUser?.uid).apply()
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                            userTypeChecker.checkUserTypeAndNavigate(email)
                        } else {
                            val errorCode = task.exception?.message
                            stopCircleLoading()
                            Toast.makeText(this, "Error : $errorCode", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        val forgetPassText = findViewById<TextView>(R.id.ForgotPassTextView)
        forgetPassText.setOnClickListener{
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

        callbackManager = CallbackManager.Factory.create()
        val buttonFacebookLogin = findViewById<ImageView>(R.id.facebookImg)
        buttonFacebookLogin.setOnClickListener {
            showCircleLoading()
            signInWithFacebook()
        }
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    stopCircleLoading()
                    Toast.makeText(this@LoginActivity, "Facebook Sign-in Canceled!", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    stopCircleLoading()
                    Toast.makeText(
                        this@LoginActivity,
                        "Facebook Sign-in failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        val twitterLoginButton = findViewById<ImageView>(R.id.twitterImg)
        twitterLoginButton.setOnClickListener {
            showCircleLoading()
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

    private fun storeUserDataInFireStore(account: GoogleSignInAccount?) {
        account?.let {
            val userId = it.id ?: ""
            val userData = hashMapOf(
                "displayName" to it.displayName,
                "email" to it.email,
                "profileImageUrl" to it.photoUrl.toString()
            )

            val userDocRef = fireStore.collection("Users").document(userId)
            userDocRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        Toast.makeText(this, "Google Login successful!", Toast.LENGTH_SHORT).show()
                    } else {
                        fireStore.collection("Users").document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Google Login successful!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                stopCircleLoading()
                                Toast.makeText(
                                    this,
                                    "Google Sign-in failed. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }
        }
    }

    private fun signInWithFacebook() {
        LoginManager.getInstance().logOut()

        val permissions = listOf("public_profile", "email")
        LoginManager.getInstance().logInWithReadPermissions(this, permissions)
    }


    private fun handleFacebookAccessToken(token: AccessToken) {

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

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
                        )

                        val userDocRef = fireStore.collection("Users").document(userId)
                        userDocRef.get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val document = task.result
                                if ( document != null && document.exists()) {
                                    Toast.makeText(this, "Facebook login successful!",
                                        Toast.LENGTH_SHORT).show()
                                    preferences.edit().putBoolean("isLoggedIn", true).apply()
                                    preferences.edit().putString("userUid", userId).apply()
                                    userTypeChecker.checkUserTypeAndNavigate(email)
                                } else {
                                    fireStore.collection("Users").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this,
                                                "Facebook login successful!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            preferences.edit().putBoolean("isLoggedIn", true).apply()
                                            preferences.edit().putString("userUid", userId).apply()
                                            userTypeChecker.checkUserTypeAndNavigate(email)
                                        }
                                        .addOnFailureListener {
                                            stopCircleLoading()
                                            Toast.makeText(
                                                this,
                                                "Facebook Sign-in failed. Please try again.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                        }
                    }

                } else {
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }


    @Deprecated("")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)
    }



    private fun signInWithTwitter() {
        val provider = OAuthProvider.newBuilder("twitter.com")

        auth.startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                val userId = user?.uid ?: ""
                val displayName = user?.displayName ?: ""
                val email = user?.email ?: ""
                val profileImageUrl = user?.photoUrl?.toString()?.replace("_normal", "") ?: ""

                val userData = hashMapOf(
                    "displayName" to displayName,
                    "email" to email,
                    "profileImageUrl" to profileImageUrl
                )

                val userDocRef = fireStore.collection("Users").document(userId)
                userDocRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null && document.exists()) {
                            Toast.makeText(this, "Twitter login successful!", Toast.LENGTH_SHORT).show()
                            preferences.edit().putString("userUid", userId).apply()
                            preferences.edit().putBoolean("isLoggedIn", true).apply()
                            userTypeChecker.checkUserTypeAndNavigate(email)
                        } else {
                            fireStore.collection("Users").document(userId)
                                .set(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Twitter login successful!", Toast.LENGTH_SHORT).show()
                                    preferences.edit().putString("userUid", userId).apply()
                                    preferences.edit().putBoolean("isLoggedIn", true).apply()
                                    userTypeChecker.checkUserTypeAndNavigate(email)
                                }
                                .addOnFailureListener {
                                    stopCircleLoading()
                                    Toast.makeText(
                                        this,
                                        "Twitter sign-in failed. Please try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }
            }
            .addOnFailureListener{
                Toast.makeText(
                    this,
                    "Twitter sign-in failed. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
                stopCircleLoading()
            }
            .addOnCanceledListener {
                Toast.makeText(this, "Twitter Sign-in Canceled!", Toast.LENGTH_SHORT).show()
                stopCircleLoading()
            }
        stopCircleLoading()
    }


    private fun showCircleLoading(){
        frameLayout.visibility = View.VISIBLE
        videoView.setOnPreparedListener {
            it.isLooping = true
        }
        val videoPath = "android.resource://" + packageName + "/" + R.raw.circle_loading

        videoView.setVideoURI(Uri.parse(videoPath))
        videoView.setZOrderOnTop(true)

        videoView.start()
    }
    private fun stopCircleLoading(){
        frameLayout.visibility = View.GONE
        videoView.stopPlayback()
    }
}