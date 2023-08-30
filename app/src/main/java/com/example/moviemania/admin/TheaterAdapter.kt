package com.example.moviemania.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.example.moviemania.R
import com.example.moviemania.admin.bottom_fragment.TheatersFragment
import com.google.firebase.firestore.FirebaseFirestore


class TheaterAdapter(private val context: Context, private val theaters: List<TheatersFragment.Theater>) : BaseAdapter() {

    private val db = FirebaseFirestore.getInstance()

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

        theaterDeleteButton.setOnClickListener {
            deleteTheater(theater.name)
        }

        Glide.with(context)
            .load(theater.imageUri)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_custom_error)
            .centerCrop()
            .into(theaterImageView)

        view.setOnClickListener {
            showTheaterDetailsDialog(theater)
        }

        return view
    }
    private fun showTheaterDetailsDialog(theater: TheatersFragment.Theater) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_theater_details, null)

        val theaterImageView: ImageView = dialogView.findViewById(R.id.theaterDetailsImageView)
        val theaterNameTextView: TextView = dialogView.findViewById(R.id.theaterDetailsNameTextView)
        val theaterLocationTextView: TextView = dialogView.findViewById(R.id.theaterDetailsLocationTextView)
        val selectSeatsButton: Button = dialogView.findViewById(R.id.selectSeatsButton)

        Glide.with(context)
            .load(theater.imageUri)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_custom_error)
            .centerCrop()
            .into(theaterImageView)

        theaterNameTextView.text = theater.name
        theaterLocationTextView.text = theater.theaterLocation

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Theater Details")
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        selectSeatsButton.setOnClickListener {
            showSeatSelectionDialog(theater)
        }

        dialog.show()
    }

    private fun showSeatSelectionDialog(theater: TheatersFragment.Theater) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_seat_selection, null)
        val seatGridView: GridView = dialogView.findViewById(R.id.seatGridView)
        val mutableSeatList:MutableList<Int> = mutableListOf()

        // Calculate numRows and numColumns based on theater's seatRowLength and seatColumnLength
        val numRows = theater.seatRowLength
        val numColumns = theater.seatColLength

        seatGridView.numColumns = numColumns + 1
        val seatDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Select Seats")
            .setPositiveButton("Book",null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        val theaterName = theater.name

        loadSeatData(theaterName, numColumns) { seatList ->
            val seatAdapter = SeatAdapter(context, seatList, numRows, numColumns+1) { list ->
                mutableSeatList.clear()
                mutableSeatList.addAll(list)
            }
            seatGridView.adapter = seatAdapter
        }

        seatDialog.show()
        val bookButton = seatDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        bookButton.setOnClickListener {
            if (mutableSeatList.isNotEmpty()) {
                updateSeatStatus(theaterName, mutableSeatList, true) { status ->
                    if (status) {
                        Toast.makeText(context, "Booked Successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Booking failed, Please try again later!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(context, "Select atleast one seat to book!", Toast.LENGTH_SHORT).show()
            }
            seatDialog.dismiss()
        }
    }

    private fun loadSeatData(theaterName: String, numColumns: Int, callback: (List<Seat>) -> Unit) {
        val theaterCollection = db.collection("Theaters")

        theaterCollection.whereEqualTo("name", theaterName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val seatGrid = documentSnapshot.get("seats") as? List<Boolean>
                    val seatList = mutableListOf<Seat>()
                    seatGrid?.forEachIndexed { index, isSelected ->
                        val seatId = "Seat_${index + 1}" // Adjust the seat ID creation if needed
                        val row = index / numColumns + 1
                        val column = index % numColumns + 1
                        seatList.add(Seat(seatId, column, row, isSelected))
                        if (numColumns/2 == column){
                            seatList.add(Seat("", column, row, isSelected))
                        }
                    }
                    callback(seatList)
                }
            }
            .addOnFailureListener { e ->
                // Handle error
            }
    }


    private fun updateSeatStatus(theaterName: String, seatPositions: List<Int>, isSelected: Boolean, callback: (Boolean) -> Unit) {
        val theaterCollection = db.collection("Theaters")
        theaterCollection.whereEqualTo("name", theaterName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val theaterRef = document.reference

                    val seatGrid = document.get("seats") as? MutableList<Boolean>
                    seatGrid?.let {
                        for (seatPosition in seatPositions) {
                            if (seatPosition >= 0 && seatPosition < it.size) {
                                it[seatPosition] = isSelected
                            }
                        }

                        theaterRef.update("seats", seatGrid)
                            .addOnSuccessListener {
                                // Successfully updated
                                callback(true)
                            }
                            .addOnFailureListener { e ->
                                // Handle error
                                callback(false)
                            }
                    }
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
                            val theaterId = documentSnapshot.id

                            theatersCollection.document(theaterId)
                                .delete()
                                .addOnSuccessListener {
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

    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    data class Seat(
        val id: String,
        val column: Int,
        val row: Int,
        val isSelected: Boolean,
    )
}