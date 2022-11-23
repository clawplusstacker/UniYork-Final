package com.example.finalproject

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient : GoogleSignInClient
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //Bypasses lockscreen if user is already signed in
        if(auth.currentUser != null){
            goHome();
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this , gso)

        findViewById<View>(R.id.signInButton).setOnClickListener {
            signInGoogle()
        }
    }


    //Starts Google Sign In Intent
    private fun signInGoogle(){
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    //Gets results from launching Google Sign in and sends them to handler
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    //Make sure Google Sign In Worked and sends to DB
    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if (account != null){

                addUserToDB(account)

                val loadingCircle = findViewById<ProgressBar>(R.id.loadingCircle);
                loadingCircle.visibility = View.VISIBLE;

                val button = findViewById<View>(R.id.signInButton);
                button.visibility = View.INVISIBLE;

            }
        }else{
            Toast.makeText(this, task.exception.toString() , Toast.LENGTH_SHORT).show()
        }
    }


    //Adds user to Firestore DB if they didn't exist then just sends them home
    private fun addUserToDB(account: GoogleSignInAccount){

        val credential = GoogleAuthProvider.getCredential(account.idToken , null)
        auth.signInWithCredential(credential).addOnCompleteListener {

            if (it.isSuccessful){

                val user = db.collection("users").document(auth.currentUser!!.uid)
                user.get()
                    .addOnSuccessListener { document ->
                        if (document.data != null) {
                            //If user exists, go to home screen
                            goHome()
                        } else {
                            val newUser = hashMapOf(
                                "name" to account.displayName,
                                "email" to account.email,
                                "photoURL" to account.photoUrl,
                                "moviesLiked" to mutableListOf<String>(),
                                "moviesDisliked" to mutableListOf<String>()
                                )

                            //Create document if User doesn't already exist
                            user
                                .set(newUser)
                                .addOnSuccessListener { goHome() }
                                .addOnFailureListener {Toast.makeText(this, "Error Creating User" , Toast.LENGTH_SHORT).show()}
                        }
                    }

            }else{
                Toast.makeText(this, it.exception.toString() , Toast.LENGTH_SHORT).show()

                val loadingCircle = findViewById<ProgressBar>(R.id.loadingCircle);
                loadingCircle.visibility = View.INVISIBLE;

                val button = findViewById<View>(R.id.signInButton);
                button.visibility = View.VISIBLE;
            }
        } //End of Auth
    }//End of function


    private fun goHome() {
        val intent = Intent(this , BottomNavActivity::class.java)
        startActivity(intent)
    }

}