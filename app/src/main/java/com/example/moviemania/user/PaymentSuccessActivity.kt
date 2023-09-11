package com.example.moviemania.user

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.moviemania.R
import com.google.firebase.firestore.FirebaseFirestore

class PaymentSuccessActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_payment_success)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = "Payment Success"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val transactionId = intent.getStringExtra("transactionId")
        val movieTitle = intent.getStringExtra("movieTitle")
        val movieImageUrl = intent.getStringExtra("movieImageUrl")
        val theaterId = intent.getStringExtra("theaterId")
        val selectedDate = intent.getStringExtra("date")
        val method = intent.getStringExtra("method")
        val movieTime = intent.getStringExtra("movieTime")
        val movieLanguage = intent.getStringExtra("language")
        val selectedSeats = intent.getStringExtra("selectedSeats")
        val price = intent.getStringExtra("price")
        val formattedTax = intent.getStringExtra("taxes")
        val formattedTotal = intent.getStringExtra("total")

        val amount = findViewById<TextView>(R.id.payment_amount)
        amount.text = formattedTotal

        val id = findViewById<TextView>(R.id.payment_transaction_id_TextView)
        id.text = transactionId

        val viewReceiptBTN = findViewById<Button>(R.id.viewReceiptBTN)
        viewReceiptBTN.setOnClickListener {

            if (theaterId!=null) {
                val theaterRef = db.collection("Theaters").document(theaterId)
                theaterRef.get().addOnSuccessListener {
                    val theaterName = it.getString("name")
                    val theaterImageUrl = it.getString("imageUri")
                    val theaterLocation = it.getString("location")

                    val intent = Intent(this, BookingDetailsActivity::class.java)
                    intent.putExtra("transactionId", transactionId)
                    intent.putExtra("movieTitle", movieTitle)
                    intent.putExtra("movieImageUrl", movieImageUrl)
                    intent.putExtra("theaterName", theaterName)
                    intent.putExtra("theaterLocation", theaterLocation)
                    intent.putExtra("date", selectedDate)
                    intent.putExtra("method", method)
                    intent.putExtra("movieTime", movieTime)
                    intent.putExtra("language", movieLanguage)
                    intent.putExtra("selectedSeats", selectedSeats)
                    intent.putExtra("price", price)
                    intent.putExtra("taxes",formattedTax)
                    intent.putExtra("total", formattedTotal)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, UserActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onBackPressed() {
        val intent = Intent(this, UserActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

data class Booking(
    val transactionId: String,
    val movieTitle: String,
    val movieImageUrl: String,
    val bookedOn: String,
    val date: String,
    val time: String,
    val language: String,
    val theaterName: String,
    val location: String,
    val bookedSeats: String,
    val method: String,
    val price: String,
    val tax: String,
    val totalPrice: String
)