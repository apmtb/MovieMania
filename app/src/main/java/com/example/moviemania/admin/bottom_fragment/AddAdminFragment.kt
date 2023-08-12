package com.example.moviemania.admin.bottom_fragment

import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.VideoView
import androidx.core.content.ContextCompat
import com.example.moviemania.R
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddAdminFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddAdminFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val db = FirebaseFirestore.getInstance()
    private lateinit var videoView: VideoView
    private lateinit var frameLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.admin_add_admin, container, false)
        videoView = view.findViewById(R.id.videoViewLoadingCircleNA)
        frameLayout = view.findViewById(R.id.frameLayoutNA)
        val addAdminBTN = view.findViewById<Button>(R.id.addnewadmin)
        addAdminBTN.setOnClickListener {
            val email = view.findViewById<EditText>(R.id.emailnewadmin).text.toString().trim()
            if (validateForm(email)) {
                frameLayout.visibility = View.VISIBLE
                videoView.setOnPreparedListener {
                    it.isLooping = true
                }
                val videoPath = "android.resource://" + requireContext().packageName + "/" + R.raw.circle_loading

                videoView.setVideoURI(Uri.parse(videoPath))
                videoView.setZOrderOnTop(true)

                videoView.start()
                val adminMailCollection = db.collection("AdminUsers")

                adminMailCollection.whereEqualTo("email", email.trim())
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        frameLayout.visibility = View.GONE
                        videoView.stopPlayback()
                        if (querySnapshot.isEmpty) {
                            // Email does not exist, add it to the collection
                            val data = hashMapOf("email" to email)
                            adminMailCollection.add(data)
                                .addOnSuccessListener {
                                    showToast("Admin email added successfully.")
                                }
                                .addOnFailureListener {
                                    showToast("Error adding email.")
                                }
                        } else {
                            showToast("Email already exists.")
                        }
                    }
                    .addOnFailureListener {
                        frameLayout.visibility = View.GONE
                        videoView.stopPlayback()
                        showToast("Error checking email.")
                    }
            }
        }
        // Inflate the layout for this fragment
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddAdmin.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddAdminFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun validateForm(email: String): Boolean {
        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_custom_error)
        icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)

        if (email.isEmpty()) {
            requireView().findViewById<EditText>(R.id.emailnewadmin).setError("Email is required.", icon)
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            requireView().findViewById<EditText>(R.id.emailnewadmin).setError("Invalid email address.", icon)
            return false
        }
        return true
    }
    private fun showToast(message: String){
        Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
    }
}