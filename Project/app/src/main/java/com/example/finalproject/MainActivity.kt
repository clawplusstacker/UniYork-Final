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
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient
    private val db = Firebase.firestore
    private val movieList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

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
//        val loadingCircle = findViewById<ProgressBar>(R.id.loadingCircle);
//        loadingCircle.visibility = View.VISIBLE;
//
//        val button = findViewById<View>(R.id.signInButton);
//        button.visibility = View.INVISIBLE;
//
        //Get movies we need first or else we can't go into the app.
        getPopularMovies();

        var stringArr : Array<String> = movieList.toTypedArray();

        if(stringArr.isNullOrEmpty()){
            return;
        }

        val intent = Intent(this , BottomNavActivity::class.java)
        intent.putExtra("movies", stringArr)
        startActivity(intent)
    }

    /**
     * Goes through JSONArray gotten from API and adds the movie
     * codes to the class variable.
     */
    fun setList(list: JSONArray) {
        for (i in 0 until list.length()) {
            val movie = list.getJSONObject(i)
            movieList.add(movie.getString("id"))
        }
    }


    fun getPopularMovies(){

        var url1 = "https://api.themoviedb.org/3/movie/popular?api_key=63d93b08a5c17f9bbb9d8205524f892f&language=en-US&page=1"
        var url2 = "https://api.themoviedb.org/3/movie/popular?api_key=63d93b08a5c17f9bbb9d8205524f892f&language=en-US&page=2"

        try{
            getDataFromServer(url1)
            getDataFromServer(url2)
        }
        catch (e: IOException){ //The getDataFromServer function can "throw" an IOException if there is an error. We have to "catch" that here, otherwise our entire app will crash.
            e.printStackTrace()
            return
        }
    }

    /**
     * Method that uses OkHTTP to connect to the server
     * @param url the URL of the remote data source
     * @throws IOException
     */
    private fun getDataFromServer(url:String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback { //This is an inner class that will be used to handle the response.

            override fun onFailure(call: Call, e: IOException) { //If there is an error in the response...
                e.printStackTrace() //Print the error to the console
            }

            override fun onResponse(call: Call, response: Response) { //If the response is good...
                response.use{
                    if (!response.isSuccessful) throw IOException ("Unexpected code $response") // Ensure that we throw an exception if response is not successful
                    readJSONFact(response.body!!.string()) //send the JSON we got from the server to the readJSONFact function.
                }
            }
        })
    }


    fun readJSONFact(rawJson:String){
        runOnUiThread(java.lang.Runnable { //This section has to happen on the same thread as the user interface.
            try {
                var json = JSONObject(rawJson)
                var list = json.getJSONArray("results")
                setList(list)
            }
            catch (e: JSONException){ //Handle any issues where the JSON is badly formed or invalid
//                setFact("Invalid JSON text")
            }
        })
    }
}