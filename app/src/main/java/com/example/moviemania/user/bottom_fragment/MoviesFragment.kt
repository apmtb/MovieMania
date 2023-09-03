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
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore


class MoviesFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var movieAdapter:UserMovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view =  inflater.inflate(R.layout.user_fragment_movies, container, false)
        val tabLayout = view.findViewById<TabLayout>(R.id.userMoviesTabLayout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                loadMoviesData(tab?.position?:0)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMoviesData(0)
    }

    private fun loadMoviesData(tabPosition: Int) {
        if (isAdded) {
            val moviesCollection = db.collection("Movies")
            val noMoviesText = requireActivity().findViewById<TextView>(R.id.noMoviesText)
            val moviesGridView = view?.findViewById<GridView>(R.id.userMoviesGridView)

            moviesCollection.get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        noMoviesText.visibility = View.VISIBLE
                        moviesGridView?.visibility = View.GONE
                    } else {
                        noMoviesText.visibility = View.GONE
                        moviesGridView?.visibility = View.VISIBLE

                        val moviesList = ArrayList<Movie>()

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
                                val movie = Movie(
                                    title, photoUri, description, section, ticketPrice,
                                    isUpcoming, language, timesList, castList, theaterList
                                )
                                if ((section == "Trending" && tabPosition == 1) ||
                                    (section == "Popular" && tabPosition == 2) ||
                                    tabPosition == 0 && !isUpcoming) {
                                    moviesList.add(movie)
                                }
                            }
                        }

                        if (isAdded) {
                            movieAdapter = UserMovieAdapter(requireContext(), moviesList)
                            moviesGridView?.adapter = movieAdapter
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    showToast("Error: $exception")
                }
        }
    }

    data class Movie(val title: String, val photoUri: String, val description: String,
                     val section: String, val ticketPrice: Double,
                     val isUpcoming: Boolean, val language: String, val timesList: List<String>,
                     val castList: List<String>, val theaterList: List<String>)

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
