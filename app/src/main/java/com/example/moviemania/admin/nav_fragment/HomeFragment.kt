package com.example.moviemania.admin.nav_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.moviemania.R
import com.example.moviemania.admin.bottom_fragment.CartFragment
import com.example.moviemania.admin.bottom_fragment.MoviesFragment
import com.example.moviemania.admin.bottom_fragment.TheatersFragment
import com.example.moviemania.admin.bottom_fragment.CastFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view =  inflater.inflate(R.layout.admin_fragment_home, container, false)

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.bottom_movies -> {
                    replaceFragment(MoviesFragment())
                    activity?.title = "Movies"
                }
                R.id.bottom_theaters -> {
                    replaceFragment(TheatersFragment())
                    activity?.title = "Theaters"
                }
                R.id.bottom_cast -> {
                    replaceFragment(CastFragment())
                    activity?.title = "Cast"
                }
                R.id.bottom_cart -> {
                    replaceFragment(CartFragment())
                    activity?.title = "Cart"
                }
            }
            true
        }
        replaceFragment(MoviesFragment())
        activity?.title = "Movies"
        bottomNavigationView.selectedItemId = R.id.bottom_movies

        val addAdminBtn = view.findViewById<FloatingActionButton>(R.id.addAdminBtn)
        addAdminBtn.setOnClickListener {
            when (bottomNavigationView.selectedItemId) {
                R.id.bottom_movies -> {
                    showToast("Movies")
                }
                R.id.bottom_theaters -> {
                    showToast("theaters")
                }
                R.id.bottom_cast -> {
                    val cf = CastFragment.newInstance()
                    cf?.buttonClick()
                }
                R.id.bottom_cart -> {
                    showToast("Cart")
                }
            }
        }
        return view
    }
    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.bottomFragment,fragment)
            .commit()
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}