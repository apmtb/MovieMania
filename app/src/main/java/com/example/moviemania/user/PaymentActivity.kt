package com.example.moviemania.user

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.moviemania.R
import com.google.firebase.firestore.FirebaseFirestore

class PaymentActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var radio1: RadioButton
    private lateinit var radio2: RadioButton
    private lateinit var radio3: RadioButton
    private lateinit var radio4: RadioButton
    private lateinit var radio5: RadioButton
    private lateinit var payOption1: CardView
    private lateinit var payOption2: CardView
    private lateinit var payOption3: CardView
    private lateinit var payOption4: CardView
    private lateinit var payOption5: CardView
    private var paymentOption: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_payment)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = "Payment Options"
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

        radio1 = findViewById(R.id.btn_radio1)
        radio2 = findViewById(R.id.btn_radio2)
        radio3 = findViewById(R.id.btn_radio3)
        radio4 = findViewById(R.id.btn_radio4)
        radio5 = findViewById(R.id.btn_radio5)

        radio1.isClickable = false
        radio2.isClickable = false
        radio3.isClickable = false
        radio4.isClickable = false
        radio5.isClickable = false

        payOption1 = findViewById(R.id.pay_type1)
        payOption2 = findViewById(R.id.pay_type2)
        payOption3 = findViewById(R.id.pay_type3)
        payOption4 = findViewById(R.id.pay_type4)
        payOption5 = findViewById(R.id.pay_type5)

        payOption1.setOnClickListener(cardClickListener)
        payOption2.setOnClickListener(cardClickListener)
        payOption3.setOnClickListener(cardClickListener)
        payOption4.setOnClickListener(cardClickListener)
        payOption5.setOnClickListener(cardClickListener)

        val paymentBTN = findViewById<Button>(R.id.paymentBTN)
        paymentBTN.text = "Pay $formattedTotal"
        paymentBTN.setOnClickListener {
            val intent = Intent(this, PaymentSuccessActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

    private val cardClickListener = View.OnClickListener { view ->

        unselectAllCardViews()

        view.isSelected = true

        when (view) {
            payOption1 -> payOption1.setCardBackgroundColor(ContextCompat.getColor(this, R.color.rippleColor))
            payOption2 -> payOption2.setCardBackgroundColor(ContextCompat.getColor(this, R.color.rippleColor))
            payOption3 -> payOption3.setCardBackgroundColor(ContextCompat.getColor(this, R.color.rippleColor))
            payOption4 -> payOption4.setCardBackgroundColor(ContextCompat.getColor(this, R.color.rippleColor))
            payOption5 -> payOption5.setCardBackgroundColor(ContextCompat.getColor(this, R.color.rippleColor))
        }

        val checkColor = ContextCompat.getColorStateList(this, R.color.blue)

        when (view) {
            payOption1 -> {
                radio1.isChecked = true
                radio1.buttonTintList = checkColor
                paymentOption = "Google Pay"
            }
            payOption2 -> {
                radio2.isChecked = true
                radio2.buttonTintList = checkColor
                paymentOption = "PhonePe"
            }
            payOption3 -> {
                radio3.isChecked = true
                radio3.buttonTintList = checkColor
                paymentOption = "Paytm"
            }
            payOption4 -> {
                radio4.isChecked = true
                radio4.buttonTintList = checkColor
                paymentOption = "UPI Id"
            }
            payOption5 -> {
                radio5.isChecked = true
                radio5.buttonTintList = checkColor
                paymentOption = "Credit Card"
            }
        }
    }

    private fun unselectAllCardViews() {
        payOption1.isSelected = false
        payOption2.isSelected = false
        payOption3.isSelected = false
        payOption4.isSelected = false
        payOption5.isSelected = false

        payOption1.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        payOption2.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        payOption3.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        payOption4.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        payOption5.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))

        val unCheckColor = ColorStateList.valueOf(Color.parseColor("#808080"))

        radio1.isChecked = false
        radio2.isChecked = false
        radio3.isChecked = false
        radio4.isChecked = false
        radio5.isChecked = false


        radio1.buttonTintList = unCheckColor
        radio2.buttonTintList = unCheckColor
        radio3.buttonTintList = unCheckColor
        radio4.buttonTintList = unCheckColor
        radio5.buttonTintList = unCheckColor
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