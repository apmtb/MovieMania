package com.example.moviemania.user

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.moviemania.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.SimpleTimeZone

class MovieBookingActivity : AppCompatActivity() {
    private lateinit var textSelectedDate: TextView
    private var selectedLanguage: String? = null
    private var selectedTime: String? = null
    private var selectedDate: String? = null
    private var isToday = true
    private lateinit var selectDateError: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_movie_booking)

        selectDateError = findViewById(R.id.movieSelectDateError)

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
            if (selectedDate==null){
                selectDateError.visibility = View.VISIBLE
            } else {
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
                selectedTime = parentView.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
        timesSpinner.setOnItemSelectedEvenIfUnchangedListener(timesSpinner.onItemSelectedListener)
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