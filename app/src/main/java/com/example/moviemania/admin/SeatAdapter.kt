package com.example.moviemania.admin

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
    private val seats: List<TheaterAdapter.Seat>,
    private val numRows: Int,
    private val numColumns: Int,
    private val onSeatClick: (MutableList<Int>) -> Unit
) : BaseAdapter() {

    private val selectedSeatPositions: MutableList<Int> = mutableListOf()
    private val SeatPositions: MutableList<Int> = mutableListOf()

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

        if (seat.isSelected) {
            seatImageView.setImageResource(R.drawable.ic_seat_booked)
        } else {
            seatImageView.setImageResource(R.drawable.ic_seat_available)
        }

        val column = position % numColumns + 1

        if(seat.id.isEmpty()){
            seatImageView.setImageResource(0)
        } else {
            view.setOnClickListener {
                if (seat.isSelected) {
                    seatImageView.setImageResource(R.drawable.ic_seat_booked)
                } else if (!SeatPositions.contains(position)) {
                    if((numColumns+1)/2 > column){
                        selectedSeatPositions.add(position-seat.row+1)
                        SeatPositions.add(position)
                    } else {
                        selectedSeatPositions.add(position-seat.row)
                        SeatPositions.add(position)
                    }
                    seatImageView.setImageResource(R.drawable.ic_seat_selected)
                    seatImageView.background =
                        ContextCompat.getDrawable(context, R.drawable.image_border)
                } else {
                    if((numColumns+1)/2 > column){
                        selectedSeatPositions.remove((position-seat.row+1))
                        SeatPositions.remove(position)
                    } else {
                        selectedSeatPositions.remove((position-seat.row))
                        SeatPositions.remove(position)
                    }
                    seatImageView.background = null
                    seatImageView.setImageResource(R.drawable.ic_seat_available)
                }
                onSeatClick(selectedSeatPositions)
            }
        }
        return view
    }
}

//val newSelectedState = !seat.isSelected
//onSeatClick(position, newSelectedState)