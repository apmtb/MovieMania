package com.example.moviemania.user

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.moviemania.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.santalu.maskara.widget.MaskEditText
import java.text.SimpleDateFormat
import java.util.Date

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
    private lateinit var creditCardText: MaskEditText
    private lateinit var upiEditText: EditText
    private lateinit var videoView: VideoView
    private lateinit var frameLayout: View
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


        videoView = findViewById(R.id.videoViewLoadingCirclePayment)
        frameLayout = findViewById(R.id.frameLayoutPayment)

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

        creditCardText = findViewById(R.id.creditCardNumber)

        upiEditText = findViewById(R.id.upiIdEditText)

        val paymentBTN = findViewById<Button>(R.id.paymentBTN)
        paymentBTN.text = "Pay $formattedTotal"
        paymentBTN.setOnClickListener {
            val icon = ContextCompat.getDrawable(this, R.drawable.ic_custom_error)
            icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
            if (paymentOption.equals("UPI Id")) {
                val upiText = upiEditText.text.toString()
                if (upiText.isEmpty()) {
                    upiEditText.setError("UPI Id is Required!",icon)
                    return@setOnClickListener
                } else {
                    if(!isValidUpiId(upiText)){
                        upiEditText.setError("Invalid UPI Id!",icon)
                        return@setOnClickListener
                    }
                }
            }
            if (paymentOption.equals("Credit Card")) {
                val expiry = findViewById<MaskEditText>(R.id.expiryDate)
                val cvv = findViewById<MaskEditText>(R.id.credidCardCvv)
                val name = findViewById<EditText>(R.id.holderName)
                if (!creditCardText.isDone) {
                    creditCardText.setError("Enter valid Credit Card Number!",icon)
                    return@setOnClickListener
                } else if (name.text.toString().trim().isEmpty() || name.text.toString().trim().length < 3) {
                    name.setError("Enter valid Name!",icon)
                    return@setOnClickListener
                } else if (!expiry.isDone) {
                    expiry.setError("Enter valid Expiry Date!",icon)
                    return@setOnClickListener
                } else if (!cvv.isDone){
                    cvv.setError("Enter valid CVV!",icon)
                    return@setOnClickListener
                }
            }
            showCircleLoading()
            val auth = FirebaseAuth.getInstance()
            val currentUserEmail = auth.currentUser?.email

            updateSeatStatus(theaterId!!, movieTitle!!, movieTime!!, selectedSeatsArray!!.toList(),
                true
            ) { status ->
                if (status) {
                    val db = Firebase.firestore
                    val bookingsCollection = db.collection("Bookings")

                    val selectedSeats = mutableListOf<String>()
                    selectedSeatsArray.sort()

                    for (seat in selectedSeatsArray) {
                        val formattedNumber = String.format("%02d", (seat + 1))
                        selectedSeats.add("S$formattedNumber")
                    }

                    val formatter = SimpleDateFormat("dd/MM/yyyy")
                    val date = Date()
                    val currentDate = formatter.format(date)

                    val newBookingDoc = bookingsCollection.document()

                    val theaterRef = db.collection("Theaters").document(theaterId)
                    theaterRef.get()
                        .addOnSuccessListener {
                            newBookingDoc.set(
                                mapOf(
                                    "transactionId" to newBookingDoc.id,
                                    "currentUserEmail" to currentUserEmail,
                                    "movieTitle" to movieTitle,
                                    "movieImageUrl" to movieImageUrl,
                                    "bookedOn" to currentDate,
                                    "date" to selectedDate,
                                    "time" to movieTime,
                                    "language" to movieLanguage,
                                    "theaterName" to it.getString("name"),
                                    "location" to it.getString("location"),
                                    "bookedSeats" to selectedSeats.joinToString(", "),
                                    "method" to paymentOption,
                                    "price" to price,
                                    "tax" to formattedTax,
                                    "totalPrice" to formattedTotal
                                )
                            ).addOnSuccessListener {
                                showToast("Payment Successful!")
                            }.addOnFailureListener { ex ->
                                showToast("Error : $ex")
                            }
                        }
                        .addOnFailureListener {
                            showToast("Error : $it")
                        }
                    val intent = Intent(this, PaymentSuccessActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("transactionId", newBookingDoc.id)
                    intent.putExtra("movieTitle", movieTitle)
                    intent.putExtra("movieImageUrl", movieImageUrl)
                    intent.putExtra("theaterId", theaterId)
                    intent.putExtra("date", selectedDate)
                    intent.putExtra("movieTime", movieTime)
                    intent.putExtra("language", movieLanguage)
                    intent.putExtra("method", paymentOption)
                    intent.putExtra("selectedSeats", selectedSeats.joinToString(", "))
                    intent.putExtra("price", price)
                    intent.putExtra("taxes",formattedTax)
                    intent.putExtra("total", formattedTotal)
                    startActivity(intent)
                    finish()
                } else {
                    showToast("Payment failed, Please try again Later!")
                }
                stopCircleLoading()
            }
        }
    }

    private fun isValidUpiId(upiId: String): Boolean {
        val regexPattern = "^[0-9A-Za-z.-]{2,256}@[A-Za-z]{2,64}\$"
        val regex = Regex(regexPattern)
        return regex.matches(upiId)
    }

    private val cardClickListener = View.OnClickListener { view ->

        unselectAllCardViews()

        val creditCardView = findViewById<CardView>(R.id.creditCardView)
        creditCardView.visibility = View.GONE

        val upiIdView = findViewById<CardView>(R.id.upiIdView)
        upiIdView.visibility = View.GONE

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
                upiIdView.visibility = View.VISIBLE
            }
            payOption5 -> {
                radio5.isChecked = true
                radio5.buttonTintList = checkColor
                paymentOption = "Credit Card"
                creditCardView.visibility = View.VISIBLE
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

    private fun showCircleLoading(){
        frameLayout.visibility = View.VISIBLE
        videoView.setOnPreparedListener {
            it.isLooping = true
        }
        val videoPath = "android.resource://" + packageName + "/" + R.raw.circle_loading

        videoView.setVideoURI(Uri.parse(videoPath))
        videoView.setZOrderOnTop(true)

        videoView.start()
    }
    private fun stopCircleLoading(){
        frameLayout.visibility = View.GONE
        videoView.stopPlayback()
    }
}