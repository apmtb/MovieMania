package com.example.moviemania

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class UserTypeChecker(private val context: Context) {
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
                Toast.makeText(context,"Error : ${exception.message}" ,Toast.LENGTH_SHORT).show()
            }
    }
    private fun startAdminActivity() {
        val intent = Intent(context, ShowProfile::class.java)
        context.startActivity(intent)
    }
    private fun startUserActivity() {
        val intent = Intent(context, UserActivity::class.java)
        context.startActivity(intent)
    }
}