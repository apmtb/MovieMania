package com.example.moviemania.admin.bottom_fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.moviemania.R
import com.example.moviemania.admin.MovieAdapter
import com.example.moviemania.admin.TheaterAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID


class MoviesFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var dialogView: View
    private lateinit var videoView: VideoView
    private lateinit var frameLayout: View
    private lateinit var imageError: TextView
    private lateinit var selectSectionError: TextView
    private lateinit var selectTypeError: TextView
    private lateinit var selectLanguageError: TextView
    private lateinit var selectCastError: TextView
    private lateinit var selectTheaterError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.admin_fragment_movies, container, false)
    }

    companion object {
        @Volatile
        private var instance: MoviesFragment? = null
        fun newInstance():MoviesFragment? {
            return instance
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMoviesData()
        val addMoviesButton = view.findViewById<TextView>(R.id.addMoviesTextView)
        addMoviesButton.setOnClickListener {
            showAddMoviesDialog()
        }
    }

    fun addMovieButtonClick() {
        showAddMoviesDialog()
    }

    private fun clearTextViewErrors(){
        imageError.visibility = View.GONE
        selectSectionError.visibility = View.GONE
        selectTypeError.visibility = View.GONE
        selectLanguageError.visibility = View.GONE
        selectCastError.visibility = View.GONE
        selectTheaterError.visibility = View.GONE
    }
    private fun validateForm(
        movieTitle: EditText,
        imageUri: EditText,
        imageView: ImageView,
        imageError: TextView,
        movieDescription: EditText,
        sectionRadioGroup: RadioGroup,
        selectSectionError: TextView,
        typeRadioGroup: RadioGroup,
        selectTypeError: TextView,
        ticketPriceEditText: EditText
    ): Boolean {
        clearTextViewErrors()
        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_custom_error)
        icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
        if (movieTitle.text.trim().isEmpty()) {
            movieTitle.setError("Movie Title is Required!",icon)
            return false
        }
        if(imageUri.text.trim().isEmpty() && imageView.drawable == null) {
            imageError.visibility = View.VISIBLE
            return false
        }
        if (movieDescription.text.trim().isEmpty()) {
            movieDescription.setError("Movie Location is Required!",icon)
            return false
        }
        if (sectionRadioGroup.checkedRadioButtonId == -1) {
            selectSectionError.visibility = View.VISIBLE
            return false
        }
        if (typeRadioGroup.checkedRadioButtonId == -1) {
            selectTypeError.visibility = View.VISIBLE
            return false
        }
        val ticketPrice = ticketPriceEditText.text.toString()
        if (ticketPrice.isEmpty()) {
            // Ticket price is empty
            ticketPriceEditText.setError("Ticket price is required!",icon)
            return false
        }

        val ticketPriceValue = ticketPrice.toDoubleOrNull()

        if (ticketPriceValue == null || ticketPriceValue <= 0) {
            // Invalid ticket price
            ticketPriceEditText.setError("Invalid ticket price!",icon)
            return false
        }
        return true
    }
    private fun showAddMoviesDialog() {
        if (isAdded) {
            dialogView = layoutInflater.inflate(R.layout.admin_dialog_add_movie, null)
            val uploadImageButton = dialogView.findViewById<Button>(R.id.movieUploadImageButton)
            val imageContainer = dialogView.findViewById<RelativeLayout>(R.id.movieImageContainer)
            setupMultiSelectEditText(R.id.languagesEditText, resources.getStringArray(R.array.languages_array),"Select Languages"){ list ->
                showToast(list.toString())
            }
            setupMultiSelectEditText(R.id.castsEditText, resources.getStringArray(R.array.casts_array),"Select Casts"){ list ->
                showToast(list.toString())
            }
            setupMultiSelectEditText(R.id.theatersEditText, resources.getStringArray(R.array.theaters_array),"Select Theaters"){ list ->
                showToast(list.toString())
            }

            videoView = requireActivity().findViewById(R.id.videoViewLoadingCircleAFM)
            frameLayout = requireActivity().findViewById(R.id.frameLayoutAFM)
            imageError = dialogView.findViewById(R.id.movieImageError)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setTitle("Add Movie")
                .setPositiveButton("Add",null)
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            val imageOptionRadioGroup =
                dialogView.findViewById<RadioGroup>(R.id.movieImageOptionRadioGroup)
            imageOptionRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                imageError.visibility = View.GONE
                val movieImageUri = dialogView.findViewById<EditText>(R.id.movieImageInput)

                movieImageUri.addTextChangedListener {
                    if(movieImageUri.text.trim().isNotEmpty()){
                        imageError.visibility = View.GONE
                    } else {
                        imageError.visibility =View.VISIBLE
                    }
                }

                if (checkedId == R.id.movieUploadImageRadioButton) {
                    imageError.text = "image is Required!"
                    imageContainer.visibility = View.VISIBLE
                    movieImageUri.visibility = View.GONE
                } else {
                    imageError.text = "image URL is Required!"
                    imageContainer.visibility = View.GONE
                    movieImageUri.visibility = View.VISIBLE
                }
            }
            uploadImageButton.setOnClickListener {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(intent, "Select Picture"),
                    234
                )
            }
            dialog.show()
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                val movieNameEditText = dialogView.findViewById<EditText>(R.id.movieTitleEditText)
                val movieImageUri = dialogView.findViewById<EditText>(R.id.movieImageInput)
                val selectedImageView = dialogView.findViewById<ImageView>(R.id.movieSelectedImageView)
                val movieDescriptionEditText = dialogView.findViewById<EditText>(R.id.movieDescriptionEditText)
                val ticketPriceEditText = dialogView.findViewById<EditText>(R.id.movieTicketPriceEditText)
                val sectionOptionRadioGroup = dialogView.findViewById<RadioGroup>(R.id.movieSectionRadioGroup)
                selectSectionError = dialogView.findViewById(R.id.movieSelectSectionError)
                val typeOptionRadioGroup = dialogView.findViewById<RadioGroup>(R.id.movieTypeRadioGroup)
                selectTypeError = dialogView.findViewById(R.id.movieSelectTypeError)
                if(validateForm(movieNameEditText,movieImageUri,selectedImageView,imageError,movieDescriptionEditText,
                        sectionOptionRadioGroup, selectSectionError, typeOptionRadioGroup, selectTypeError, ticketPriceEditText)) {
                    val theaterGridView = view?.findViewById<GridView>(R.id.theaterGridView)
                    val noTheaterTextView = view?.findViewById<RelativeLayout>(R.id.noTheaterTextView)
                    theaterGridView?.visibility = View.GONE
                    noTheaterTextView?.visibility = View.GONE
                    frameLayout.visibility = View.VISIBLE
                    videoView.setOnPreparedListener {
                        it.isLooping = true
                    }
                    val videoPath =
                        "android.resource://" + requireContext().packageName + "/" + R.raw.circle_loading

                    videoView.setVideoURI(Uri.parse(videoPath))
                    videoView.setZOrderOnTop(true)

                    videoView.start()

                    val movieName = movieNameEditText.text.toString()
                    val movieDescription = movieDescriptionEditText.text.toString()

                    val selectedSectionId = sectionOptionRadioGroup.checkedRadioButtonId
                    val section = dialogView.findViewById<RadioButton>(selectedSectionId).text.toString()
                    val selectedTypeId = typeOptionRadioGroup.checkedRadioButtonId
                    val type = dialogView.findViewById<RadioButton>(selectedTypeId).text.toString()
                    val imageOptionRadioGroup =
                        dialogView.findViewById<RadioGroup>(R.id.imageOptionRadioGroupTheater)
                    val selectedRadioButtonId = imageOptionRadioGroup.checkedRadioButtonId
                    val ticketPrice = ticketPriceEditText.text.toString().toDouble()
                    val isUploadImage = selectedRadioButtonId == R.id.uploadImageRadioButtonTheater
                    val isUpcomingchecked = dialogView.findViewById<CheckBox>(R.id.movieUpcomingCheckBox).isChecked

                    if (isUploadImage) {
                        imageContainer.visibility = View.VISIBLE
                        movieImageUri.visibility = View.GONE
//                        uploadImageToFirebaseStorage(selectedImageView, movieName) {
//                            addMovieToFirestore(movieName, it, movieDescription, section,
//                                type, ticketPrice,isUpcomingchecked)
//                        }
                    } else {
                        imageContainer.visibility = View.GONE
                        movieImageUri.visibility = View.VISIBLE
                        val imageUrl = movieImageUri.text.toString()
//                        addMovieToFirestore(movieName, imageUrl, movieDescription, section,
//                            type, ticketPrice,isUpcomingchecked)
                    }
                    dialog.dismiss()
                }
            }
        }
    }

    @Deprecated("")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 234) {
                val selectedImageURI = data?.data
                val selectedImageView = dialogView.findViewById<ImageView>(R.id.theaterImageView)
                if (selectedImageURI != null) {
                    val imageError = dialogView.findViewById<TextView>(R.id.imageErrorTheater)
                    imageError.visibility = View.GONE
                    selectedImageView.setImageURI(selectedImageURI)
                }
            }
        }
    }

    private fun uploadImageToFirebaseStorage(imageView: ImageView, theaterName: String, callback: (String) -> Unit) {
        val theaterCollection = db.collection("Theaters")
        theaterCollection.whereEqualTo("name", theaterName.trim())
            .get()
            .addOnCompleteListener { task ->
                val theaterGridView = view?.findViewById<GridView>(R.id.theaterGridView)
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot != null && !querySnapshot.isEmpty) {
                        frameLayout.visibility = View.GONE
                        videoView.stopPlayback()
                        theaterGridView?.visibility = View.VISIBLE
                        showToast("A theater with the same name already exists.")
                    } else {
                        val storageRef = FirebaseStorage.getInstance().reference
                        val imagesRef = storageRef.child("theater_images/${UUID.randomUUID()}.jpg")

                        // Get the bitmap from the ImageView
                        val drawable = imageView.drawable
                        val bitmap = Bitmap.createBitmap(
                            drawable.intrinsicWidth,
                            drawable.intrinsicHeight,
                            Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(bitmap)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)

                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()

                        val uploadTask = imagesRef.putBytes(data)
                        uploadTask.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                imagesRef.downloadUrl.addOnCompleteListener { urlTask ->
                                    if (urlTask.isSuccessful) {
                                        frameLayout.visibility = View.GONE
                                        videoView.stopPlayback()
                                        val imageUrl = urlTask.result.toString()
                                        callback(imageUrl)
                                    } else {
                                        frameLayout.visibility = View.GONE
                                        videoView.stopPlayback()
                                        theaterGridView?.visibility = View.VISIBLE
                                    }
                                }
                            } else {
                                frameLayout.visibility = View.GONE
                                videoView.stopPlayback()
                                theaterGridView?.visibility = View.VISIBLE
                            }
                        }
                    }
                } else {
                    frameLayout.visibility = View.GONE
                    videoView.stopPlayback()
                    theaterGridView?.visibility = View.VISIBLE
                }
            }
    }

    private fun addMovieToFirestore(movieTitle: String, photoUri: String, description: String,
        section: String, type: String, ticketPrice: Double, isUpcoming: Boolean, language: String,
        selectedCasts: List<String>, selectedTheaters: List<String>) {
        val moviesCollection = db.collection("Movies")
        val movieGridView = view?.findViewById<GridView>(R.id.moviesGridView)

        moviesCollection.whereEqualTo("title", movieTitle.trim())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot != null && !querySnapshot.isEmpty) {
                        frameLayout.visibility = View.GONE
                        videoView.stopPlayback()
                        movieGridView?.visibility = View.VISIBLE
                        showToast("A movie with the same title already exists.")
                    } else {
                        val movieData = hashMapOf(
                            "title" to movieTitle,
                            "photoUri" to photoUri,
                            "description" to description,
                            "section" to section,
                            "type" to type,
                            "ticketPrice" to ticketPrice,
                            "isUpcoming" to isUpcoming,
                            "language" to language,
                            "castList" to selectedCasts,
                            "theaterList" to selectedTheaters
                        )

                        moviesCollection.add(movieData)
                            .addOnSuccessListener {
                                showToast("Movie added successfully.")
                                frameLayout.visibility = View.GONE
                                videoView.stopPlayback()
                                loadMoviesData()
                            }
                            .addOnFailureListener { exception ->
                                frameLayout.visibility = View.GONE
                                videoView.stopPlayback()
                                movieGridView?.visibility = View.VISIBLE
                                showToast("Error adding movie: $exception")
                            }
                    }
                } else {
                    frameLayout.visibility = View.GONE
                    videoView.stopPlayback()
                    movieGridView?.visibility = View.VISIBLE
                    showToast("Error checking movie title: ${task.exception}")
                }
            }
    }

    private fun setupMultiSelectEditText(textViewId: Int, itemsArray: Array<String>,title: String,callback: (ArrayList<String>) -> Unit) {
        val textView = dialogView.findViewById<TextView>(textViewId)
        val selectedItems = ArrayList<String>()
        val checkedItems = BooleanArray(itemsArray.size) { false }
        textView.setOnClickListener {
            showMultiSelectDialog(textView, itemsArray, title, selectedItems, checkedItems) { list->
                callback(list)
            }
        }
    }

    private fun showMultiSelectDialog(textView: TextView, itemsArray: Array<String>,title: String, selectedItems:ArrayList<String>, checkedItems: BooleanArray, callback: (ArrayList<String>) -> Unit) {

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Select Items")
            .setCancelable(false)
            .setMultiChoiceItems(itemsArray, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedItems.add(itemsArray[which])
                    selectedItems.sort()
                } else {
                    selectedItems.remove(itemsArray[which])
                }
            }
            .setPositiveButton("OK",null)
            .setNeutralButton("Clear",null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        val okBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okBtn.setOnClickListener {
            val selectedText = selectedItems.joinToString(", ")
            if (selectedText.isNullOrEmpty()){
                textView.text = title
                textView.setTextColor(Color.GRAY)
            } else {
                if(selectedItems.size>1) {
                    textView.text = "${selectedItems.size} Selected"
                } else {
                    textView.text = selectedItems.joinToString("")
                }
                textView.setTextColor(Color.BLACK)
                callback(selectedItems)
            }
            dialog.dismiss()
        }
        val clearBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        clearBtn.setOnClickListener {
            if(selectedItems.size == 0) {
                dialog.dismiss()
            } else {
                for (i in checkedItems.indices) {
                    checkedItems[i] = false
                }
                textView.text = title
                selectedItems.clear()
                textView.setTextColor(Color.GRAY)
                dialog.dismiss()
            }
        }
    }

    private fun loadMoviesData() {
        if (isAdded) {
            val moviesCollection = db.collection("Movies")
            val noMoviesTextView = requireActivity().findViewById<RelativeLayout>(R.id.noMoviesTextView)
            val moviesGridView = view?.findViewById<GridView>(R.id.moviesGridView)

            moviesCollection.get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        noMoviesTextView.visibility = View.VISIBLE
                        moviesGridView?.visibility = View.GONE
                    } else {
                        noMoviesTextView.visibility = View.GONE
                        moviesGridView?.visibility = View.VISIBLE

                        val moviesList = ArrayList<Movie>()

                        for (document in querySnapshot.documents) {
                            val title = document.getString("title")
                            val photoUri = document.getString("photoUri")
                            val description = document.getString("description")
                            val section = document.getString("section")
                            val type = document.getString("type")
                            val ticketPrice = document.getDouble("ticketPrice") ?: 0.0
                            val dateTime = document.getString("dateTime")
                            val isUpcoming = document.getBoolean("isUpcoming") ?: false
                            val language = document.getString("language")
                            val castList = document.get("castList") as? List<String> ?: emptyList()
                            val theaterList = document.get("theaterList") as? List<String> ?: emptyList()

                            if (title != null && photoUri != null && description != null && section != null &&
                                type != null && dateTime != null && language != null) {
                                val movie = Movie(title, photoUri, description, section, type, ticketPrice,
                                    isUpcoming, language, castList, theaterList)
                                moviesList.add(movie)
                            }
                        }

                        if (isAdded) {
                            val movieAdapter = MovieAdapter(requireContext(), moviesList)
                            moviesGridView?.adapter = movieAdapter
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    showToast("Error: $exception")
                }
        }
    }

    data class Movie(val title: String, val photoUri: String, val description: String,
                     val section: String, val type: String, val ticketPrice: Double,
                     val isUpcoming: Boolean, val language: String,
                     val castList: List<String>, val theaterList: List<String>)

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}