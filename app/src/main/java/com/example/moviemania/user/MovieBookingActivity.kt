package com.example.moviemania.user

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
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

class MovieBookingActivity : AppCompatActivity() {
    private lateinit var textSelectedDate: TextView
    private var selectedLanguage: String? = null
    private var selectedTime: String? = null
    private var selectedDate: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_movie_booking)

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

        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
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

        val timesSpinner = findViewById<MySpinner>(R.id.timesSpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, times!!)
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