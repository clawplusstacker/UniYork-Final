package com.example.finalproject.ui.notifications

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.finalproject.CameraActivity
import com.example.finalproject.MainActivity
import com.example.finalproject.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    private val binding get() = _binding!!
    private var auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val currentUser = db.collection("users").document(auth.currentUser!!.uid)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //New Profile Picture Button
        binding.newProfilePictureButton.setOnClickListener {
            //Starts Camera Activity
            val intent = Intent(activity, CameraActivity::class.java)
            startActivity(intent)
        }


        //Resets Account
        fun reset(){
            val newList = mutableListOf<String>()
            currentUser.update("moviesLiked", newList)
            currentUser.update("moviesDisliked", newList)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Successfully Reset Account!", Toast.LENGTH_SHORT).show()
                }
        }

        //Reset Account Button
        binding.resetButton.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(activity)
            dialogBuilder.setMessage("Are you sure you want to reset your account? This will clear all your likes and dislikes.")

                .setPositiveButton("Reset Account", DialogInterface.OnClickListener {
                    dialog, id -> reset()
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                })

            val alert = dialogBuilder.create()
            alert.setTitle("Reset Account")
            alert.show()
        }

        //Sign out button
        binding.signOutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        binding.nameViewLarge.text = auth.currentUser?.displayName ?: "..."
        binding.nameView.text = auth.currentUser?.displayName ?: "..."
        binding.emailView.text = auth.currentUser?.email ?: "..."


        //Profile Picture
        currentUser.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Glide.with(activity)
                        .load(document["photoURL"])
                        .into(binding.profilePicture);
                }
            }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}