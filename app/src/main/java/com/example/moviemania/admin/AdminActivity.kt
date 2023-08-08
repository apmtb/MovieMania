package com.example.moviemania.admin

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.moviemania.LoginActivity
import com.example.moviemania.R
import com.example.moviemania.admin.nav_fragment.HomeFragment
import com.example.moviemania.admin.nav_fragment.ProfileFragment
import com.example.moviemania.admin.nav_fragment.SettingFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class AdminActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var preferences: SharedPreferences
    private var isLoggedIn: Boolean = false
    private var isNotPlayed: Boolean = false
    private var userId = ""
    private lateinit var displayName: String
    private lateinit var email: String
    private lateinit var photoUrl: String
    private lateinit var videoView: VideoView
    private lateinit var videoView1: VideoView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private val onBackPressedCallback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            onBackPressedMethod()
        }

    }

    private fun onBackPressedMethod() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin)
        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()
        preferences = getSharedPreferences("MovieMania", MODE_PRIVATE)
        isNotPlayed = preferences.getBoolean("isNotPlayed",false)
        isLoggedIn = preferences.getBoolean("isLoggedIn",false)
        userId = preferences.getString("userUid", null) ?: ""
        navigationView = findViewById(R.id.navigationView)
        val header = navigationView.getHeaderView(0)
        val textUsername = header.findViewById<TextView>(R.id.userNameTxt)
        val textEmail = header.findViewById<TextView>(R.id.emailTxt)
        val imageView = header.findViewById<ImageView>(R.id.profileImg)
        if (isLoggedIn) {
            val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
            if(isNotPlayed) {
                videoView = findViewById(R.id.videoViewLoading)
                videoView1 = findViewById(R.id.videoViewLoading1)

                // Set the video path from the raw folder
                val videoPath = "android.resource://" + packageName + "/" + R.raw.loading
                val videoPath1 = "android.resource://" + packageName + "/" + R.raw.loading1

                videoView.setVideoURI(Uri.parse(videoPath))
                videoView1.setVideoURI(Uri.parse(videoPath1))
                // Start playing the video
                videoView.setZOrderOnTop(true)
                videoView1.setZOrderOnTop(true)

                videoView.start()
                videoView1.start()

                videoView.setOnCompletionListener {
                    drawerLayout.removeView(findViewById(R.id.videobg))
                }
            } else {
                drawerLayout.removeView(findViewById(R.id.videobg))
            }
            if (userId!="") {
                val userData = fireStore.collection("Users").document(userId)

                userData.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            displayName = document.getString("displayName") ?: ""
                            email = document.getString("email") ?: ""
                            photoUrl = document.getString("profileImageUrl") ?: ""
                            imageView.clipToOutline = true
                            // Load the user's profile image in the ImageView
                            if (photoUrl.isNotEmpty()) {
                                Picasso.get()
                                    .load(photoUrl)
                                    .error(R.drawable.default_logo) // Set an error image if loading fails
                                    .into(imageView)
                            } else {
                                Picasso.get()
                                    .load(R.drawable.default_logo)
                                    .error(R.drawable.default_logo)// Set an error image if loading fails
                                    .into(imageView)
                            }
                            textUsername.text = displayName
                            textEmail.text = email
                        } else {
                            // Null document
                            Toast.makeText(
                                this,
                                "User Not Found, Register First",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener {
                        // Error occurred while fetching user data
                        Toast.makeText(
                            this,
                            "Failed to retrieve user data. Please try again!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(this, "User Not Found Try Again With Different Account!", Toast.LENGTH_SHORT).show()
                signOut()
            }
        } else {
            signOut()
        }
        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)


        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this,drawerLayout,toolbar,R.string.open_drawer,R.string.close_drawer)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        /// Default Navigation bar Tab Selected
        replaceFragment(HomeFragment())
        navigationView.setCheckedItem(R.id.nav_home)
    }

    private fun signOut() {
        auth.signOut()
        navigateToMainActivity()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        preferences.edit().putBoolean("isLoggedIn",false).apply()
        preferences.edit().putString("userUid", "").apply()
        preferences.edit().putBoolean("isNotPlayed", true).apply()
        // Clear the back stack so that the user cannot go back to the ProfileActivity after logout
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.navFragment,fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_home -> {
                replaceFragment(HomeFragment())
            }
            R.id.nav_profile -> {
                replaceFragment(ProfileFragment())
                title = "Profile"
            }
            R.id.nav_setting -> {
                replaceFragment(SettingFragment())
                title = "Setting"
            }
            R.id.nav_share -> {
                Toast.makeText(this,"Share Clicked",Toast.LENGTH_LONG).show()
            }
            R.id.nav_logout -> {
                signOut()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
