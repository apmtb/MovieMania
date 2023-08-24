package com.example.moviemania.admin.nav_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.moviemania.R
import com.example.moviemania.admin.bottom_fragment.PaymentFragment
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

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.adminBottomNavigation)
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.admin_bottom_movies -> {
                    replaceFragment(MoviesFragment())
                    activity?.title = "Movies"
                }
                R.id.admin_bottom_theaters -> {
                    replaceFragment(TheatersFragment())
                    activity?.title = "Theaters"
                }
                R.id.admin_bottom_cast -> {
                    replaceFragment(CastFragment())
                    activity?.title = "Cast"
                }
                R.id.admin_bottom_payment -> {
                    replaceFragment(PaymentFragment())
                    activity?.title = "Payment"
                }
            }
            true
        }
        bottomNavigationView.selectedItemId = R.id.admin_bottom_movies

        val addAdminBtn = view.findViewById<FloatingActionButton>(R.id.adminPlusBtn)
        addAdminBtn.setOnClickListener {
            when (bottomNavigationView.selectedItemId) {
                R.id.admin_bottom_movies -> {
                    val mf = MoviesFragment.newInstance()
                    mf?.addMovieButtonClick()
                }
                R.id.admin_bottom_theaters -> {
                    val tf = TheatersFragment.newInstance()
                    tf?.addTheaterButtonClick()
                }
                R.id.admin_bottom_cast -> {
                    val cf = CastFragment.newInstance()
                    cf?.addCastButtonClick()
                }
                R.id.admin_bottom_payment -> {
                    showToast("Payment")
                }
            }
        }
        return view
    }
    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.adminBottomFragment,fragment)
            .commit()
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}