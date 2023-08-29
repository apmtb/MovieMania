package com.example.moviemania.user.nav_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.moviemania.R
import com.example.moviemania.user.bottom_fragment.CartFragment
import com.example.moviemania.user.bottom_fragment.MoviesFragment
import com.example.moviemania.user.bottom_fragment.UpcomingFragment
import com.example.moviemania.user.bottom_fragment.NotificationFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class HomeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view =  inflater.inflate(R.layout.user_fragment_home, container, false)

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.userBottomNavigation)
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.user_bottom_movies -> {
                    replaceFragment(MoviesFragment())
                    activity?.title = "Movies"
                }
                R.id.user_bottom_upcoming_movies -> {
                    replaceFragment(UpcomingFragment())
                    activity?.title = "Upcoming Movies"
                }
                R.id.user_bottom_notification -> {
                    replaceFragment(NotificationFragment())
                    activity?.title = "Notification"
                }
                R.id.user_bottom_cart -> {
                    replaceFragment(CartFragment())
                    activity?.title = "Cart"
                }
            }
            true
        }

        replaceFragment(MoviesFragment())
        activity?.title = "Movies"
        bottomNavigationView.selectedItemId = R.id.user_bottom_movies
        return view
    }
    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.userBottomFragment,fragment)
            .commit()
    }

}