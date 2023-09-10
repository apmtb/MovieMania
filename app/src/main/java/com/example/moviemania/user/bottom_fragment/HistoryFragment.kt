package com.example.moviemania.user.bottom_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.moviemania.R
import com.example.moviemania.user.Booking
import com.example.moviemania.user.BookingAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class HistoryFragment : Fragment() {

    val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.user_fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadBookingHistory()
    }

    private fun loadBookingHistory() {
        if (isAdded) {
            val auth = FirebaseAuth.getInstance()
            val currentUserEmail = auth.currentUser?.email
            val bookingsCollection = db.collection("Bookings")
            val noBookingTextView = requireView().findViewById<RelativeLayout>(R.id.noBookingTextView)
            val bookingListView = view?.findViewById<ListView>(R.id.historyListView)
            bookingsCollection.whereEqualTo("currentUserEmail", currentUserEmail)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        noBookingTextView.visibility = View.VISIBLE
                        bookingListView?.visibility = View.GONE
                    } else {
                        noBookingTextView.visibility = View.GONE
                        bookingListView?.visibility = View.VISIBLE


                        val currentUserBookingList = ArrayList<Booking>()

                        for (document in querySnapshot.documents) {
                            val transactionId = document.getString("transactionId") ?: ""
                           val movieTitle = document.getString("movieTitle") ?: ""
                           val movieImageUrl = document.getString("movieImageUrl") ?: ""
                           val date = document.getString("date") ?: ""
                           val time = document.getString("time") ?: ""
                           val language = document.getString("language") ?: ""
                           val theaterName = document.getString("theaterName") ?: ""
                           val location = document.getString("location") ?: ""
                           val bookedSeats = document.getString("bookedSeats") ?: ""
                           val method = document.getString("method") ?: ""
                           val price = document.getString("price") ?: ""
                           val tax = document.getString("tax") ?: ""
                           val totalPrice = document.getString("totalPrice")

                            if (totalPrice!=null) {
                                val booking =
                                    Booking(
                                        transactionId, movieTitle, movieImageUrl,
                                        date, time, language, theaterName, location, bookedSeats,
                                        method, price, tax, totalPrice
                                    )
                                currentUserBookingList.add(booking)
                            }
                        }

                        if (isAdded) {
                            val bookingAdapter = BookingAdapter(requireContext(), currentUserBookingList)
                            bookingListView?.adapter = bookingAdapter
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    showToast("Error : $exception")
                }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}