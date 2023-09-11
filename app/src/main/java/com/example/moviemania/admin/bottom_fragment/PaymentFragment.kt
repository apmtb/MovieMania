package com.example.moviemania.admin.bottom_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.moviemania.R
import com.example.moviemania.admin.PaymentAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class PaymentFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        instance = this
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.admin_fragment_payment, container, false)
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
            val noBookingTextView = requireView().findViewById<RelativeLayout>(R.id.noBookingTextViewAdmin)
            val bookingListView = view?.findViewById<ListView>(R.id.historyListViewAdmin)
            bookingsCollection.get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        noBookingTextView.visibility = View.VISIBLE
                        bookingListView?.visibility = View.GONE
                    } else {
                        noBookingTextView.visibility = View.GONE
                        bookingListView?.visibility = View.VISIBLE


                        val currentUserBookingList = ArrayList<Booking>()

                        for (document in querySnapshot.documents) {
                            val email = document.getString("currentUserEmail") ?: ""
                            val transactionId = document.getString("transactionId") ?: ""
                            val movieTitle = document.getString("movieTitle") ?: ""
                            val movieImageUrl = document.getString("movieImageUrl") ?: ""
                            val bookedOn = document.getString("bookedOn") ?: ""
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
                                        email, transactionId, movieTitle, movieImageUrl, bookedOn,
                                        date, time, language, theaterName, location, bookedSeats,
                                        method, price, tax, totalPrice
                                    )
                                currentUserBookingList.add(booking)
                            }
                        }

                        if (isAdded) {
                            val paymentAdapter = PaymentAdapter(requireContext(), currentUserBookingList)
                            bookingListView?.adapter = paymentAdapter
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

//    companion object {
//        @Volatile
//        private var instance: PaymentFragment? = null
//        fun newInstance():PaymentFragment? {
//            return instance
//        }
//    }
//    override fun onDestroy() {
//        super.onDestroy()
//        instance = null
//    }
}
data class Booking(
    val email: String,
    val transactionId: String,
    val movieTitle: String,
    val movieImageUrl: String,
    val bookedOn: String,
    val date: String,
    val time: String,
    val language: String,
    val theaterName: String,
    val location: String,
    val bookedSeats: String,
    val method: String,
    val price: String,
    val tax: String,
    val totalPrice: String
)