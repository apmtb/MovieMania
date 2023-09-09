package com.example.moviemania.user

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.moviemania.R

class ReceiptActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_receipt)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = "Receipt"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val movieTitle = intent.getStringExtra("movieTitle")
        val movieImageUrl = intent.getStringExtra("movieImageUrl")
        val movieLanguage = intent.getStringExtra("language")
        val theaterName = intent.getStringExtra("theaterName")
        val theaterLocation = intent.getStringExtra("theaterLocation")
        val selectedDate = intent.getStringExtra("date")
        val transactionId = intent.getStringExtra("transactionId")
        val method = intent.getStringExtra("method")
        val movieTime = intent.getStringExtra("movieTime")
        val selectedSeats = intent.getStringExtra("selectedSeats")
        val price = intent.getStringExtra("price")
        val formattedTax = intent.getStringExtra("taxes")
        val formattedTotal = intent.getStringExtra("total")

        val title = findViewById<TextView>(R.id.receiptMovieTitleTextView)
        title.text = movieTitle

        val movieImageView = findViewById<ImageView>(R.id.receiptMovieImageView)
        val imageViewLayoutParams = movieImageView.layoutParams
        val displayMetrics = this.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels
        imageViewLayoutParams.width = (screenWidth * 0.45).toInt()
        imageViewLayoutParams.height = (screenHeight * 0.30).toInt()
        Glide.with(this).load(movieImageUrl).centerCrop().error(R.drawable.ic_custom_error)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(movieImageView)

        val date = findViewById<TextView>(R.id.receiptDateTextView)
        date.text = selectedDate

        val time = findViewById<TextView>(R.id.receiptTimesTextView)
        time.text = movieTime

        val language = findViewById<TextView>(R.id.receiptLanguageTextView)
        language.text = movieLanguage

        val tName = findViewById<TextView>(R.id.receiptTheaterNameTextView)
        tName.text = theaterName

        val tLocation = findViewById<TextView>(R.id.receiptTheaterLocationTextView)
        tLocation.text = theaterLocation

        val booked = findViewById<TextView>(R.id.receiptBookedSeatsTextView)
        booked.text = selectedSeats

        val methodText = findViewById<TextView>(R.id.receiptPaymentMethodTextView)
        methodText.text = method

        val id = findViewById<TextView>(R.id.receiptPaymentIdTextView)
        id.text = transactionId

        val selectedSeatsCount = selectedSeats?.split(", ")?.size
        val seatCount = findViewById<TextView>(R.id.receiptSeatCount)
        seatCount.text = "$selectedSeatsCount x Seats :"
        val seatCountPrice = findViewById<TextView>(R.id.receiptPriceSeatCount)
        seatCountPrice.text = price

        val taxesCharge = findViewById<TextView>(R.id.receiptTaxesCharge)
        taxesCharge.text = formattedTax

        val totalAmount = findViewById<TextView>(R.id.receiptTotalAmount)
        totalAmount.text = formattedTotal

        val home = findViewById<Button>(R.id.goBackToHomeBTN)
        home.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}