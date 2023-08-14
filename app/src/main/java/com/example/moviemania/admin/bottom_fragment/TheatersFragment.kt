package com.example.moviemania.admin.bottom_fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.moviemania.R
import com.example.moviemania.admin.TheaterAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID


@Suppress("DEPRECATION")
class TheatersFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var dialogView: View
    private lateinit var videoView: VideoView
    private lateinit var frameLayout: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.admin_fragment_theaters, container, false)
    }

    companion object {
        @Volatile
        private var instance: TheatersFragment? = null
        fun newInstance():TheatersFragment? {
            return instance
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadTheaterData()
        val addTheaterButton = view.findViewById<TextView>(R.id.addTheaterTextView)
        addTheaterButton.setOnClickListener {
            showAddTheaterDialog()
        }
    }

    fun addTheaterButtonClick() {
        showAddTheaterDialog()
    }
    private fun showAddTheaterDialog() {
        if (isAdded) {
            dialogView = layoutInflater.inflate(R.layout.admin_dialog_add_theater, null)
            val uploadImageButton = dialogView.findViewById<Button>(R.id.uploadImageButtonTheater)
            val imageContainer = dialogView.findViewById<RelativeLayout>(R.id.imageContainerTheater)
            val seatColLengthEditText = dialogView.findViewById<EditText>(R.id.seatColLengthEditText)
            val seatRowLengthEditText = dialogView.findViewById<EditText>(R.id.seatRowLengthEditText)
            videoView = requireActivity().findViewById(R.id.videoViewLoadingCircleAFT)
            frameLayout = requireActivity().findViewById(R.id.frameLayoutAFT)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setTitle("Add Theater")
                .setPositiveButton("Add") { dialog, _ ->
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

                    val theaterName =
                        dialogView.findViewById<EditText>(R.id.theaterNameEditText).text.toString()
                    val theaterLocation =
                        dialogView.findViewById<EditText>(R.id.theaterLocationEditText).text.toString()
                    val imageOptionRadioGroup =
                        dialogView.findViewById<RadioGroup>(R.id.imageOptionRadioGroupTheater)
                    val selectedRadioButtonId = imageOptionRadioGroup.checkedRadioButtonId
                    val isUploadImage = selectedRadioButtonId == R.id.uploadImageRadioButtonTheater

                    val theaterImageUri = dialogView.findViewById<EditText>(R.id.theaterImageInput)
                    val selectedImageView =
                        dialogView.findViewById<ImageView>(R.id.theaterImageView)
                    val seatColLength = seatColLengthEditText.text.toString()
                    val seatRowLength = seatRowLengthEditText.text.toString()

                    if (isUploadImage) {
                        imageContainer.visibility = View.VISIBLE
                        theaterImageUri.visibility = View.GONE

                        if (theaterName.isNotBlank() && selectedImageView.drawable != null && theaterLocation.isNotBlank() && seatColLength.isNotBlank()
                            && seatRowLength.isNotBlank()) {
                            uploadImageToFirebaseStorage(selectedImageView, theaterName) {
                                addTheaterToFirestore(theaterName, it,theaterLocation,seatColLength,seatRowLength)
                            }
                        } else {
                            frameLayout.visibility = View.GONE
                            videoView.stopPlayback()
                            theaterGridView?.visibility = View.VISIBLE
                            showToast("Please enter Valid Data.")
                        }
                        dialog.dismiss()
                    } else {
                        imageContainer.visibility = View.GONE
                        theaterImageUri.visibility = View.VISIBLE
                        val imageUrl = theaterImageUri.text.toString()
                        if (theaterName.isNotBlank() && imageUrl.isNotBlank()) {
                            addTheaterToFirestore(theaterName, imageUrl,theaterLocation,seatColLength,seatRowLength)
                        } else {
                            frameLayout.visibility = View.GONE
                            videoView.stopPlayback()
                            theaterGridView?.visibility = View.VISIBLE
                            showToast("Please enter Valid Data.")
                        }
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            val imageOptionRadioGroup =
                dialogView.findViewById<RadioGroup>(R.id.imageOptionRadioGroupTheater)
            imageOptionRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                val theaterImageUri = dialogView.findViewById<EditText>(R.id.theaterImageInput)

                if (checkedId == R.id.uploadImageRadioButtonTheater) {
                    imageContainer.visibility = View.VISIBLE
                    theaterImageUri.visibility = View.GONE
                } else {
                    imageContainer.visibility = View.GONE
                    theaterImageUri.visibility = View.VISIBLE
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
                    selectedImageView.setImageURI(selectedImageURI)
                }
            }
        }
    }

    private fun uploadImageToFirebaseStorage(imageView: ImageView,theaterName: String, callback: (String) -> Unit) {
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

    private fun addTheaterToFirestore(theaterName: String, imageUri: String, theaterLocation: String,seatColLength: String,seatRowLength: String) {
        val theaterCollection = db.collection("Theaters")
        val theaterGridView = view?.findViewById<GridView>(R.id.theaterGridView)
        val colLength = seatColLength.toInt()
        val rowLength = seatRowLength.toInt()

        theaterCollection.whereEqualTo("name", theaterName.trim())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot != null && !querySnapshot.isEmpty) {
                        frameLayout.visibility = View.GONE
                        videoView.stopPlayback()
                        theaterGridView?.visibility = View.VISIBLE
                        showToast("A theater with the same name already exists.")
                    } else {
                        val initialSeatState = MutableList(colLength * rowLength) { false }
                        val theaterData = hashMapOf(
                            "name" to theaterName,
                            "imageUri" to imageUri,
                            "location" to theaterLocation,
                            "seatColnum" to seatColLength,
                            "seatRownum" to seatRowLength,
                            "seats" to initialSeatState
                        )

                        theaterCollection.add(theaterData)
                            .addOnSuccessListener {
                                showToast("Theater added successfully.")
                                frameLayout.visibility = View.GONE
                                videoView.stopPlayback()
                                loadTheaterData()
                            }
                            .addOnFailureListener { exception ->
                                frameLayout.visibility = View.GONE
                                videoView.stopPlayback()
                                theaterGridView?.visibility = View.VISIBLE
                                showToast("Error adding theater: $exception")
                            }
                    }
                } else {
                    frameLayout.visibility = View.GONE
                    videoView.stopPlayback()
                    theaterGridView?.visibility = View.VISIBLE
                    showToast("Error checking theater name: ${task.exception}")
                }
            }
    }

    private fun loadTheaterData() {
        if (isAdded) {
            val theaterCollection = db.collection("Theaters")
            val noTheaterTextView = requireActivity().findViewById<RelativeLayout>(R.id.noTheaterTextView)
            val theaterGridView = view?.findViewById<GridView>(R.id.theaterGridView)

            theaterCollection.get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        noTheaterTextView.visibility = View.VISIBLE
                        theaterGridView?.visibility = View.GONE
                    } else {
                        noTheaterTextView.visibility = View.GONE
                        theaterGridView?.visibility = View.VISIBLE

                        val theaterList = ArrayList<Theater>()

                        for (document in querySnapshot.documents) {
                            val name = document.getString("name")
                            val imageUri = document.getString("imageUri")
                            val location = document.getString("location")
                            val seatColNum = document.getString("seatColnum")?.toIntOrNull() ?: 0
                            val seatRowNum = document.getString("seatRownum")?.toIntOrNull() ?: 0
                            val seatStates = document.get("seats") as? List<Boolean> ?: emptyList()
                            if (name != null && location != null && imageUri != null && seatRowNum > 0 && seatColNum > 0) {
                                val theater = Theater(name, Uri.parse(imageUri).toString(), location, seatColNum, seatRowNum, seatStates)
                                theaterList.add(theater)
                            }
                        }

                        if (isAdded) {
                            val theaterAdapter = TheaterAdapter(requireContext(), theaterList)
                            theaterGridView?.adapter = theaterAdapter
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    showToast("Error: $exception")
                }
        }
    }

    private fun validateForm(
        theaterName: EditText,
        imageUri: EditText,
        imageView: ImageView,
        textView: TextView
    ): Boolean {
        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_custom_error)
        icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
        if (theaterName.text.trim().isEmpty()) {
            theaterName.setError("TheaterName Name is Required!",icon)
            return false
        }
        if(imageUri.text.trim().isEmpty() && imageView.drawable == null) {
            textView.visibility = View.VISIBLE
            return false
        }
        return true
    }

    data class Theater(val name: String, val imageUri: String, val theaterLocation: String,
                       val seatColLength: Int, val seatRowLength: Int, val seatStates: List<Boolean>)

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}