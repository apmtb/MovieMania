package com.example.moviemania.admin

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.NumberPicker
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
import com.example.moviemania.admin.bottom_fragment.TheatersFragment
import com.google.firebase.firestore.FirebaseFirestore


class TheaterAdapter(private val context: Context, private val theaters: List<TheatersFragment.Theater>) : BaseAdapter() {

    private val db = FirebaseFirestore.getInstance()
    private var imageChanged = false

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
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.grid_item_theater, parent, false)

        val theaterImageView = view.findViewById<ImageView>(R.id.theaterImageView)
        val theaterNameTextView = view.findViewById<TextView>(R.id.theaterNameTextView)

        theaterNameTextView.text = theater.name


        val imageViewLayoutParams = theaterImageView.layoutParams
        val displaymetrics = context.resources.displayMetrics

        val screenHeight = displaymetrics.heightPixels
        val screenWidth = displaymetrics.widthPixels
        imageViewLayoutParams.width = (screenWidth*0.85).toInt()
        imageViewLayoutParams.height = (screenHeight*0.20).toInt()


        val theaterEditButton = view.findViewById<Button>(R.id.editButtonTheater)
        val theaterDeleteButton = view.findViewById<Button>(R.id.deleteButtonTheater)

        theaterEditButton.setOnClickListener {
            showUpdateTheaterDialog(theater.name,theater.imageUri,theater.theaterLocation,
                theater.seatColLength, theater.seatRowLength)
        }

        theaterDeleteButton.setOnClickListener {
            deleteTheater(theater.name)
        }

        Glide.with(context)
            .load(theater.imageUri)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_custom_error)
            .centerCrop()
            .into(theaterImageView)
        return view
    }

    private fun validateForm(
        currentName: String,
        currentImageUrl: String,
        currentLocation: String,
        currentColLength: Int,
        currentRowLength: Int,
        theaterName: EditText,
        imageUri: EditText,
        imageView: ImageView,
        textView: TextView,
        theaterLocation: EditText,
        colLength: Int,
        rowLength: Int,
        noDataChangedError: TextView
    ): Boolean {
        val icon = ContextCompat.getDrawable(context, R.drawable.ic_custom_error)
        icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
        if (theaterName.text.trim().isEmpty()) {
            theaterName.setError("Theater Name is Required!",icon)
            return false
        }
        if(imageUri.text.trim().isEmpty() && imageView.drawable == null) {
            textView.visibility = View.VISIBLE
            return false
        }
        if (theaterLocation.text.trim().isEmpty()) {
            theaterLocation.setError("Theater Location is Required!",icon)
            return false
        }
        if ( currentName == theaterName.text.trim().toString() &&
             currentImageUrl == imageUri.text.trim().toString() &&
             currentLocation == theaterLocation.text.trim().toString() &&
             currentColLength == colLength && currentRowLength == rowLength &&!isImageChanged()){
            noDataChangedError.visibility = View.VISIBLE
            return false
        }
        return true
    }

    private fun isImageChanged(): Boolean {
        return imageChanged
    }

    private fun showUpdateTheaterDialog(currentName: String, currentImageUrl: String,
                                        currentLocation: String, currentColLength: Int,
                                        currentRowLength: Int) {
        val tf = TheatersFragment.newInstance()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.admin_dialog_add_theater, null)
        tf?.dialogView = dialogView
        tf?.videoView = tf?.requireActivity()!!.findViewById(R.id.videoViewLoadingCircleAFT)
        tf.frameLayout = tf.requireActivity().findViewById(R.id.frameLayoutAFT)
        val uploadImageButton = dialogView.findViewById<Button>(R.id.uploadImageButtonTheater)
        val imageContainer = dialogView.findViewById<RelativeLayout>(R.id.imageContainerTheater)
        val rowLengthPicker: NumberPicker = dialogView.findViewById(R.id.rowLengthPicker)
        val colLengthPicker: NumberPicker = dialogView.findViewById(R.id.colLengthPicker)

        rowLengthPicker.minValue = 2
        rowLengthPicker.maxValue = 10
        rowLengthPicker.value = currentRowLength

        colLengthPicker.minValue = 2
        colLengthPicker.maxValue = 10
        colLengthPicker.value = currentColLength

        tf.videoView = tf.requireActivity().findViewById(R.id.videoViewLoadingCircleAFT)
        tf.frameLayout = tf.requireActivity().findViewById(R.id.frameLayoutAFT)
        val imageError = dialogView.findViewById<TextView>(R.id.imageErrorTheater)
        val noDataChangedError = dialogView.findViewById<TextView>(R.id.noDataChangedErrorTheater)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Add Theater")
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        val selectedImageView = dialogView.findViewById<ImageView>(R.id.theaterImageView)
        val theaterNameEditText = dialogView.findViewById<EditText>(R.id.theaterNameEditText)
        val theaterImageUri = dialogView.findViewById<EditText>(R.id.theaterImageInput)
        val theaterLocationEditText = dialogView.findViewById<EditText>(R.id.theaterLocationEditText)

        val imageViewLayoutParams = selectedImageView.layoutParams
        val displaymetrics = tf.requireContext().resources.displayMetrics
        val screenHeight = displaymetrics.heightPixels
        val screenWidth = displaymetrics.widthPixels
        imageViewLayoutParams.width = (screenWidth * 0.85).toInt()
        imageViewLayoutParams.height = (screenHeight * 0.20).toInt()

        theaterNameEditText.setText(currentName)
        theaterImageUri.setText(currentImageUrl)
        theaterLocationEditText.setText(currentLocation)

        Glide.with(context).load(currentImageUrl).centerCrop().error(R.drawable.ic_custom_error)
            .placeholder(R.drawable.ic_image_placeholder).into(selectedImageView)

        val imageOptionRadioGroup =
            dialogView.findViewById<RadioGroup>(R.id.imageOptionRadioGroupTheater)
        imageOptionRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            imageError.visibility = View.GONE

            theaterImageUri.addTextChangedListener {
                if (theaterImageUri.text.trim().isNotEmpty()) {
                    imageError.visibility = View.GONE
                } else {
                    imageError.visibility = View.VISIBLE
                }
            }

            if (checkedId == R.id.uploadImageRadioButtonTheater) {
                imageError.text = "image is Required!"
                imageContainer.visibility = View.VISIBLE
                theaterImageUri.visibility = View.GONE
            } else {
                imageError.text = "image URL is Required!"
                imageContainer.visibility = View.GONE
                theaterImageUri.visibility = View.VISIBLE
            }
        }
        selectedImageView.tag = "0"
        uploadImageButton.setOnClickListener {
            tf.selectImageIntent()
        }

        dialog.show()
        val updateButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        updateButton.setOnClickListener {
            if (selectedImageView.tag == "1") {
                imageChanged = true
            }
            if ( validateForm( currentName, currentImageUrl, currentLocation, currentColLength,
                    currentRowLength, theaterNameEditText, theaterImageUri, selectedImageView,
                    imageError, theaterLocationEditText, colLengthPicker.value,
                    rowLengthPicker.value, noDataChangedError ) ) {
                val theaterGridView = tf.view?.findViewById<GridView>(R.id.theaterGridView)
                theaterGridView?.visibility = View.GONE
                tf.frameLayout.visibility = View.VISIBLE
                tf.videoView.setOnPreparedListener {
                    it.isLooping = true
                }
                val videoPath =
                    "android.resource://" + tf.requireContext().packageName + "/" + R.raw.circle_loading

                tf.videoView.setVideoURI(Uri.parse(videoPath))
                tf.videoView.setZOrderOnTop(true)

                tf.videoView.start()

                val theaterName = theaterNameEditText.text.toString()
                val theaterLocation = theaterLocationEditText.text.toString()
                val selectedRadioButtonId = imageOptionRadioGroup.checkedRadioButtonId
                val isUploadImage = selectedRadioButtonId == R.id.uploadImageRadioButtonTheater
                val seatColLength = colLengthPicker.value.toString()
                val seatRowLength = rowLengthPicker.value.toString()

                if (isUploadImage) {
                    imageContainer.visibility = View.VISIBLE
                    theaterImageUri.visibility = View.GONE
                    tf.uploadImageToFirebaseStorage(selectedImageView, theaterName) {
                        updateTheaterData(
                            currentName, theaterName, it, theaterLocation,
                            seatColLength, seatRowLength
                        )
                    }
                } else {
                    imageContainer.visibility = View.GONE
                    theaterImageUri.visibility = View.VISIBLE
                    val imageUrl = theaterImageUri.text.toString()
                    updateTheaterData(
                        currentName, theaterName, imageUrl, theaterLocation,
                        seatColLength, seatRowLength
                    )
                }
                dialog.dismiss()
            }
        }
    }

    private fun updateTheaterData(
        currentName: String,
        newName: String,
        newImageUrl: String,
        newLocation: String,
        newSeatColLength: String,
        newSeatRowLength: String
    ) {
        val tf = TheatersFragment.newInstance()
        val theaterCollection = db.collection("Theaters")

        theaterCollection.whereEqualTo("name", currentName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val theaterDocument = querySnapshot.documents[0]
                val theaterRef = theaterDocument.reference

                val initialSeatState =
                    MutableList(newSeatColLength.toInt() * newSeatRowLength.toInt()) { false }
                // Create the update data map
                val updateData = mutableMapOf<String, Any>()
                updateData["name"] = newName
                updateData["imageUri"] = newImageUrl
                updateData["location"] = newLocation
                updateData["seatColnum"] = newSeatColLength
                updateData["seatRownum"] = newSeatRowLength
                updateData["seats"] = initialSeatState

                // Update the document
                theaterRef.update(updateData)
                    .addOnSuccessListener {
                        retrieveMovieTitles("Movies") { movies ->
                            for ( movie in movies ) {
                                theaterRef.collection(movie).get()
                                    .addOnSuccessListener { querySnapshot ->
                                        val batch = db.batch()
                                        for (timeDocument in querySnapshot.documents) {
                                            val seatLayoutData = mapOf("seats" to initialSeatState)
                                            batch.update( timeDocument.reference, seatLayoutData )
                                        }
                                        batch.commit()
                                    }
                            }
                        }
                        tf?.loadTheaterData()
                        tf?.frameLayout?.visibility = View.GONE
                        tf?.videoView?.stopPlayback()
                        showToast("Theater updated successfully")
                    }
                    .addOnFailureListener { e ->
                        tf?.frameLayout?.visibility = View.GONE
                        tf?.videoView?.stopPlayback()
                        val theaterGridView = tf?.view?.findViewById<GridView>(R.id.theaterGridView)
                        theaterGridView?.visibility = View.VISIBLE
                        showToast("Error updating theater: ${e.message}")
                    }
            }
    }

    private fun deleteTheater(theaterName: String) {
        val tf = TheatersFragment.newInstance()
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Delete Theater")
            .setMessage(
                HtmlCompat.fromHtml("<br/><b>Are you sure,</b> you want to delete<br/><br/> $theaterName?<br/>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setPositiveButton("Yes") { _, _ ->
                // Delete the Theater from Firestore
                val theatersCollection = db.collection("Theaters")
                theatersCollection.whereEqualTo("name", theaterName)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val documentSnapshot = querySnapshot.documents[0]
                            val documentRef = documentSnapshot.reference
                            val theaterId = documentSnapshot.id

                            retrieveMovieTitles("Movies") { movies ->
                                for ( movie in movies ) {
                                    documentRef.collection(movie).get()
                                        .addOnSuccessListener { querySnapshot ->
                                            val batch = db.batch()
                                            for (timeDocument in querySnapshot.documents) {
                                                batch.delete( timeDocument.reference )
                                            }
                                            batch.commit()
                                        }
                                }
                            }

                            theatersCollection.document(theaterId)
                                .delete()
                                .addOnSuccessListener {
                                    val moviesCollectionRef = db.collection("Movies")

                                    moviesCollectionRef.whereArrayContains("theaterList", theaterId)
                                        .get()
                                        .addOnSuccessListener { querySnapshot ->
                                            val batch = db.batch()
                                            for (movieDocument in querySnapshot.documents) {
                                                val movieRef = moviesCollectionRef.document(movieDocument.id)
                                                val updatedTheaterList = movieDocument.get("theaterList") as? MutableList<String>
                                                updatedTheaterList?.remove(theaterId)
                                                batch.update(movieRef, "theaterList", updatedTheaterList)
                                            }
                                            batch.commit()
                                        }
                                    showToast("$theaterName deleted successfully!")
                                    tf?.loadTheaterData()
                                }
                                .addOnFailureListener { e ->
                                    showToast("Error deleting $theaterName: ${e.message}")
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        showToast("Error retrieving theater: ${e.message}")
                    }
            }
            .setNegativeButton("No", null)
            .create()

        alertDialog.show()
    }

    private fun retrieveMovieTitles(collectionPath: String, callback: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection(collectionPath)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val names = querySnapshot.documents.map { document ->
                    document.getString("title") ?: ""
                }
                callback(names)
            }
            .addOnFailureListener { e ->
                showToast("Error Retrieving Document Names!")
            }
    }

    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}