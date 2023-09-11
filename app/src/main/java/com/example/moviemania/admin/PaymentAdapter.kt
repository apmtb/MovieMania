package com.example.moviemania.admin

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
import com.example.moviemania.admin.bottom_fragment.Booking
import com.example.moviemania.user.BookingDetailsActivity

class PaymentAdapter(private val context: Context, private val castList: List<Booking>)  :
    BaseAdapter() {

    override fun getCount(): Int = castList.size

    override fun getItem(position: Int): Any = castList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val booking = getItem(position) as Booking
        val itemView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_payment, parent, false)
        val bookingImageView = itemView.findViewById<ImageView>(R.id.bookingImage)
        val userMailTextView = itemView.findViewById<TextView>(R.id.userMail)
        val bookingTitleTextView = itemView.findViewById<TextView>(R.id.bookingTitle)
        val theaterTextView = itemView.findViewById<TextView>(R.id.bookedTheater)
        val bookingDateTextView = itemView.findViewById<TextView>(R.id.bookingDate)
        val bookingAmountTextView = itemView.findViewById<TextView>(R.id.bookingAmount)

        userMailTextView.text = booking.email
        bookingTitleTextView.text = booking.movieTitle
        bookingDateTextView.text = "Booked on ${booking.bookedOn}"
        theaterTextView.text = booking.theaterName
        bookingAmountTextView.text = booking.totalPrice

        val url = booking.movieImageUrl
        Glide.with(context).load(url).centerCrop().error(R.drawable.ic_custom_error)
            .placeholder(R.drawable.ic_image_placeholder).into(bookingImageView)

        val methodImageView = itemView.findViewById<ImageView>(R.id.paymentMethodImage)
        when(booking.method){
            "PhonePe" ->
                Glide.with(context).load(R.drawable.logo_phonepe).error(R.drawable.ic_custom_error)
                    .placeholder(R.drawable.ic_image_placeholder).into(methodImageView)
            "Google Pay" ->
                Glide.with(context).load(R.drawable.logo_google_pay).error(R.drawable.ic_custom_error)
                    .placeholder(R.drawable.ic_image_placeholder).into(methodImageView)
            "Credit Card" ->
                Glide.with(context).load(R.drawable.logo_credit_card).error(R.drawable.ic_custom_error)
                    .placeholder(R.drawable.ic_image_placeholder).into(methodImageView)
            "Paytm" ->
                Glide.with(context).load(R.drawable.logo_paytm).error(R.drawable.ic_custom_error)
                    .placeholder(R.drawable.ic_image_placeholder).into(methodImageView)
            "UPI Id" ->
                Glide.with(context).load(R.drawable.logo_upi).error(R.drawable.ic_custom_error)
                    .placeholder(R.drawable.ic_image_placeholder).into(methodImageView)
        }

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