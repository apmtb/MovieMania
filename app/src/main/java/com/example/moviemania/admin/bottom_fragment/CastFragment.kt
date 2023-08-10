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
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.moviemania.R
import com.example.moviemania.admin.CastAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CastFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@Suppress("DEPRECATION")
class CastFragment : Fragment(), CastAdapter.OnAddButtonClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var dialogView: View
    private lateinit var videoView: VideoView
    private lateinit var frameLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NotificationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CastFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun loadCastData() {
        val castCollection = db.collection("Casts")
        val noCastTextView = requireActivity().findViewById<RelativeLayout>(R.id.noCastTextView)
        val castGridView = view?.findViewById<GridView>(R.id.castGridView)
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
                    val drawableCast = Cast("", "")
                    castList.add(drawableCast)

                    val castAdapter = CastAdapter(requireContext(), castList, this)
                    castGridView?.adapter = castAdapter
                }
            }
            .addOnFailureListener { exception ->
                showToast("Error : $exception")
            }
    }

    override fun onAddButtonClick() {
        showAddCastDialog()
    }

    private fun showAddCastDialog() {
        dialogView = layoutInflater.inflate(R.layout.admin_dialog_add_cast, null)
        val uploadImageButton = dialogView.findViewById<Button>(R.id.uploadImageButton)
        val imageContainer = dialogView.findViewById<RelativeLayout>(R.id.imageContainer)
        videoView = requireActivity().findViewById(R.id.videoViewLoadingCircleAFC)
        frameLayout = requireActivity().findViewById(R.id.frameLayoutAFC)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Cast")
            .setPositiveButton("Add") { dialog, _ ->
                val castGridView = view?.findViewById<GridView>(R.id.castGridView)
                castGridView?.visibility = View.GONE
                frameLayout.visibility = View.VISIBLE
                videoView.setOnPreparedListener {
                    it.isLooping = true
                }
                val videoPath = "android.resource://" + requireContext().packageName + "/" + R.raw.circle_loading

                videoView.setVideoURI(Uri.parse(videoPath))
                videoView.setZOrderOnTop(true)

                videoView.start()
                val castName =
                    dialogView.findViewById<EditText>(R.id.castNameEditText).text.toString()
                val imageOptionRadioGroup =
                    dialogView.findViewById<RadioGroup>(R.id.imageOptionRadioGroup)
                val selectedRadioButtonId = imageOptionRadioGroup.checkedRadioButtonId
                val isUploadImage = selectedRadioButtonId == R.id.uploadImageRadioButton

                val castImageInput = dialogView.findViewById<EditText>(R.id.castImageInput)
                val selectedImageView = dialogView.findViewById<ImageView>(R.id.selectedImageView)

                if (isUploadImage) {
                    imageContainer.visibility = View.VISIBLE
                    castImageInput.visibility = View.GONE


                    if (castName.isNotBlank() && selectedImageView.drawable != null) {
                        uploadImageToFirebaseStorage(selectedImageView,castName) {
                            addCastToFirestore(castName, it)
                        }
                    } else {
                        showToast("Please enter cast name and image.")
                    }
                    dialog.dismiss()
                } else {
                    imageContainer.visibility = View.GONE
                    castImageInput.visibility = View.VISIBLE
                    val imageUrl = castImageInput.text.toString()
                    if (castName.isNotBlank() && imageUrl.isNotBlank()) {
                        addCastToFirestore(castName, imageUrl)
                    } else {
                        showToast("Please enter cast name and image.")
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        val imageOptionRadioGroup = dialogView.findViewById<RadioGroup>(R.id.imageOptionRadioGroup)
        imageOptionRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val castImageInput = dialogView.findViewById<EditText>(R.id.castImageInput)

            if (checkedId == R.id.uploadImageRadioButton) {
                imageContainer.visibility = View.VISIBLE
                castImageInput.visibility = View.GONE
            } else {
                imageContainer.visibility = View.GONE
                castImageInput.visibility = View.VISIBLE
            }
        }
        uploadImageButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                123
            )
        }

        dialog.show()
    }

    @Deprecated("")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 123) {
                val selectedImageURI = data?.data
                val selectedImageView = dialogView.findViewById<ImageView>(R.id.selectedImageView)
                if (selectedImageURI != null) {
                    selectedImageView.setImageURI(selectedImageURI)
                }
            }
        }
    }

    private fun uploadImageToFirebaseStorage(imageView: ImageView,castName: String, callback: (String) -> Unit) {
        val castCollection = db.collection("Casts")
        castCollection.whereEqualTo("name", castName)
            .get()
            .addOnCompleteListener { task ->
                val castGridView = view?.findViewById<GridView>(R.id.castGridView)
                if (task.isSuccessful) {
                    val querySnapshot = task.result
                    if (querySnapshot != null && !querySnapshot.isEmpty) {
                        // Cast name already exists, show an error message
                        frameLayout.visibility = View.GONE
                        videoView.stopPlayback()
                        castGridView?.visibility = View.VISIBLE
                        showToast("A cast with the same name already exists.")
                    } else {
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

        castCollection.whereEqualTo("name", castName)
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
                            "name" to castName,
                            "imageUri" to imageUri
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

    data class Cast(val name: String, val imageUri: String)
}