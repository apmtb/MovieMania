package com.example.moviemania.user

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.moviemania.R

class SeatAdapter(
    private val context: Context,
    private val selectedSeats: MutableList<Int>,
    private val seats: List<MovieBookingActivity.Seat>,
    private val numRows: Int,
    private val numColumns: Int,
    private val positions: (storagePositions: MutableList<Int>, gridPositions: MutableList<Int>) -> Unit
) : BaseAdapter() {

    private val selectedSeatPositions: MutableList<Int> = mutableListOf()
    private val seatPositions: MutableList<Int> = selectedSeats

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
        val displayMetrics = context.resources.displayMetrics

        val screenWidth = displayMetrics.widthPixels
        val ratio = 100/numColumns*0.01
        val tmp = ratio - (ratio*0.3)
        imageViewLayoutParams.width = (screenWidth*tmp).toInt()
        imageViewLayoutParams.height = imageViewLayoutParams.width

        if (seatPositions.contains(position)){
            seatImageView.setImageResource(R.drawable.ic_seat_selected)
            seatImageView.background = ContextCompat.getDrawable(context, R.drawable.image_border)
        } else if (seat.isBooked) {
            seatImageView.setImageResource(R.drawable.ic_seat_booked)
        } else {
            seatImageView.setImageResource(R.drawable.ic_seat_available)
        }

        val column = position % numColumns + 1

        if(seat.id.isEmpty()){
            seatImageView.setImageResource(0)
            view.isClickable = true
        } else {
            view.setOnClickListener {
                if (seat.isBooked) {
                    seatImageView.setImageResource(R.drawable.ic_seat_booked)
                } else if (!seatPositions.contains(position)) {
                    if((numColumns+1)/2 > column){
                        selectedSeatPositions.add(position-seat.row+1)
                        seatPositions.add(position)
                    } else {
                        selectedSeatPositions.add(position-seat.row)
                        seatPositions.add(position)
                    }
                    seatImageView.setImageResource(R.drawable.ic_seat_selected)
                    seatImageView.background = ContextCompat.getDrawable(context, R.drawable.image_border)
                } else {
                    if((numColumns+1)/2 > column){
                        selectedSeatPositions.remove((position-seat.row+1))
                        seatPositions.remove(position)
                    } else {
                        selectedSeatPositions.remove((position-seat.row))
                        seatPositions.remove(position)
                    }
                    seatImageView.background = null
                    val dp = 5
                    val scale = displayMetrics.density
                    val dpToPx = (dp * scale +0.5f).toInt()
                    seatImageView.setPadding(dpToPx,dpToPx,dpToPx,dpToPx)
                    seatImageView.setImageResource(R.drawable.ic_seat_available)
                }
                positions(selectedSeatPositions,seatPositions)
            }
        }
        return view
    }
}