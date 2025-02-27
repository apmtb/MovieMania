package com.example.moviemania.user

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.moviemania.R
import com.example.moviemania.user.bottom_fragment.MoviesFragment

class UserMovieAdapter(
    private val context: Context,
    private val movieList: List<MoviesFragment.Movie>
) : BaseAdapter() {

    override fun getCount(): Int = movieList.size

    override fun getItem(position: Int): Any = movieList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val movie = getItem(position) as MoviesFragment.Movie
        val itemView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.grid_item_movie, parent, false)
        val adminActions = itemView.findViewById<LinearLayout>(R.id.admin_actions_movie)
        adminActions.visibility = View.GONE
        val movieImageView = itemView.findViewById<ImageView>(R.id.movieImageView)
        val movieTitleTextView = itemView.findViewById<TextView>(R.id.movieTitleTextView)

        movieTitleTextView.text = movie.title
        val imageViewLayoutParams = movieImageView.layoutParams
        val displayMetrics = context.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels
        imageViewLayoutParams.width = (screenWidth * 0.45).toInt()
        imageViewLayoutParams.height = (screenHeight * 0.30).toInt()

        val uri = movie.photoUri
        Glide.with(context).load(uri).centerCrop().error(R.drawable.ic_custom_error)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(movieImageView)

        itemView.setOnClickListener {
            val intent = Intent(context, MovieDetailsActivity::class.java)
            intent.putExtra("movieTitle", movie.title)
            intent.putExtra("imageUri", movie.photoUri)
            intent.putExtra("description", movie.description)
            intent.putExtra("ticketPrice", movie.ticketPrice)
            intent.putExtra("upcoming", movie.isUpcoming)

            val timesListArray = ArrayList<String>()
            timesListArray.addAll(movie.timesList)
            intent.putStringArrayListExtra("timesList", timesListArray)

            val languagesListArray = ArrayList<String>()
            languagesListArray.addAll(movie.language.split(", "))
            intent.putStringArrayListExtra("languagesListArray", languagesListArray)

            val castListArray = ArrayList<String>()
            castListArray.addAll(movie.castList)
            intent.putStringArrayListExtra("castList", castListArray)

            val theatersListArray = ArrayList<String>()
            theatersListArray.addAll(movie.theaterList)
            intent.putStringArrayListExtra("theatersListArray", theatersListArray)

            context.startActivity(intent)
        }

        return itemView
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}