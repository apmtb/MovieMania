package com.example.moviemania.user

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.moviemania.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.util.Calendar

class MovieBookingActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var textSelectedDate: TextView
    private var selectedLanguage: String? = null
    private var selectedTime: String? = null
    private var selectedDate: String? = null
    private var isToday = true
    private lateinit var selectDateError: TextView
    private lateinit var selectTimeError: TextView
    private lateinit var selectLanguageError: TextView
    private val selectedSeatPositionsList: MutableList<Int> = mutableListOf()
    private val storageSeatPositionsList: MutableList<Int> = mutableListOf()
    private val tempSeatPositionsList: MutableList<Int> = mutableListOf()
    private val tempStorageSeatPositionsList: MutableList<Int> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_movie_booking)

        val theaterList = ArrayList<Theater>()

        val theaters = intent.getStringArrayListExtra("theaters")

        if (theaters!=null){
            for (theaterId in theaters){
                val theaterRef = db.collection("Theaters").document(theaterId)
                theaterRef.get().addOnSuccessListener {
                    val theaterName = it.getString("name")
                    val imageUrl = it.getString("imageUri")
                    val location = it.getString("location")
                    val seatColNum = it.getString("seatColnum")?.toIntOrNull() ?: 0
                    val seatRowNum = it.getString("seatRownum")?.toIntOrNull() ?: 0
                    val initialSeatState = MutableList(seatColNum * seatRowNum) { false }
                    if (theaterName != null && location != null && imageUrl != null) {
                        val theater = Theater(theaterId, theaterName, Uri.parse(imageUrl).toString(),
                            location, seatColNum, seatRowNum, initialSeatState)
                        theaterList.add(theater)
                    }
                }
            }
        }

        selectDateError = findViewById(R.id.movieSelectDateError)
        selectTimeError = findViewById(R.id.movieSelectTimesError)
        selectLanguageError = findViewById(R.id.movieSelectLanguageError)

        val movieTitle = intent.getStringExtra("movieTitle")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = "Book $movieTitle"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        textSelectedDate = findViewById(R.id.dateSpinner)
        textSelectedDate.setOnClickListener{
            showDatePickerDialog()
        }

        val times = intent.getStringArrayListExtra("times")
        val languages = intent.getStringArrayListExtra("languages")

        val languagesSpinner = findViewById<MySpinner>(R.id.languagesSpinner)
        val languagesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages!!)
        languagesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languagesSpinner.adapter = languagesAdapter
        languagesSpinner.setSelection(Adapter.NO_SELECTION, true)
        languagesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                val languagesTextView = findViewById<TextView>(R.id.languagesTextView)
                languagesTextView.visibility = View.GONE
                selectLanguageError.visibility = View.GONE
                selectedLanguage = parentView.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        languagesSpinner.setOnItemSelectedEvenIfUnchangedListener(languagesSpinner.onItemSelectedListener)

        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY) // Current hour in 24-hour format
        val currentMinute = currentTime.get(Calendar.MINUTE)
        val timesSpinner = findViewById<MySpinner>(R.id.timesSpinner)

        val timesTextView = findViewById<TextView>(R.id.timesTextView)
        timesTextView.setOnClickListener {
            if (validateDate()){
                timesSpinner.performClick()
            }
        }

        val adapter = object: ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, times!!){
            // Disable click for past Times ( time < current time )
            override fun isEnabled(position: Int): Boolean {
                val itemTime = times!![position]
                val itemHour = itemTime.substringBefore(":").trim().toInt()
                val itemMinute = itemTime.substringAfter(":").substringBefore(" ").trim().toInt()
                val amPm = itemTime.substringAfterLast(" ").trim()
                val itemHour24 = if (amPm.equals("AM", ignoreCase = true)) {
                    if (itemHour == 12) 0 else itemHour
                } else {
                    if (itemHour == 12) itemHour else itemHour + 12
                }
                return !(itemHour24 < currentHour || (itemHour24 == currentHour && itemMinute < currentMinute)) || !isToday
            }

            // Change color item
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val mView = super.getDropDownView(position, convertView, parent)
                val mTextView = mView as? TextView

                val itemTime = times!![position]
                val itemHour = itemTime.substringBefore(":").trim().toInt()
                val itemMinute = itemTime.substringAfter(":").substringBefore(" ").trim().toInt()
                val amPm = itemTime.substringAfterLast(" ").trim()
                val itemHour24 = if (amPm.equals("AM", ignoreCase = true)) {
                    if (itemHour == 12) 0 else itemHour
                } else {
                    if (itemHour == 12) itemHour else itemHour + 12
                }

                if (!(itemHour24 < currentHour || (itemHour24 == currentHour && itemMinute < currentMinute)) || !isToday) {
                    mTextView?.setTextColor(Color.BLACK)
                } else {
                    mTextView?.setTextColor(Color.GRAY)
                }
                return mView
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timesSpinner.adapter = adapter
        timesSpinner.setSelection(Adapter.NO_SELECTION, true)
        timesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                val timesTextView = findViewById<TextView>(R.id.timesTextView)
                timesTextView.visibility = View.GONE
                selectTimeError.visibility = View.GONE
                clearSeatSelection()
                selectedTime = parentView.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        timesSpinner.setOnItemSelectedEvenIfUnchangedListener(timesSpinner.onItemSelectedListener)

        val theatersTextView = findViewById<TextView>(R.id.theatersSpinnerTextView)
        theatersTextView.setOnClickListener {
            if (validateDate() && validateTime()) {
                val adapter = TheaterAdapter(this, theaterList)

                val dialog = AlertDialog.Builder(this)
                    .setTitle("Select a Theater")
                    .setSingleChoiceItems(adapter, -1) { dialog, which ->
                        val selectedTheater = theaterList[which]
                        val selectedTheaterName = selectedTheater.name
                        theatersTextView.text = selectedTheaterName

                        val theaterImageView = findViewById<ImageView>(R.id.theaterImageView)

                        val theaterNameTextView = findViewById<TextView>(R.id.theaterNameTextView)
                        theaterNameTextView.text = selectedTheaterName

                        val theaterLocationTextView =
                            findViewById<TextView>(R.id.theaterLocationTextView)
                        theaterLocationTextView.text = selectedTheater.theaterLocation

                        val imageViewLayoutParams = theaterImageView.layoutParams
                        val displayMetrics = this.resources.displayMetrics

                        val screenHeight = displayMetrics.heightPixels
                        val screenWidth = displayMetrics.widthPixels
                        imageViewLayoutParams.width = (screenWidth * 0.85).toInt()
                        imageViewLayoutParams.height = (screenHeight * 0.20).toInt()

                        Glide.with(this)
                            .load(selectedTheater.imageUri)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_custom_error)
                            .centerCrop()
                            .into(theaterImageView)

                        val selectedTheaterLayout =
                            findViewById<LinearLayout>(R.id.selectedTheaterView)
                        selectedTheaterLayout.visibility = View.VISIBLE

                        clearSeatSelection()

                        val selectSeatButton = findViewById<Button>(R.id.selectSeatBTN)
                        selectSeatButton.setOnClickListener {
                            if (validateTime()) {
                                val theaterRef =
                                    db.collection("Theaters").document(selectedTheater.id)
                                if (movieTitle != null) {
                                    val movieSubcollection = theaterRef.collection(movieTitle)
                                    val seatsDocument = movieSubcollection.document(selectedTime!!)
                                    seatsDocument.get().addOnSuccessListener {
                                        val seats = it.get("seats")
                                        showSeatSelectionDialog(
                                            selectedTheater.id,
                                            movieTitle,
                                            selectedTime!!,
                                            selectedTheater.seatRowLength,
                                            selectedTheater.seatColLength
                                        )
                                    }
                                }
                            }
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
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

    private fun clearSeatSelection(){
        selectedSeatPositionsList.clear()
        storageSeatPositionsList.clear()
        tempSeatPositionsList.clear()
        tempStorageSeatPositionsList.clear()
        val receiptView = findViewById<RelativeLayout>(R.id.receiptView)
        receiptView.visibility = View.GONE
    }

    private fun validateDate(): Boolean {
        if (selectedDate==null){
            selectDateError.visibility = View.VISIBLE
            return false
        }
        return true
    }
    private fun validateTime(): Boolean {
        if (selectedTime == null) {
            selectTimeError.visibility = View.VISIBLE
            return false
        }
        return true
    }

    private fun validateLanguage(): Boolean {
        if (selectedLanguage == null) {
            selectLanguageError.visibility = View.VISIBLE
            return false
        }
        return true
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val formattedDay = String.format("%02d", selectedDay)
                val formattedMonth = String.format("%02d", selectedMonth + 1)
                selectDateError.visibility = View.GONE
                isToday = day==selectedDay
                val timesTextView = findViewById<TextView>(R.id.timesTextView)
                timesTextView.visibility = View.VISIBLE
                selectedTime = null
                clearSeatSelection()
                textSelectedDate.text = "$formattedDay/$formattedMonth/$selectedYear"
                selectedDate = "$formattedDay/$formattedMonth/$selectedYear"
            },
            year, month, day
        )
        if (currentHour >= 23) {
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() + ( 24 * 60 * 60 * 1000 )
        } else {
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        }
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
        datePickerDialog.show()
    }
    private fun showToast(message: String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    private fun showSeatSelectionDialog(theaterId: String, movieTitle: String, movieTime: String, seatRowLength: Int, seatColLength: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_seat_selection, null)
        val seatGridView: GridView = dialogView.findViewById(R.id.seatGridView)

        seatGridView.numColumns = seatColLength + 1

        val movieImageUrl = intent.getStringExtra("movieImageUrl")
        val ticketPriceString = intent.getStringExtra("ticketPrice")
        val ticketPrice = ticketPriceString!!.toDouble()

        val initialSeatPositions = ArrayList(selectedSeatPositionsList)
        val initialStorageSeatPositions = ArrayList(storageSeatPositionsList)

        val seatDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Select Seats")
            .setCancelable(false)
            .setPositiveButton("Select", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                tempSeatPositionsList.clear()
                tempStorageSeatPositionsList.clear()
                selectedSeatPositionsList.clear()
                selectedSeatPositionsList.addAll(initialSeatPositions)
                tempSeatPositionsList.addAll(initialSeatPositions)
                tempStorageSeatPositionsList.addAll(initialStorageSeatPositions)
                storageSeatPositionsList.clear()
                storageSeatPositionsList.addAll(initialStorageSeatPositions)
                dialog.dismiss()
            }
            .create()

        loadSeatData(theaterId, movieTitle, movieTime, seatColLength) { seatList ->
            val seatAdapter = SeatAdapter(
                this,
                selectedSeatPositionsList,
                storageSeatPositionsList,
                seatList,
                seatRowLength,
                seatColLength + 1
            ) { list, gridPositions ->
                tempSeatPositionsList.clear()
                tempSeatPositionsList.addAll(gridPositions)
                tempStorageSeatPositionsList.clear()
                tempStorageSeatPositionsList.addAll(list)
            }
            seatGridView.adapter = seatAdapter
        }

        seatDialog.show()
        val selectButton = seatDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        selectButton.setOnClickListener {
            val receiptView = findViewById<RelativeLayout>(R.id.receiptView)
            if (tempSeatPositionsList.isNotEmpty()) {
                selectedSeatPositionsList.clear()
                selectedSeatPositionsList.addAll(tempSeatPositionsList)
                storageSeatPositionsList.clear()
                storageSeatPositionsList.addAll(tempStorageSeatPositionsList)
                receiptView.visibility = View.VISIBLE
                val decimalFormat = DecimalFormat("0.#")
                val selectedSeatsCount = tempSeatPositionsList.size
                val seatCount = findViewById<TextView>(R.id.seatCount)
                seatCount.text = "$selectedSeatsCount x Seats :"
                val seatCountPrice = findViewById<TextView>(R.id.priceSeatCount)
                val price = decimalFormat.format(ticketPrice * selectedSeatsCount)
                seatCountPrice.text = "Rs. $price"
                val taxesCharge = findViewById<TextView>(R.id.taxesCharge)
                val tax = selectedSeatsCount * ticketPrice * 0.05
                val formattedTax = decimalFormat.format(tax)
                taxesCharge.text = "Rs. $formattedTax"

                val total = ticketPrice * selectedSeatsCount + tax
                val formattedTotal = decimalFormat.format(total)
                val totalAmount = findViewById<TextView>(R.id.totalAmount)
                totalAmount.text = "Rs. $formattedTotal"

                val payBtn = findViewById<Button>(R.id.paymentBTN)

                payBtn.setOnClickListener {
                    if (validateLanguage()) {
                        val intent = Intent(this, PaymentActivity::class.java)
                        intent.putExtra("movieTitle", movieTitle)
                        intent.putExtra("movieImageUrl", movieImageUrl)
                        intent.putExtra("theaterId", theaterId)
                        intent.putExtra("date", selectedDate)
                        intent.putExtra("movieTime", movieTime)
                        val selectedSeatsArray = ArrayList<Int>()
                        selectedSeatsArray.addAll(storageSeatPositionsList)
                        intent.putIntegerArrayListExtra("selectedSeatsList", selectedSeatsArray)
                        intent.putExtra("price", "Rs. $price")
                        intent.putExtra("taxes", "Rs. $formattedTax")
                        intent.putExtra("total", "Rs. $formattedTotal")
                        startActivity(intent)
                    }
                }
            } else {
                receiptView.visibility = View.GONE
                Toast.makeText(this, "Select atleast one seat!", Toast.LENGTH_SHORT).show()
            }
            seatDialog.dismiss()
        }
    }

    private fun loadSeatData(theaterId: String, movieTitle: String, movieTime: String, numColumns: Int, callback: (List<Seat>) -> Unit) {

        val theaterRef = db.collection("Theaters").document(theaterId)
        val movieSubCollection = theaterRef.collection(movieTitle)
        val seatsDocument = movieSubCollection.document(movieTime)
        seatsDocument.get().addOnSuccessListener {
            val seats = it.get("seats") as? List<Boolean>
            val seatList = mutableListOf<Seat>()
            seats?.forEachIndexed { index, isSelected ->
                val seatId = "Seat_${index + 1}" // Adjust the seat ID creation if needed
                val row = index / numColumns + 1
                val column = index % numColumns + 1
                seatList.add(Seat(seatId, column, row, isSelected))
                if (numColumns / 2 == column) {
                    seatList.add(Seat("", column, row, isSelected))
                }
            }
            callback(seatList)
        }
    }

    data class Seat(
        val id: String,
        val column: Int,
        val row: Int,
        val isBooked: Boolean,
    )

    data class Theater(
        val id:String, val name: String, val imageUri: String, val theaterLocation: String,
        val seatColLength: Int, val seatRowLength: Int, val seatStates: List<Boolean>
    )
}

class MySpinner : androidx.appcompat.widget.AppCompatSpinner {
    private var listener: OnItemSelectedListener? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun setSelection(position: Int) {
        super.setSelection(position)
        listener?.onItemSelected(this, selectedView, position, selectedItemId)
    }

    fun setOnItemSelectedEvenIfUnchangedListener(listener: OnItemSelectedListener?) {
        this.listener = listener
    }
}