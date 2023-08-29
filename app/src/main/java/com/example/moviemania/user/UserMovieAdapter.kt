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
import com.example.moviemania.user.bottom_fragment.MoviesFragment

class UserMovieAdapter(private val context: Context, private val movieList: List<MoviesFragment.Movie>) : BaseAdapter() {

    override fun getCount(): Int = movieList.size

    override fun getItem(position: Int): Any = movieList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val movie = getItem(position) as MoviesFragment.Movie
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.grid_item_movie, parent, false)
        val movieImageView = itemView.findViewById<ImageView>(R.id.movieImageView)
        val movieTitleTextView = itemView.findViewById<TextView>(R.id.movieTitleTextView)

        movieTitleTextView.text = movie.title
        val imageViewLayoutParams = movieImageView.layoutParams
        val displaymetrics = context.resources.displayMetrics
        val screenHeight = displaymetrics.heightPixels
        val screenWidth = displaymetrics.widthPixels
        imageViewLayoutParams.width = (screenWidth*0.45).toInt()
        imageViewLayoutParams.height = (screenHeight*0.30).toInt()

        val uri = movie.photoUri
        Glide.with(context).load(uri).centerCrop().error(R.drawable.ic_custom_error)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(movieImageView)

        return itemView
    }
}