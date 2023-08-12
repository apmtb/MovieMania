package com.example.moviemania.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.moviemania.R
import com.example.moviemania.admin.bottom_fragment.TheatersFragment

class TheaterAdapter(private val context: Context, private val theaters: List<TheatersFragment.Theater>) : BaseAdapter() {

    override fun getCount(): Int {
        return theaters.size
    }

    override fun getItem(position: Int): Any {
        return theaters[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val theater = theaters[position]
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.grid_item_theater, parent, false)

        val theaterImageView = view.findViewById<ImageView>(R.id.theaterImageView)
        val theaterNameTextView = view.findViewById<TextView>(R.id.theaterNameTextView)

        theaterNameTextView.text = theater.name

        val aspectRatio = 16.0 / 9.0
        val imageViewLayoutParams = theaterImageView.layoutParams
        imageViewLayoutParams.height = (imageViewLayoutParams.width / aspectRatio).toInt()

        Glide.with(context)
            .load(theater.imageUri)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_custom_error)
            .centerCrop()
            .into(theaterImageView)

        view.setOnClickListener {
            showTheaterDetailsDialog(theater)
        }

        return view
    }
    private fun showTheaterDetailsDialog(theater: TheatersFragment.Theater) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_theater_details, null)

        val theaterImageView: ImageView = dialogView.findViewById(R.id.theaterDetailsImageView)
        val theaterNameTextView: TextView = dialogView.findViewById(R.id.theaterDetailsNameTextView)
        val theaterLocationTextView: TextView = dialogView.findViewById(R.id.theaterDetailsLocationTextView)
        val selectSeatsButton: Button = dialogView.findViewById(R.id.selectSeatsButton)

        Glide.with(context)
            .load(theater.imageUri)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_custom_error)
            .centerCrop()
            .into(theaterImageView)

        theaterNameTextView.text = theater.name
        theaterLocationTextView.text = theater.theaterLocation

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Theater Details")
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        selectSeatsButton.setOnClickListener {
            showSeatSelectionDialog(theater)
        }

        dialog.show()
    }

    private fun showSeatSelectionDialog(theater: TheatersFragment.Theater) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_seat_selection, null)
        val seatGridView: GridView = dialogView.findViewById(R.id.seatGridView)
        val closeButton: Button = dialogView.findViewById(R.id.closeButton)

        // Calculate numRows and numColumns based on theater's seatRowLength and seatColumnLength
        val numRows = theater.seatRowLength
        val numColumns = theater.seatColLength

        val seatList = ArrayList<Seat>()

        // Populate seatList with seat data

        val seatAdapter = SeatAdapter(context, seatList, numRows, numColumns)
        seatGridView.adapter = seatAdapter

        val seatDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Select Seats")
            .create()

        closeButton.setOnClickListener {
            seatDialog.dismiss()
        }

        seatDialog.show()
    }

    data class Seat(
        val row: Int,
        val column: Int,
        val isSelected: Boolean
    )
}