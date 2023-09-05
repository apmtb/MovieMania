package com.example.moviemania.user

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.moviemania.R


class TheaterAdapter(private val context: Context, private val theaters: List<MovieBookingActivity.Theater>) : BaseAdapter() {

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
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.user_item_theater, parent, false)

        val theaterImageView = view.findViewById<ImageView>(R.id.theaterImageView)

        val theaterNameTextView = view.findViewById<TextView>(R.id.theaterNameTextView)
        theaterNameTextView.text = theater.name

        val theaterLocationTextView = view.findViewById<TextView>(R.id.theaterLocationTextView)
        theaterLocationTextView.text = theater.theaterLocation

        val imageViewLayoutParams = theaterImageView.layoutParams
        val displayMetrics = context.resources.displayMetrics

        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels
        imageViewLayoutParams.width = (screenWidth*0.85).toInt()
        imageViewLayoutParams.height = (screenHeight*0.20).toInt()

        Glide.with(context)
            .load(theater.imageUri)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_custom_error)
            .centerCrop()
            .into(theaterImageView)

        return view
    }
}