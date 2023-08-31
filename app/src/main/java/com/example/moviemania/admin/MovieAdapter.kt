package com.example.moviemania.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.example.moviemania.R
import com.example.moviemania.admin.bottom_fragment.MoviesFragment
import com.google.firebase.firestore.FirebaseFirestore

class MovieAdapter(private val context: Context, private val movieList: List<MoviesFragment.Movie>) : BaseAdapter() {

    private val db = FirebaseFirestore.getInstance()

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

        val movieEditButton = itemView.findViewById<Button>(R.id.editButtonMovie)
        val movieDeleteButton = itemView.findViewById<Button>(R.id.deleteButtonMovie)

        movieDeleteButton.setOnClickListener {
            deleteMovie(movie.title)
        }

        val uri = movie.photoUri
        Glide.with(context).load(uri).centerCrop().error(R.drawable.ic_custom_error)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(movieImageView)

        return itemView
    }

    private fun deleteMovie(movieTitle: String) {
        val mf = MoviesFragment.newInstance()
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Delete Movie")
            .setMessage(
                HtmlCompat.fromHtml("<br/><b>Are you sure,</b> you want to delete<br/><br/> $movieTitle?<br/>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setPositiveButton("Yes") { _, _ ->
                // Delete the Movie from Firestore
                val moviesCollection = db.collection("Movies")
                moviesCollection.whereEqualTo("title", movieTitle)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val documentSnapshot = querySnapshot.documents[0]
                            val movieId = documentSnapshot.id

                            moviesCollection.document(movieId)
                                .delete()
                                .addOnSuccessListener {
                                    for (document in querySnapshot.documents) {
                                        val theaters =
                                            document.get("theaterList") as? List<String> ?: emptyList()
                                        for (theaterId in theaters ) {
                                            val theaterRef =
                                                db.collection("Theaters").document(theaterId)
                                            val movieSubcollection = theaterRef.collection(movieTitle)

                                            movieSubcollection.get()
                                                .addOnSuccessListener { querySnapshot ->
                                                    val batch = db.batch()
                                                    for (movie in querySnapshot) {
                                                        batch.delete(movie.reference)
                                                    }
                                                    batch.commit()
                                                }
                                        }
                                    }
                                    showToast("$movieTitle deleted successfully!")
                                    mf?.loadMoviesData()
                                }
                                .addOnFailureListener { e ->
                                    showToast("Error deleting $movieTitle: ${e.message}")
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        showToast("Error retrieving movie: ${e.message}")
                    }
            }
            .setNegativeButton("No", null)
            .create()

        alertDialog.show()
    }

    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}