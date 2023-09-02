package com.example.moviemania.admin

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.example.moviemania.R
import com.example.moviemania.admin.bottom_fragment.MoviesFragment
import com.google.firebase.firestore.FirebaseFirestore

class MovieAdapter(private val context: Context, private val movieList: List<MoviesFragment.Movie>) : BaseAdapter() {

    private val db = FirebaseFirestore.getInstance()
    lateinit var dialogView: View
    private lateinit var imageError: TextView
    private lateinit var selectSectionError: TextView
    private lateinit var selectLanguageError: TextView
    private lateinit var selectTimeError: TextView
    private lateinit var selectCastError: TextView
    private lateinit var selectTheaterError: TextView
    private var imageChanged = false
    private var timesChanged = false
    private var theatersChanged = false

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

        movieEditButton.setOnClickListener {
            showUpdateMoviesDialog(movie.title, movie.photoUri, movie.description,
                movie.section, movie.ticketPrice, movie.isUpcoming, movie.language,
                movie.timesList, movie.castNames, movie.theaterNames, movie.castList, movie.theaterList)
        }

        movieDeleteButton.setOnClickListener {
            deleteMovie(movie.title)
        }

        val uri = movie.photoUri
        Glide.with(context).load(uri).centerCrop().error(R.drawable.ic_custom_error)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(movieImageView)

        return itemView
    }

    private fun clearTextViewErrors(){
        imageError.visibility = View.GONE
        selectSectionError.visibility = View.GONE
        selectLanguageError.visibility = View.GONE
        selectCastError.visibility = View.GONE
        selectTimeError.visibility = View.GONE
        selectTheaterError.visibility = View.GONE
    }

    private fun validateForm(
        currentTitle: String,
        currentImageUri: String,
        currentDescription: String,
        currentSection: String,
        currentPrice: Double,
        currentIsUpcoming: Boolean,
        currentLanguages: String,
        currentTimesList: List<String>,
        currentCastList: List<String>,
        currentTheaterList: List<String>,
        movieTitle: EditText,
        imageUri: EditText,
        imageView: ImageView,
        movieDescription: EditText,
        sectionRadioGroup: RadioGroup,
        ticketPriceEditText: EditText,
        isUpcoming: Boolean,
        languagesList: MutableList<String>,
        timeList: MutableList<String>,
        castList: MutableList<String>,
        theatersList: MutableList<String>,
        noDataChangedError: TextView
    ): Boolean {
        clearTextViewErrors()
        val icon = ContextCompat.getDrawable(context, R.drawable.ic_custom_error)
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

        if(timeList.isEmpty()){
            selectTimeError.visibility = View.VISIBLE
            return false
        }

        if(languagesList.isEmpty()){
            selectLanguageError.visibility = View.VISIBLE
            return false
        }

        if(castList.isEmpty()){
            selectCastError.visibility = View.VISIBLE
            return false
        }

        if(theatersList.isEmpty()){
            selectTheaterError.visibility = View.VISIBLE
            return false
        }

        val selectedSectionId = sectionRadioGroup.checkedRadioButtonId
        val section = dialogView.findViewById<RadioButton>(selectedSectionId).text.toString()

        if ( currentTitle == movieTitle.text.trim().toString() &&
            currentImageUri == imageUri.text.trim().toString() &&
            currentDescription == movieDescription.text.trim().toString() &&
            currentSection == section && currentPrice == ticketPriceValue &&
            (isUpcoming == currentIsUpcoming) &&
            currentLanguages == languagesList.joinToString(", ") &&
            currentTimesList.joinToString(", ") == timeList.joinToString(", ") &&
            currentCastList.joinToString(", ") == castList.joinToString(", ") &&
            currentTheaterList.joinToString(", ") == theatersList.joinToString(", ") &&
            !isImageChanged()){
            noDataChangedError.visibility = View.VISIBLE
            return false
        }

        return true
    }

    private fun isImageChanged(): Boolean {
        return imageChanged
    }

    private fun showUpdateMoviesDialog(currentTitle: String, currentPhotoUri: String,
                                       currentDescription: String, currentSection: String,
                                       currentTicketPrice: Double, currentIsUpcoming: Boolean,
                                       currentLanguage: String, currentTimesList: List<String>,
                                       castNames:List<String>, theaterNames:List<String>,
                                       currentCastList: List<String>, currentTheaterList: List<String>) {
        val mf = MoviesFragment.newInstance()
        dialogView = LayoutInflater.from(context).inflate(R.layout.admin_dialog_add_movie, null)
        mf?.dialogView = dialogView
        mf?.videoView = mf?.requireActivity()!!.findViewById(R.id.videoViewLoadingCircleAFM)
        mf.frameLayout = mf.requireActivity().findViewById(R.id.frameLayoutAFM)
        val uploadImageButton = dialogView.findViewById<Button>(R.id.movieUploadImageButton)
        val imageContainer = dialogView.findViewById<RelativeLayout>(R.id.movieImageContainer)
        imageError = dialogView.findViewById(R.id.movieImageError)
        val noDataChangedError = dialogView.findViewById<TextView>(R.id.noDataChangedErrorMovie)
        val timesList: MutableList<String> = mutableListOf()
        val languagesList: MutableList<String> = mutableListOf()
        val castsListIds: MutableList<String> = mutableListOf()
        val theatersListIds: MutableList<String> = mutableListOf()
        val removedTheaterList: MutableList<String> = mutableListOf()
        val addedTheaterList: MutableList<String> = mutableListOf()
        val removedTimesList: MutableList<String> = mutableListOf()
        val addedTimesList: MutableList<String> = mutableListOf()

        timesList.addAll(currentTimesList)
        languagesList.addAll(currentLanguage.split(", "))
        castsListIds.addAll(currentCastList)
        theatersListIds.addAll(currentTheaterList)

        mf.retrieveDocumentNames("Casts") { list ->
            setupMultiSelectTextView(
                R.id.castsSpinnerTextView,
                castNames,
                list.toTypedArray(),
                "Select Casts"
            ) { castList ->
                castsListIds.clear()
                mf.getDocumentIdsFromNames("Casts", castList) { ids ->
                    castsListIds.addAll(ids)
                }
            }
        }
        mf.retrieveDocumentNames("Theaters") { list ->
            setupMultiSelectTextView(
                R.id.theatersSpinnerTextView,
                theaterNames,
                list.toTypedArray(),
                "Select Theaters"
            ) { theaterList ->
                val removedList = theaterNames.subtract(theaterList.toSet())
                val addedList  = theaterList.subtract(theaterNames.toSet())
                theatersChanged = removedList.isNotEmpty() || addedList.isNotEmpty()
                removedTheaterList.clear()
                addedTheaterList.clear()
                if(theatersChanged){
                    if (removedList.isNotEmpty()){
                        removedTheaterList.addAll(removedList)
                    }
                    if (addedList.isNotEmpty()){
                        addedTheaterList.addAll(addedList)
                    }
                }
                theatersListIds.clear()
                mf.getDocumentIdsFromNames("Theaters", theaterList) { ids ->
                    theatersListIds.addAll(ids)
                }
            }
        }

        setupMultiSelectTextView(
            R.id.languagesSpinnerTextView,
            currentLanguage.split(", "),
            mf.resources.getStringArray(R.array.languages_array),
            "Select Languages"
        ) { list ->
            languagesList.clear()
            languagesList.addAll(list)
        }

        setupMultiSelectTextView(
            R.id.timesSpinnerTextView,
            currentTimesList,
            mf.resources.getStringArray(R.array.times_array),
            "Select Times"
        ) { list ->
            val removedList = currentTimesList.subtract(list.toSet())
            val addedList = list.subtract(currentTimesList.toSet())
            timesChanged = removedList.isNotEmpty() || addedList.isNotEmpty()
            removedTimesList.clear()
            addedTimesList.clear()
            if(timesChanged){
                if (removedList.isNotEmpty()){
                    removedTimesList.addAll(removedList)
                }
                if (addedList.isNotEmpty()){
                    addedTimesList.addAll(addedList)
                }
            }
            timesList.clear()
            timesList.addAll(list)
        }

        mf.videoView = mf.requireActivity().findViewById(R.id.videoViewLoadingCircleAFM)
        mf.frameLayout = mf.requireActivity().findViewById(R.id.frameLayoutAFM)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Update Movie")
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()


        val selectedImageView = dialogView.findViewById<ImageView>(R.id.movieSelectedImageView)
        val movieTitleEditText = dialogView.findViewById<EditText>(R.id.movieTitleEditText)
        val movieImageUri = dialogView.findViewById<EditText>(R.id.movieImageInput)
        val movieDescriptionEditText =
            dialogView.findViewById<EditText>(R.id.movieDescriptionEditText)
        val ticketPriceEditText =
            dialogView.findViewById<EditText>(R.id.movieTicketPriceEditText)
        val sectionOptionRadioGroup =
            dialogView.findViewById<RadioGroup>(R.id.movieSectionRadioGroup)
        selectSectionError = dialogView.findViewById(R.id.movieSelectSectionError)
        selectLanguageError = dialogView.findViewById(R.id.movieSelectLanguageError)
        selectCastError = dialogView.findViewById(R.id.movieSelectCastError)
        selectTimeError = dialogView.findViewById(R.id.movieSelectTimesError)
        selectTheaterError = dialogView.findViewById(R.id.movieSelectTheaterError)
        val isUpcomingCheckBox =
            dialogView.findViewById<CheckBox>(R.id.movieUpcomingCheckBox)
        movieTitleEditText.setText(currentTitle)
        movieImageUri.setText(currentPhotoUri)
        movieDescriptionEditText.setText(currentDescription)
        ticketPriceEditText.setText(currentTicketPrice.toString())
        isUpcomingCheckBox.isChecked = currentIsUpcoming
        val radioTrending = dialogView.findViewById<RadioButton>(R.id.trendingRadioButton)
        val radioPopular = dialogView.findViewById<RadioButton>(R.id.popularRadioButton)
        val radioAll = dialogView.findViewById<RadioButton>(R.id.radioAll)
        when (currentSection) {
            radioTrending.text.toString() -> radioTrending.isChecked = true
            radioPopular.text.toString() -> radioPopular.isChecked = true
            radioAll.text.toString() -> radioAll.isChecked = true
        }
        val imageViewLayoutParams = selectedImageView.layoutParams
        val displaymetrics = context.resources.displayMetrics
        val screenHeight = displaymetrics.heightPixels
        val screenWidth = displaymetrics.widthPixels
        imageViewLayoutParams.width = (screenWidth * 0.3).toInt()
        imageViewLayoutParams.height = (screenHeight * 0.20).toInt()


        Glide.with(context).load(currentPhotoUri).centerCrop().error(R.drawable.ic_custom_error)
            .placeholder(R.drawable.ic_image_placeholder).into(selectedImageView)

        val imageOptionRadioGroup =
            dialogView.findViewById<RadioGroup>(R.id.movieImageOptionRadioGroup)
        imageOptionRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            imageError.visibility = View.GONE
            val movieImageUri = dialogView.findViewById<EditText>(R.id.movieImageInput)

            movieImageUri.addTextChangedListener {
                if (movieImageUri.text.trim().isNotEmpty()) {
                    imageError.visibility = View.GONE
                } else {
                    imageError.visibility = View.VISIBLE
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

        selectedImageView.tag = "0"
        uploadImageButton.setOnClickListener {
            mf.selectImageIntent()
        }
        dialog.show()

        val updateButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        updateButton.setOnClickListener {
            if (selectedImageView.tag == "1") {
                imageChanged = true
            }
            val isUpcomingchecked = isUpcomingCheckBox.isChecked
            if (validateForm(currentTitle, currentPhotoUri, currentDescription,
                    currentSection, currentTicketPrice, currentIsUpcoming, currentLanguage,
                    currentTimesList,currentCastList, currentTheaterList, movieTitleEditText, movieImageUri,
                    selectedImageView, movieDescriptionEditText, sectionOptionRadioGroup, ticketPriceEditText,
                    isUpcomingchecked, languagesList, timesList, castsListIds, theatersListIds, noDataChangedError
                )
            ) {
                val theaterGridView = mf.view?.findViewById<GridView>(R.id.moviesGridView)
                val noTheaterTextView = mf.view?.findViewById<RelativeLayout>(R.id.noMoviesTextView)
                theaterGridView?.visibility = View.GONE
                noTheaterTextView?.visibility = View.GONE
                mf.frameLayout.visibility = View.VISIBLE
                mf.videoView.setOnPreparedListener {
                    it.isLooping = true
                }
                val videoPath =
                    "android.resource://" + context.packageName + "/" + R.raw.circle_loading

                mf.videoView.setVideoURI(Uri.parse(videoPath))
                mf.videoView.setZOrderOnTop(true)

                mf.videoView.start()

                val movieTitle = movieTitleEditText.text.toString()
                val movieDescription = movieDescriptionEditText.text.toString()

                val selectedSectionId = sectionOptionRadioGroup.checkedRadioButtonId
                val section =
                    dialogView.findViewById<RadioButton>(selectedSectionId).text.toString()
                val selectedRadioButtonId = imageOptionRadioGroup.checkedRadioButtonId
                val ticketPrice = ticketPriceEditText.text.toString().toDouble()
                val isUploadImage = selectedRadioButtonId == R.id.movieUploadImageRadioButton

                if (isUploadImage) {
                    imageContainer.visibility = View.VISIBLE
                    movieImageUri.visibility = View.GONE

                    if (theatersChanged) {
                        if (removedTheaterList.isNotEmpty()) {
                            mf.getDocumentIdsFromNames("Theaters",removedTheaterList) { ids ->
                                for (theaterId in ids ) {
                                    val theaterRef =
                                        db.collection("Theaters").document(theaterId)
                                    val movieSubcollection = theaterRef.collection(movieTitle)

                                    movieSubcollection.get()
                                        .addOnSuccessListener { querySnapshot ->
                                            val batch = db.batch()
                                            for (times in querySnapshot) {
                                                batch.delete(times.reference)
                                            }
                                            batch.commit()
                                        }
                                }
                            }
                        }
                        if ( addedTheaterList.isNotEmpty() ) {
                            mf.getDocumentIdsFromNames("Theaters",addedTheaterList) { ids ->
                                for (theaterId in ids ) {
                                    val theaterRef = db.collection("Theaters").document(theaterId)
                                    theaterRef.get().addOnSuccessListener {
                                        val colLength = it.getString("seatColnum")!!.toInt()
                                        val rowLength = it.getString("seatRownum")!!.toInt()
                                        val initialSeatState = MutableList(colLength * rowLength) { false }
                                        val moviesSubCollectionRef = theaterRef.collection(movieTitle)
                                        for (time in timesList) {
                                            val timesDocument = moviesSubCollectionRef.document(time)
                                            val seatData = hashMapOf(
                                                "seats" to initialSeatState
                                            )
                                            timesDocument.set(seatData)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (timesChanged) {
                        if ( addedTimesList.isNotEmpty() ) {
                            for (theaterId in theatersListIds) {
                                val theaterRef = db.collection("Theaters").document(theaterId)

                                theaterRef.get().addOnSuccessListener {
                                    val colLength = it.getString("seatColnum")!!.toInt()
                                    val rowLength = it.getString("seatRownum")!!.toInt()
                                    val initialSeatState = MutableList(colLength * rowLength) { false }
                                    val moviesSubCollectionRef = theaterRef.collection(movieTitle)
                                    for (time in addedTimesList) {
                                        val timesDocument = moviesSubCollectionRef.document(time)
                                        val seatData = hashMapOf(
                                            "seats" to initialSeatState
                                        )
                                        timesDocument.set(seatData)
                                    }
                                }
                            }
                        }
                        if ( removedTimesList.isNotEmpty() ) {
                            for ( theaterId in theatersListIds ) {
                                val theaterRef = db.collection("Theaters").document(theaterId)
                                val movieSubcollection = theaterRef.collection(movieTitle)

                                for ( times in removedTimesList ) {
                                    movieSubcollection.document(times).delete()
                                }
                            }
                        }
                    }
                    mf.uploadImageToFirebaseStorage(selectedImageView, movieTitle) {
                        updateMovieData(
                            currentTitle, movieTitle, it, movieDescription, section,
                            ticketPrice, isUpcomingchecked, languagesList.joinToString(", "),
                            timesList, castsListIds, theatersListIds
                        )
                    }
                } else {
                    imageContainer.visibility = View.GONE
                    movieImageUri.visibility = View.VISIBLE
                    val imageUrl = movieImageUri.text.toString()
                    updateMovieData(
                        currentTitle, movieTitle, imageUrl, movieDescription, section,
                        ticketPrice, isUpcomingchecked, languagesList.joinToString(", "),
                        timesList, castsListIds, theatersListIds
                    )
                }
                dialog.dismiss()
            }
        }
    }

    private fun updateMovieData(
        currentTitle: String, newTitle: String, newImageUrl: String,
        newDescription: String, newSection: String, newTicketPrice: Double,
        newIsUpcoming: Boolean, newLanguage: String, newTimes: List<String>,
        newCasts: List<String>, newTheaters: List<String>
    ) {
        val mf = MoviesFragment.newInstance()

        db.collection("Movies")
            .whereEqualTo("title", currentTitle)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val movieDocument = querySnapshot.documents[0]
                val movieRef = db.collection("Movies").document(movieDocument.id)

                val updateData = mutableMapOf<String, Any>()
                updateData["title"] = newTitle
                updateData["imageUri"] = newImageUrl
                updateData["description"] = newDescription
                updateData["section"] = newSection
                updateData["ticketPrice"] = newTicketPrice
                updateData["isUpcoming"] = newIsUpcoming
                updateData["language"] = newLanguage
                updateData["times"] = newTimes
                updateData["castList"] = newCasts
                updateData["theaterList"] = newTheaters

                movieRef.update(updateData)
                    .addOnSuccessListener {
                        mf?.loadMoviesData()
                        mf?.frameLayout?.visibility = View.GONE
                        mf?.videoView?.stopPlayback()
                        showToast("Movie updated successfully")
                    }
                    .addOnFailureListener { e ->
                        mf?.frameLayout?.visibility = View.GONE
                        mf?.videoView?.stopPlayback()
                        val castGridView = mf?.view?.findViewById<GridView>(R.id.castGridView)
                        castGridView?.visibility = View.VISIBLE
                        showToast("Error updating movie: ${e.message}")
                    }
            }
    }

    private fun setupMultiSelectTextView(textViewId: Int, selectedList: List<String>, itemsArray: Array<String>,title: String,callback: (ArrayList<String>) -> Unit) {
        val textView = dialogView.findViewById<TextView>(textViewId)
        val selectedItems = ArrayList<String>()
        selectedItems.addAll(selectedList)
        val checkedItems = BooleanArray(itemsArray.size) { false }
        for (i in itemsArray.indices) {
            if (selectedList.contains(itemsArray[i])) {
                checkedItems[i] = true
            }
        }
        val selectedText = selectedItems.joinToString(", ")
        if (selectedText.isNullOrEmpty()){
            textView.text = title
            textView.setTextColor(Color.GRAY)
            selectedItems.clear()
        } else {
            if(selectedItems.size>1) {
                textView.text = "${selectedItems.size} Selected"
            } else {
                textView.text = selectedItems.joinToString("")
            }
            textView.setTextColor(Color.BLACK)
        }
        textView.setOnClickListener {
            showMultiSelectDialog(textView, itemsArray, title, selectedItems, checkedItems) { list->
                callback(list)
            }
        }
    }

    private fun showMultiSelectDialog(textView: TextView, itemsArray: Array<String>,title: String, selectedItems:ArrayList<String>, checkedItems: BooleanArray, callback: (ArrayList<String>) -> Unit) {

        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
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
                selectedItems.clear()
            } else {
                if(selectedItems.size>1) {
                    textView.text = "${selectedItems.size} Selected"
                } else {
                    textView.text = selectedItems.joinToString("")
                }
                textView.setTextColor(Color.BLACK)
            }
            callback(selectedItems)
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
            callback(selectedItems)
        }
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
                                                    for (times in querySnapshot) {
                                                        batch.delete(times.reference)
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