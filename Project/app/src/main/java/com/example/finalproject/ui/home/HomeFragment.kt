package com.example.finalproject.ui.home

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.finalproject.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class HomeFragment : Fragment() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var currentMovieID = "634649";

    private val currentUser = db.collection("users").document(auth.currentUser!!.uid)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //Get first movie when we create the view.
        GlobalScope.launch(Dispatchers.Main) {
            getMovie()
        }

        binding.likeButton.setOnClickListener{

            currentUser.update("moviesLiked", FieldValue.arrayUnion(currentMovieID))

            GlobalScope.launch(Dispatchers.Main) {
                getMovie()
            }

        }
        binding.dislikeButton.setOnClickListener{

            currentUser.update("moviesDisliked", FieldValue.arrayUnion(currentMovieID))

            GlobalScope.launch(Dispatchers.Main) {
                getMovie()
            }
        }


        return root

    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }


    /**
     * Gets list of all the movies the user has seen
     */
    private suspend fun getUserList(): MutableList<String> {
        var userList = mutableListOf<String>()

            currentUser.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userList.addAll(document["moviesLiked"] as Collection<String>)
                        userList.addAll(document["moviesDisliked"] as Collection<String>)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }.await()

        return userList;
    }


    /**
     * Gets what movie the User should see.
     */
    @SuppressLint("SetTextI18n")
    private suspend fun getMovie() {

        binding.movieTitle.text = "Loading..."

        val userList = getUserList();
        val movieList = activity?.intent?.getStringArrayExtra("movies")

        var count = 0;
        if (movieList != null) {
            for(movie in movieList){
                if(!userList.contains(movie)){
                    currentMovieID = movie;
                    break;
                }
                count+=1;
            }
        }
        if(count > 39){
            setNoMovieLeft();
            return
        }

        var url = "https://api.themoviedb.org/3/movie/${currentMovieID}?api_key=63d93b08a5c17f9bbb9d8205524f892f&language=en-US"

        try {
            getDataFromServer(url) //Here we call the function that will connect to the server
        } catch (e: IOException) { //The getDataFromServer function can "throw" an IOException if there is an error. We have to "catch" that here, otherwise our entire app will crash.
            binding.movieTitle.text = "Server Error!"
            return
        }
    }


    /**
     * Sets UI based on the current movie.
     */
    @SuppressLint("SetTextI18n")
    private fun setMovie(movie: Map<String, Any>) {

        //Getting genres through JSONArray passed
        fun setList(list: JSONArray): String {
            var genres = "";
            for (i in 0 until list.length()) {
                val movie = list.getJSONObject(i)
                genres += movie.getString("name") + ", "
            }
            return genres.substring(0, genres.length-2);
        }


        binding.movieTitle.text = movie["title"].toString()
        binding.movieOverview.text = movie["overview"].toString();
        binding.movieRating.rating = movie["rating"].toString().toFloat() / 2
        binding.movieYear.text = "(${movie["release_date"].toString().substring(0, 4)})"
        binding.movieDetails.text =
            "${movie["release_date"].toString()} - (${movie["country"].toString()}) - ${movie["runtime"]}min\n" +
                    setList(movie["genres"] as JSONArray)

        var imageUrl = "https://image.tmdb.org/t/p/w500/" + movie["poster"]
        var backUrl = "https://image.tmdb.org/t/p/w500/" + movie["backdrop"]

        Glide.with(activity)
            .load(backUrl)
            .into(binding.movieBanner);

        Glide.with(activity)
            .load(imageUrl)
            .into(binding.moviePoster);
    }

    /**
     * Method that uses OkHTTP to connect to the server
     * @param url the URL of the remote data source
     * @throws IOException
     */
    private fun getDataFromServer(url: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback { //This is an inner class that will be used to handle the response.

            override fun onFailure(call: Call, e: IOException) { //If there is an error in the response...
                e.printStackTrace() //Print the error to the console
            }

            override fun onResponse(
                call: Call,
                response: Response
            ) { //If the response is good...
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response") // Ensure that we throw an exception if response is not successful
                    readJSONMovie(response.body!!.string()) //send the JSON we got from the server to the readJSONFact function.
                }
            }
        })
    }

    /**
     * Method that takes a string containing JSON, and extracts the movie from API.
     * @param rawJson A string containing JSON encoded daa
     */
    fun readJSONMovie(rawJson: String) {
        activity?.runOnUiThread(java.lang.Runnable { //This section has to happen on the same thread as the user interface.
            try {
                var json = JSONObject(rawJson)

                var movie = mapOf(
                    "title" to json.getString("title"),
                    "overview" to json.getString("overview"),
                    "rating" to json.getString("vote_average"),
                    "release_date" to json.getString("release_date"),
                    "poster" to json.getString("poster_path"),
                    "backdrop" to json.getString("backdrop_path"),
                    "country" to json.getJSONArray("production_countries").getJSONObject(0).getString("iso_3166_1"),
                    "genres" to json.getJSONArray("genres"),
                    "runtime" to json.getString("runtime")
                )
                setMovie(movie)

            } catch (e: JSONException) {
                binding.movieTitle.text = "INVALID JSON TEXT"
            }
        })
    }

    /**
     * Change UI if the user has run out of movies
     */
    private fun setNoMovieLeft(){

        binding.movieTitle.text = "There are no movies left :("
        binding.movieOverview.text = "Check back tomorrow for updated popular movies.";
        binding.movieRating.rating = 0F
        binding.movieYear.text = ""
        binding.movieDetails.text = ""

        Glide.with(activity)
            .load("")
            .into(binding.movieBanner);

        Glide.with(activity)
            .load("")
            .into(binding.moviePoster)
    }
}

