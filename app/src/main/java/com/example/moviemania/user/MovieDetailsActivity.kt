package com.example.moviemania.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.moviemania.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class MovieDetailsActivity : AppCompatActivity() {


    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_movie_details)

        val movieTitle = intent.getStringExtra("movieTitle")
        val movieTitleTextView = findViewById<TextView>(R.id.movieTitleTextView)
        movieTitleTextView.text = movieTitle

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = movieTitle
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val movieImageView = findViewById<ImageView>(R.id.movieImageView)
        val imageViewLayoutParams = movieImageView.layoutParams
        val displayMetrics = this.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels
        imageViewLayoutParams.width = (screenWidth*0.45).toInt()
        imageViewLayoutParams.height = (screenHeight*0.30).toInt()
        val imageUri = intent.getStringExtra("imageUri")
        Glide.with(this).load(imageUri).centerCrop().error(R.drawable.ic_custom_error)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(movieImageView)

        val description = intent.getStringExtra("description")
        val descriptionTextView = findViewById<TextView>(R.id.movieDescriptionTextView)
        descriptionTextView.text = description

        val decimalFormat = DecimalFormat("0.#")
        val ticketPrice = decimalFormat.format(intent.getDoubleExtra("ticketPrice",0.0))
        val ticketPriceTextView = findViewById<TextView>(R.id.movieTicketPriceTextView)
        ticketPriceTextView.text = "Rs. $ticketPrice"


        val timesList = intent.getStringArrayListExtra("timesList")
        val languagesListArray = intent.getStringArrayListExtra("languagesListArray")
        val theatersListArray = intent.getStringArrayListExtra("theatersListArray")

        val castList = intent.getStringArrayListExtra("castList")
        val linearLayout = findViewById<LinearLayout>(R.id.linearCastView)
        if (castList!!.isNotEmpty()) {
            for (cast in castList) {
                val castRef = db.collection("Casts").document(cast)
                castRef.get().addOnSuccessListener {
                    val castName = it.getString("name")
                    val imageUrl = it.getString("imageUri")
                    val item = layoutInflater.inflate(R.layout.grid_item_cast, null)

                    val castNameTextView = item.findViewById<TextView>(R.id.castNameTextView)
                    castNameTextView.text = castName

                    val castImageView = item.findViewById<ImageView>(R.id.castImageView)

                    val castImageViewLayoutParams = castImageView.layoutParams
                    castImageViewLayoutParams.width = (screenWidth * 0.3).toInt()
                    castImageViewLayoutParams.height = (screenHeight * 0.20).toInt()
                    Glide.with(this).load(imageUrl).centerCrop().error(R.drawable.ic_custom_error)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(castImageView)

                    val adminActions = item.findViewById<LinearLayout>(R.id.admin_actions_cast)
                    adminActions.visibility = View.GONE

                    val dp = 25
                    val scale = displayMetrics.density
                    val dpToPx = (dp * scale +0.5f).toInt()
                    item.setPadding(dpToPx,0,0,dpToPx)
                    linearLayout.addView(item)
                }
            }
        }

        val bookMovieBtn = findViewById<Button>(R.id.bookMovieBTN)
        bookMovieBtn.setOnClickListener {
            val intent = Intent(this, MovieBookingActivity::class.java)
            intent.putExtra("movieTitle", movieTitle)
            intent.putExtra("movieImageUrl", imageUri)
            intent.putStringArrayListExtra("times",timesList)
            intent.putStringArrayListExtra("languages",languagesListArray)
            intent.putStringArrayListExtra("theaters",theatersListArray)
            intent.putExtra("ticketPrice",ticketPrice)
            startActivity(intent)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}