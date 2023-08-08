package com.example.moviemania

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.example.moviemania.admin.AdminActivity
import com.example.moviemania.user.UserActivity
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
        val intent = Intent(activity, AdminActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
    private fun startUserActivity() {
        val intent = Intent(activity, UserActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}