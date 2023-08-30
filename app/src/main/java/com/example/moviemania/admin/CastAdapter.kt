package com.example.moviemania.admin


import android.content.Context
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
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
import com.example.moviemania.admin.bottom_fragment.CastFragment
import com.google.firebase.firestore.FirebaseFirestore

class CastAdapter(private val context: Context, private val castList: List<CastFragment.Cast>) :
    BaseAdapter() {

    private val db = FirebaseFirestore.getInstance()
    var dataChanged = false

    override fun getCount(): Int = castList.size

    override fun getItem(position: Int): Any = castList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val cast = getItem(position) as CastFragment.Cast
        val itemView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.grid_item_cast, parent, false)
        val castImageView = itemView.findViewById<ImageView>(R.id.castImageView)
        val castNameTextView = itemView.findViewById<TextView>(R.id.castNameTextView)
        val castEditButton = itemView.findViewById<Button>(R.id.editButtonCast)
        val castDeleteButton = itemView.findViewById<Button>(R.id.deleteButtonCast)
        castNameTextView.text = cast.name
        val imageViewLayoutParams = castImageView.layoutParams
        val displaymetrics = context.resources.displayMetrics
        val screenHeight = displaymetrics.heightPixels
        val screenWidth = displaymetrics.widthPixels
        imageViewLayoutParams.width = (screenWidth * 0.3).toInt()
        imageViewLayoutParams.height = (screenHeight * 0.20).toInt()
        val uri = cast.imageUri
        castEditButton.setOnClickListener {
            showUpdateCastDialog(cast.name, uri)
        }
        castDeleteButton.setOnClickListener {
            deleteCast(cast.name)
        }
        Glide.with(context).load(uri).centerCrop().error(R.drawable.ic_custom_error)
            .placeholder(R.drawable.ic_image_placeholder).into(castImageView)
        return itemView
    }

    private fun validateForm(
        castName: EditText,
        imageUri: EditText,
        imageView: ImageView,
        imageError: TextView,
        noDataChangedError: TextView
    ): Boolean {
        val icon = ContextCompat.getDrawable(context, R.drawable.ic_custom_error)
        icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
        if (castName.text.trim().isEmpty()) {
            castName.setError("Cast Name is Required!", icon)
            return false
        }
        if (imageUri.text.trim().isEmpty() && imageView.drawable == null) {
            imageError.visibility = View.VISIBLE
            return false
        }
        if (!isDataChanged()){
            noDataChangedError.visibility = View.VISIBLE
            return false
        }
        return true
    }

    private fun isDataChanged(): Boolean {
        return dataChanged
    }

    private fun showUpdateCastDialog(currentName: String, currentImageUrl: String) {
        val cf = CastFragment.newInstance()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.admin_dialog_add_cast, null)
        cf?.dialogView = dialogView
        cf?.videoView = cf?.requireActivity()!!.findViewById(R.id.videoViewLoadingCircleAFC)
        cf.frameLayout = cf.requireActivity().findViewById(R.id.frameLayoutAFC)
        val uploadImageButton = dialogView.findViewById<Button>(R.id.uploadImageButton)
        val imageContainer = dialogView.findViewById<RelativeLayout>(R.id.imageContainer)
        val imageError = dialogView.findViewById<TextView>(R.id.imageError)
        val noDataChangedError = dialogView.findViewById<TextView>(R.id.noDataChangedError)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Update Cast")
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        val selectedImageView = dialogView.findViewById<ImageView>(R.id.selectedImageView)
        val imageOptionRadioGroup =
            dialogView.findViewById<RadioGroup>(R.id.imageOptionRadioGroup)
        val castImageInput = dialogView.findViewById<EditText>(R.id.castImageInput)
        val castNameEditText = dialogView.findViewById<EditText>(R.id.castNameEditText)
        val imageViewLayoutParams = selectedImageView.layoutParams
        val displaymetrics = context.resources.displayMetrics
        val screenHeight = displaymetrics.heightPixels
        val screenWidth = displaymetrics.widthPixels
        imageViewLayoutParams.width = (screenWidth * 0.3).toInt()
        imageViewLayoutParams.height = (screenHeight * 0.20).toInt()
        castNameEditText.setText(currentName)
        castImageInput.setText(currentImageUrl)
        Glide.with(context).load(currentImageUrl).centerCrop().error(R.drawable.ic_custom_error)
            .placeholder(R.drawable.ic_image_placeholder).into(selectedImageView)
        castNameEditText.addTextChangedListener {
            dataChanged = castNameEditText.text.trim().toString() != currentName
        }
        castImageInput.addTextChangedListener {
            if (castImageInput.text.trim().isNotEmpty()) {
                imageError.visibility = View.GONE
            } else {
                imageError.visibility = View.VISIBLE
            }
            dataChanged = castImageInput.text.trim().toString() != currentImageUrl
        }
        imageOptionRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            imageError.visibility = View.GONE
            if (checkedId == R.id.uploadImageRadioButton) {
                imageError.text = "image is Required!"
                imageContainer.visibility = View.VISIBLE
                castImageInput.visibility = View.GONE
            } else {
                imageError.text = "image URL is Required!"
                imageContainer.visibility = View.GONE
                castImageInput.visibility = View.VISIBLE
            }
        }
        selectedImageView.tag = "0"
        uploadImageButton.setOnClickListener {
            cf.selectImageIntent()
        }

        dialog.show()
        val updateButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        updateButton.setOnClickListener {
            if (selectedImageView.tag == "1") {
                dataChanged = true
            }
            if (validateForm(castNameEditText, castImageInput, selectedImageView, imageError, noDataChangedError)) {
                val castGridView = cf.view?.findViewById<GridView>(R.id.castGridView)
                castGridView?.visibility = View.GONE
                cf.frameLayout.visibility = View.VISIBLE
                cf.videoView.setOnPreparedListener {
                    it.isLooping = true
                }
                val videoPath =
                    "android.resource://" + cf.requireContext().packageName + "/" + R.raw.circle_loading

                cf.videoView.setVideoURI(Uri.parse(videoPath))
                cf.videoView.setZOrderOnTop(true)

                cf.videoView.start()
                val newCastName = castNameEditText.text.toString()
                val selectedRadioButtonId = imageOptionRadioGroup.checkedRadioButtonId
                val isUploadImage = selectedRadioButtonId == R.id.uploadImageRadioButton

                if (isUploadImage) {
                    imageContainer.visibility = View.VISIBLE
                    castImageInput.visibility = View.GONE
                    cf.uploadImageToFirebaseStorage(selectedImageView, currentName) {
                        updateCastData(currentName, newCastName, it)
                    }
                } else {
                    imageContainer.visibility = View.GONE
                    castImageInput.visibility = View.VISIBLE
                    val imageUrl = castImageInput.text.toString()
                    updateCastData(currentName, newCastName, imageUrl)
                }
                dialog.dismiss()
            }
        }
    }

    private fun updateCastData(castName: String, newName: String, newImageUrl: String) {
        val cf = CastFragment.newInstance()
        val castsCollection = db.collection("Casts")

        castsCollection.whereEqualTo("name", castName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val castDocument = querySnapshot.documents[0]
                val castRef = castDocument.reference

                val updateData = mutableMapOf<String, Any>()
                updateData["name"] = newName
                updateData["imageUri"] = newImageUrl

                castRef.update(updateData)
                    .addOnSuccessListener {
                        cf?.frameLayout?.visibility = View.GONE
                        cf?.videoView?.stopPlayback()
                        cf?.loadCastData()
                        showToast("Cast updated successfully")
                    }
                    .addOnFailureListener { e ->
                        cf?.frameLayout?.visibility = View.GONE
                        cf?.videoView?.stopPlayback()
                        val castGridView = cf?.view?.findViewById<GridView>(R.id.castGridView)
                        castGridView?.visibility = View.VISIBLE
                        showToast("Error updating cast: ${e.message}")
                    }
            }
    }

    private fun deleteCast(castName: String) {
        val cf =CastFragment.newInstance()
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Delete Cast")
            .setMessage(HtmlCompat.fromHtml("<br/><b>Are you sure,</b> you want to delete<br/><br/> $castName?<br/>",HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setPositiveButton("Yes") { _, _ ->
                // Delete the cast from Firestore
                val castCollection = db.collection("Casts")
                castCollection.whereEqualTo("name", castName)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val documentSnapshot = querySnapshot.documents[0]
                            val castId = documentSnapshot.id

                            castCollection.document(castId)
                                .delete()
                                .addOnSuccessListener {
                                    showToast("$castName deleted successfully!")
                                    cf?.loadCastData()
                                }
                                .addOnFailureListener { e ->
                                    showToast("Error deleting $castName: ${e.message}")
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        showToast("Error retrieving cast: ${e.message}")
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