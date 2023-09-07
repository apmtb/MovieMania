package com.example.moviemania.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.example.moviemania.R
import com.google.firebase.firestore.FirebaseFirestore

class PaymentActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_payment)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = "Secure Payment"
        actionBar?.setDisplayHomeAsUpEnabled(true)


//                    updateSeatStatus(theaterId, movieTitle, movieTime, storageSeatPositionsList,
//                        true
//                    ) { status ->
//                        if (status) {
//                            Toast.makeText(this, "Booked Successfully!", Toast.LENGTH_SHORT).show()
//                        } else {
//                            Toast.makeText(
//                                this,
//                                "Booking failed, Please try again later!",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }

    }

    private fun updateSeatStatus(theaterId: String, movieTitle: String, movieTime: String, seatPositions: List<Int>, isSelected: Boolean, callback: (Boolean) -> Unit) {
        val theaterRef = db.collection("Theaters").document(theaterId)
        val movieSubCollection = theaterRef.collection(movieTitle)
        val seatsDocument = movieSubCollection.document(movieTime)
        seatsDocument.get().addOnSuccessListener {
            val seats = it.get("seats") as? MutableList<Boolean>
            seats?.let { seatsList ->
                for (seatPosition in seatPositions) {
                    if (seatPosition >= 0 && seatPosition < seatsList.size) {
                        seatsList[seatPosition] = isSelected
                    }
                }

                seatsDocument.update("seats", seatsList)
                    .addOnSuccessListener {
                        // Successfully updated
                        callback(true)
                    }
                    .addOnFailureListener { e ->
                        // Handle error
                        callback(false)
                    }
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}