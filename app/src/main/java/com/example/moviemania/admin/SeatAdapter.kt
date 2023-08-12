package com.example.moviemania.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.moviemania.R

class SeatAdapter(
    private val context: Context,
    private val seats: List<TheaterAdapter.Seat>,
    private val numRows: Int,
    private val numColumns: Int
) : BaseAdapter() {

    override fun getCount(): Int {
        return numRows * numColumns
    }

    override fun getItem(position: Int): Any {
        return seats[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val seat = seats[position]
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.grid_item_seats, parent, false)

        val seatImageView: ImageView = view.findViewById(R.id.seatImageView)


        val imageViewLayoutParams = seatImageView.layoutParams
        val displaymetrics = context.resources.displayMetrics

        val screenHeight = displaymetrics.heightPixels
        val screenWidth = displaymetrics.widthPixels
        imageViewLayoutParams.width = (screenWidth*0.10).toInt()
        imageViewLayoutParams.height = imageViewLayoutParams.width

        if (seat.isSelected) {
            seatImageView.setImageResource(R.drawable.ic_seat_selected)
        } else {
            seatImageView.setImageResource(R.drawable.ic_seat_available)
        }

        return view
    }
}