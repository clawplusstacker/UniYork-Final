package com.example.finalproject.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.finalproject.CameraActivity
import com.example.finalproject.MainActivity
import com.example.finalproject.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()


        //New Profile Picture Button
        binding.newProfilePictureButton.setOnClickListener {
            //Starts Camera Activity
            val intent = Intent(activity, CameraActivity::class.java)
            startActivity(intent)
        }


        //Reset Account Button
        binding.resetButton.setOnClickListener {
            //Add Rest Here
        }

        //Sign out button
        binding.signOutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        binding.nameView.text = auth.currentUser?.displayName ?: "..."
        binding.emailView.text = auth.currentUser?.email ?: "..."

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}