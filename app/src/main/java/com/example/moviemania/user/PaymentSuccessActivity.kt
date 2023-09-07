package com.example.moviemania.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.moviemania.R

class PaymentSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_payment_success)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = "Payment Success"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val movieTitle = intent.getStringExtra("movieTitle")
        val movieImageUrl = intent.getStringExtra("movieImageUrl")
        val theaterId = intent.getStringExtra("theaterId")
        val selectedDate = intent.getStringExtra("date")
        val movieTime = intent.getStringExtra("movieTime")
        val movieLanguage = intent.getStringExtra("language")
        val selectedSeatsArray = intent.getIntegerArrayListExtra("selectedSeatsList")
        val price = intent.getStringExtra("price")
        val formattedTax = intent.getStringExtra("taxes")
        val formattedTotal = intent.getStringExtra("total")

        val amount = findViewById<TextView>(R.id.payment_amount)
        amount.text = formattedTotal
        val viewReceiptBTN = findViewById<Button>(R.id.viewReceiptBTN)
        viewReceiptBTN.setOnClickListener {
            val intent = Intent(this, ReceiptActivity::class.java)
            intent.putExtra("movieTitle", movieTitle)
            intent.putExtra("movieImageUrl", movieImageUrl)
            intent.putExtra("theaterId", theaterId)
            intent.putExtra("date", selectedDate)
            intent.putExtra("movieTime", movieTime)
            intent.putExtra("language", movieLanguage)
            intent.putIntegerArrayListExtra("selectedSeatsList", selectedSeatsArray)
            intent.putExtra("price", price)
            intent.putExtra("taxes",formattedTax)
            intent.putExtra("total", formattedTotal)
            startActivity(intent)
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