package com.example.moviemania.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
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

        val movieTitle = intent.getStringExtra("movieTitle")
        val movieImageUrl = intent.getStringExtra("movieImageUrl")
        val theaterId = intent.getStringExtra("theaterId")
        val selectedDate = intent.getStringExtra("date")
        val movieTime = intent.getStringExtra("movieTime")
        val selectedSeatsArray = intent.getIntegerArrayListExtra("selectedSeatsList")
        val price = intent.getStringExtra("price")
        val formattedTax = intent.getStringExtra("taxes")
        val formattedTotal = intent.getStringExtra("total")

        val paymentBTN = findViewById<Button>(R.id.paymentBTN)
        paymentBTN.text = "Pay $formattedTotal"
        paymentBTN.setOnClickListener {
            val intent = Intent(this, ReceiptActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("movieTitle", movieTitle)
            intent.putExtra("movieImageUrl", movieImageUrl)
            intent.putExtra("theaterId", theaterId)
            intent.putExtra("date", selectedDate)
            intent.putExtra("movieTime", movieTime)
            intent.putIntegerArrayListExtra("selectedSeatsList", selectedSeatsArray)
            intent.putExtra("price", "Rs. $price")
            intent.putExtra("taxes", "Rs. $formattedTax")
            intent.putExtra("total", "Rs. $formattedTotal")
            startActivity(intent)
            finish()
        }
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

    private fun showToast(message: String){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
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