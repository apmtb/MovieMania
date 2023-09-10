package com.example.moviemania.user

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.moviemania.R

class BookingAdapter(private val context: Context, private val castList: List<Booking>)  :
    BaseAdapter() {

    override fun getCount(): Int = castList.size

    override fun getItem(position: Int): Any = castList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val booking = getItem(position) as Booking
        val itemView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.grid_item_booking_history, parent, false)
        val bookingImageView = itemView.findViewById<ImageView>(R.id.bookingImage)
        val bookingTitleTextView = itemView.findViewById<TextView>(R.id.bookingTitle)
        val bookingDateTextView = itemView.findViewById<TextView>(R.id.bookingDate)
        val bookingAmountTextView = itemView.findViewById<TextView>(R.id.bookingAmount)
        bookingTitleTextView.text = booking.movieTitle
        bookingDateTextView.text = booking.date
        bookingAmountTextView.text = booking.totalPrice
        val imageViewLayoutParams = bookingImageView.layoutParams
        val displayMetrics = context.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels
        imageViewLayoutParams.width = (screenWidth * 0.3).toInt()
        imageViewLayoutParams.height = (screenHeight * 0.20).toInt()
        val url = booking.movieImageUrl
        Glide.with(context).load(url).centerCrop().error(R.drawable.ic_custom_error)
            .placeholder(R.drawable.ic_image_placeholder).into(bookingImageView)

        itemView.setOnClickListener {
            val intent = Intent(context, BookingDetailsActivity::class.java)
            intent.putExtra("transactionId", booking.transactionId)
            intent.putExtra("movieTitle", booking.movieTitle)
            intent.putExtra("movieImageUrl", booking.movieImageUrl)
            intent.putExtra("theaterName", booking.theaterName)
            intent.putExtra("theaterLocation", booking.location)
            intent.putExtra("date", booking.date)
            intent.putExtra("method", booking.method)
            intent.putExtra("movieTime", booking.time)
            intent.putExtra("language", booking.language)
            intent.putExtra("selectedSeats", booking.bookedSeats)
            intent.putExtra("price", booking.price)
            intent.putExtra("taxes",booking.tax)
            intent.putExtra("total", booking.totalPrice)
            context.startActivity(intent)
        }
        return itemView
    }
}