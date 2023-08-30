package com.example.moviemania.admin.bottom_fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.moviemania.R
import com.example.moviemania.admin.CastAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID


@Suppress("DEPRECATION")
class CastFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    lateinit var dialogView: View
    lateinit var videoView: VideoView
    lateinit var frameLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.admin_fragment_cast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCastData()
        val addCastButton = view.findViewById<TextView>(R.id.addCastTextView)
        addCastButton.setOnClickListener {
            showAddCastDialog()
        }
    }

    companion object {
        @Volatile
        private var instance: CastFragment? = null
        fun newInstance(): CastFragment? {
            return instance
        }
    }

    fun loadCastData() {
        if (isAdded) {
            val castCollection = db.collection("Casts")
            val noCastTextView = requireView().findViewById<RelativeLayout>(R.id.noCastTextView)
            val castGridView = view?.findViewById<GridView>(R.id.castGridView)
            val displaymetrics = requireContext().resources.displayMetrics
            val screenWidth = displaymetrics.widthPixels
            castGridView?.columnWidth = (screenWidth * 0.40).toInt()
            castCollection.get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        noCastTextView.visibility = View.VISIBLE
                        castGridView?.visibility = View.GONE
                    } else {
                        noCastTextView.visibility = View.GONE
                        castGridView?.visibility = View.VISIBLE


                        val castList = ArrayList<Cast>()

                        for (document in querySnapshot.documents) {
                            val name = document.getString("name")
                            val imageUri = document.getString("imageUri")

                            if (name != null && imageUri != null) {
                                val cast = Cast(name, Uri.parse(imageUri).toString())
                                castList.add(cast)
                            }
                        }

                        if (isAdded) {
                            val castAdapter = CastAdapter(requireContext(), castList)
                            castGridView?.adapter = castAdapter
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    showToast("Error : $exception")
                }
        }
    }

    private fun validateForm(
        castName: EditText,
        imageUri: EditText,
        imageView: ImageView,
        textView: TextView
    ): Boolean {
        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_custom_error)
        icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
        if (castName.text.trim().isEmpty()) {
            castName.setError("Cast Name is Required!", icon)
            return false
        }
        if (imageUri.text.trim().isEmpty() && imageView.drawable == null) {
            textView.visibility = View.VISIBLE
            return false
        }
        return true
    }

    fun addCastButtonClick() {
        showAddCastDialog()
    }

    private fun showAddCastDialog() {
        if (isAdded) {
            dialogView = layoutInflater.inflate(R.layout.admin_dialog_add_cast, null)
            val uploadImageButton = dialogView.findViewById<Button>(R.id.uploadImageButton)
            val imageContainer = dialogView.findViewById<RelativeLayout>(R.id.imageContainer)
            videoView = requireActivity().findViewById(R.id.videoViewLoadingCircleAFC)
            frameLayout = requireActivity().findViewById(R.id.frameLayoutAFC)
            val imageError = dialogView.findViewById<TextView>(R.id.imageError)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setTitle("Add Cast")
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            val selectedImageView = dialogView.findViewById<ImageView>(R.id.selectedImageView)
            val imageOptionRadioGroup =
                dialogView.findViewById<RadioGroup>(R.id.imageOptionRadioGroup)
            val imageViewLayoutParams = selectedImageView.layoutParams
            val displaymetrics = requireContext().resources.displayMetrics
            val screenHeight = displaymetrics.heightPixels
            val screenWidth = displaymetrics.widthPixels
            imageViewLayoutParams.width = (screenWidth * 0.3).toInt()
            imageViewLayoutParams.height = (screenHeight * 0.20).toInt()
            val castImageInput = dialogView.findViewById<EditText>(R.id.castImageInput)
            castImageInput.addTextChangedListener {
                if (castImageInput.text.trim().isNotEmpty()) {
                    imageError.visibility = View.GONE
                } else {
                    imageError.visibility = View.VISIBLE
                }
            }
            imageOptionRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                imageError.visibility = View.GONE
                if (checkedId == R.id.uploadImageRadioButton) {
                    imageError.text = "image is Required!"
                    imageContainer.visibility = View.VISIBLE
                    castImageInput.visibility = View.GONE
                } else {
                    imageError.text = "image URL is Required!"
                    selectedImageView.setImageResource(0)
                    imageContainer.visibility = View.GONE
                    castImageInput.visibility = View.VISIBLE
                }
            }
            uploadImageButton.setOnClickListener {
                selectImageIntent()
            }

            dialog.show()
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                val castNameEditText =
                    dialogView.findViewById<EditText>(R.id.castNameEditText)
                val castName = castNameEditText.text.toString()
                val imageOptionRadioGroup =
                    dialogView.findViewById<RadioGroup>(R.id.imageOptionRadioGroup)
                val castImageInput = dialogView.findViewById<EditText>(R.id.castImageInput)
                val selectedImageView =
                    dialogView.findViewById<ImageView>(R.id.selectedImageView)
                if (validateForm(castNameEditText, castImageInput, selectedImageView, imageError)) {
                    val castGridView = view?.findViewById<GridView>(R.id.castGridView)
                    val noCastTextView = view?.findViewById<RelativeLayout>(R.id.noCastTextView)
                    castGridView?.visibility = View.GONE
                    noCastTextView?.visibility = View.GONE
                    frameLayout.visibility = View.VISIBLE
                    videoView.setOnPreparedListener {
                        it.isLooping = true
                    }
                    val videoPath =
                        "android.resource://" + requireContext().packageName + "/" + R.raw.circle_loading

                    videoView.setVideoURI(Uri.parse(videoPath))
                    videoView.setZOrderOnTop(true)

                    videoView.start()
                    val selectedRadioButtonId = imageOptionRadioGroup.checkedRadioButtonId
                    val isUploadImage = selectedRadioButtonId == R.id.uploadImageRadioButton

                    if (isUploadImage) {
                        imageContainer.visibility = View.VISIBLE
                        castImageInput.visibility = View.GONE
                        uploadImageToFirebaseStorage(selectedImageView, castName) {
                            addCastToFirestore(castName, it)
                        }
                    } else {
                        imageContainer.visibility = View.GONE
                        castImageInput.visibility = View.VISIBLE
                        val imageUrl = castImageInput.text.toString()
                        addCastToFirestore(castName, imageUrl)
                    }
                    dialog.dismiss()
                }
            }
        }
    }

    fun selectImageIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            123
        )
    }

    @Deprecated("")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 123) {
                val selectedImageURI = data?.data
                val selectedImageView = dialogView.findViewById<ImageView>(R.id.selectedImageView)
                if (selectedImageURI != null) {
                    val imageError = dialogView.findViewById<TextView>(R.id.imageError)
                    imageError.visibility = View.GONE
                    selectedImageView.tag = "1"
                    Glide.with(requireContext()).load(selectedImageURI).centerCrop()
                        .error(R.drawable.ic_custom_error)
                        .placeholder(R.drawable.ic_image_placeholder).into(selectedImageView)
                }
            }
        }
    }

    fun uploadImageToFirebaseStorage(
        imageView: ImageView,
        castName: String,
        callback: (String) -> Unit
    ) {
        val castCollection = db.collection("Casts")
        castCollection.whereEqualTo("name", castName.trim())
            .get()
            .addOnCompleteListener { task ->
                val castGridView = view?.findViewById<GridView>(R.id.castGridView)
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    val storageRef = FirebaseStorage.getInstance().reference
                    val imagesRef = storageRef.child("cast_images/${UUID.randomUUID()}.jpg")

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
                                    castGridView?.visibility = View.VISIBLE
                                }
                            }
                        } else {
                            frameLayout.visibility = View.GONE
                            videoView.stopPlayback()
                            castGridView?.visibility = View.VISIBLE
                        }
                    }
                } else {
                    frameLayout.visibility = View.GONE
                    videoView.stopPlayback()
                    castGridView?.visibility = View.VISIBLE
                }
            }
    }

    private fun addCastToFirestore(castName: String, imageUri: String) {
        val castCollection = db.collection("Casts")
        val castGridView = view?.findViewById<GridView>(R.id.castGridView)

        castCollection.whereEqualTo("name", castName.trim())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot != null && !querySnapshot.isEmpty) {
                        frameLayout.visibility = View.GONE
                        videoView.stopPlayback()
                        castGridView?.visibility = View.VISIBLE
                        showToast("A cast with the same name already exists.")
                    } else {

                        val castData = hashMapOf(
                            "name" to castName.trim(),
                            "imageUri" to imageUri.trim()
                        )

                        castCollection.add(castData)
                            .addOnSuccessListener {
                                showToast("Cast added successfully.")
                                frameLayout.visibility = View.GONE
                                videoView.stopPlayback()
                                loadCastData() // Reload the cast data after adding a new cast
                            }
                            .addOnFailureListener { exception ->
                                frameLayout.visibility = View.GONE
                                videoView.stopPlayback()
                                castGridView?.visibility = View.VISIBLE
                                showToast("Error adding cast: $exception")
                            }
                    }
                } else {
                    frameLayout.visibility = View.GONE
                    videoView.stopPlayback()
                    castGridView?.visibility = View.VISIBLE
                    showToast("Error checking cast name: ${task.exception}")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    data class Cast(val name: String, val imageUri: String)
}