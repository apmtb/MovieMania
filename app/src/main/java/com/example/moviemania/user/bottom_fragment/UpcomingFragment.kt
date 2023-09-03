package com.example.moviemania.user.bottom_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import com.example.moviemania.R
import com.example.moviemania.user.UserMovieAdapter
import com.google.firebase.firestore.FirebaseFirestore

class UpcomingFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.user_fragment_upcoming, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMoviesData()
    }

    private fun loadMoviesData() {
        if (isAdded) {
            val moviesCollection = db.collection("Movies")
            val noMoviesText = requireActivity().findViewById<TextView>(R.id.noMoviesText)
            val upcomingMoviesGridView = view?.findViewById<GridView>(R.id.upcomingMoviesGridView)

            moviesCollection.get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        noMoviesText.visibility = View.VISIBLE
                        upcomingMoviesGridView?.visibility = View.GONE
                    } else {
                        noMoviesText.visibility = View.GONE
                        upcomingMoviesGridView?.visibility = View.VISIBLE

                        val moviesList = ArrayList<MoviesFragment.Movie>()

                        for (document in querySnapshot.documents) {
                            val title = document.getString("title")
                            val photoUri = document.getString("photoUri")
                            val description = document.getString("description")
                            val section = document.getString("section")
                            val ticketPrice = document.getDouble("ticketPrice") ?: 0.0
                            val isUpcoming = document.getBoolean("isUpcoming") ?: false
                            val language = document.getString("language")
                            val timesList = document.get("times") as? List<String> ?: emptyList()
                            val castList = document.get("castList") as? List<String> ?: emptyList()
                            val theaterList = document.get("theaterList") as? List<String> ?: emptyList()

                            if (title != null && photoUri != null && description != null &&
                                section != null && language != null) {
                                val movie = MoviesFragment.Movie(
                                    title, photoUri, description, section, ticketPrice,
                                    isUpcoming, language, timesList, castList, theaterList
                                )
                                if(isUpcoming){
                                    moviesList.add(movie)
                                }
                            }
                        }

                        if (isAdded) {
                            val movieAdapter = UserMovieAdapter(requireContext(), moviesList)
                            upcomingMoviesGridView?.adapter = movieAdapter
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    showToast("Error: $exception")
                }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}