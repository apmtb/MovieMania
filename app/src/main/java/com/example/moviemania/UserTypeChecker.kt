package com.example.moviemania

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class UserTypeChecker(private val activity: Activity) {
    private val db = FirebaseFirestore.getInstance()
    private val adminUsersRef = db.collection("AdminUsers")

    fun checkUserTypeAndNavigate(userEmail: String) {
        adminUsersRef.whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    startAdminActivity()
                } else {
                    startUserActivity()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(activity,"Error : ${exception.message}" ,Toast.LENGTH_SHORT).show()
            }
    }
    private fun startAdminActivity() {
        val intent = Intent(activity, ShowProfile::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
    private fun startUserActivity() {
        val intent = Intent(activity, UserActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}