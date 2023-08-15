package com.example.moviemania.admin.bottom_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moviemania.R


class PaymentFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.admin_fragment_payment, container, false)
    }

    companion object {
        @Volatile
        private var instance: PaymentFragment? = null
        fun newInstance():PaymentFragment? {
            return instance
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}