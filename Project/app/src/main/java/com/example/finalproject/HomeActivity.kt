package com.example.finalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid



        var email = ""
        var displayName = ""

        val user = db.collection("users").document(uid!!)
        user.get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    email = document.data!!["name"] as String;
                    findViewById<TextView>(R.id.textView).text = email + "\n" + displayName
                }
            }


        findViewById<Button>(R.id.signOutButton).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this , MainActivity::class.java))
        }
    }
}