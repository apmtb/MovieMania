package com.example.moviemania.user.nav_fragment

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.moviemania.LoginActivity
import com.example.moviemania.R
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.Timer
import java.util.UUID
import kotlin.concurrent.timerTask

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var userId = ""
    private lateinit var preferences: SharedPreferences
    private lateinit var displayName: String
    private lateinit var email: String
    private lateinit var photoUrl: String
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var textUsername: TextView
    private lateinit var imageView: ImageView
    private lateinit var textUsernameHeader: TextView
    private lateinit var imageViewHeader: ImageView
    private val db = FirebaseFirestore.getInstance()
    private lateinit var navigationView: NavigationView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.user_fragment_profile, container, false)
        preferences = requireActivity().getSharedPreferences("MovieMania", AppCompatActivity.MODE_PRIVATE)
        userId = preferences.getString("userUid", null) ?: ""
        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()
        imageView = view.findViewById(R.id.img_profile_pic)
        textUsername = view.findViewById(R.id.userNameTxt)
        val textEmail = view.findViewById<TextView>(R.id.emailTxt)

        navigationView = requireActivity().findViewById(R.id.navigationView)
        val header = navigationView.getHeaderView(0)
        textUsernameHeader = header.findViewById(R.id.userNameTxt)
        imageViewHeader = header.findViewById(R.id.profileImg)

        val cameraIcon = view.findViewById<CardView>(R.id.cameraProfile)
        cameraIcon.setOnClickListener{
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }
        val editName = view.findViewById<RelativeLayout>(R.id.editName)
        editName.setOnClickListener {
            if (userId!="") {
                val userData = fireStore.collection("Users").document(userId)
                userData.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            displayName = document.getString("displayName") ?: ""
                            showUpdateNameDialog()
                        }
                    }
            }
        }
        if (userId!="") {
            val userData = fireStore.collection("Users").document(userId)

            userData.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        displayName = document.getString("displayName") ?: ""
                        email = document.getString("email") ?: ""
                        photoUrl = document.getString("profileImageUrl") ?: ""
                        // Load the user's profile image in the ImageView
                        if (photoUrl.isNotEmpty()) {
                            Glide.with(this)
                                .load(photoUrl)
                                .error(R.drawable.default_logo)
                                .placeholder(R.drawable.ic_image_placeholder)
                                .into(imageView)
                        } else {
                            Glide.with(this)
                                .load(R.drawable.default_logo)
                                .error(R.drawable.default_logo)
                                .placeholder(R.drawable.ic_image_placeholder)
                                .into(imageView)
                        }
                        textUsername.text = displayName
                        textEmail.text = email
                    } else {
                        // Null document
                        Toast.makeText(
                            requireContext(),
                            "User Not Found, Register First",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener {
                    // Error occurred while fetching user data
                    Toast.makeText(
                        requireContext(),
                        "Failed to retrieve user data. Please try again Later!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(requireContext(), "User Not Found Try Again With Different Account!", Toast.LENGTH_SHORT).show()
            signOut()
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                val uri: Uri = data?.data!!
                Glide.with(requireContext())
                    .load(uri)
                    .error(R.drawable.default_logo)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(imageView)

                Glide.with(requireActivity())
                    .load(uri)
                    .error(R.drawable.default_logo)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(imageViewHeader)

                Timer().schedule(timerTask {
                    uploadImageToFirebaseStorage(imageView){
                        val userRef = db.collection("Users").document(userId)
                        val updateData = mutableMapOf<String, Any>()
                        updateData["profileImageUrl"] = it
                        userRef.update(updateData)
                    }
                },100)
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageToFirebaseStorage(
        imageView: ImageView,
        callback: (String) -> Unit
    ) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child("user_images/${UUID.randomUUID()}.jpg")

        val drawable = imageView.drawable
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imagesRef.putBytes(data)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imagesRef.downloadUrl.addOnCompleteListener { urlTask ->
                    if (urlTask.isSuccessful) {
                        val imageUrl = urlTask.result.toString()
                        callback(imageUrl)
                    }
                }
            }
        }
    }

    private fun showUpdateNameDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Update Name")

        // Set up the input field in the dialog
        val input = EditText(requireContext())
        input.hint = "Name"
        input.setText(displayName)
        builder.setView(input)

        builder.setPositiveButton("Update") { _, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateNameInFirestore(newName)
            } else {
                val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_custom_error)
                icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
                input.setError("Name can't be empty!",icon)
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun updateNameInFirestore(newName: String) {
        val userData = fireStore.collection("Users").document(userId)
        userData.update("displayName", newName)
            .addOnSuccessListener {
                Toast.makeText(requireContext(),"Name Updated Successfully!",Toast.LENGTH_SHORT).show()
                textUsername.text = newName
                textUsernameHeader.text = newName
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(),"Error : $e",Toast.LENGTH_SHORT).show()
            }
    }

    private fun signOut() {
        auth.signOut()
        navigateToMainActivity()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        preferences.edit().putBoolean("isLoggedIn",false).apply()
        preferences.edit().putString("userUid", "").apply()
        preferences.edit().putBoolean("isNotPlayed", true).apply()
        // Clear the back stack so that the user cannot go back to the ProfileActivity after logout
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}