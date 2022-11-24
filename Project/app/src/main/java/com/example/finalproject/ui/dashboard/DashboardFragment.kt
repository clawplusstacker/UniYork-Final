package com.example.finalproject.ui.dashboard

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.finalproject.MoviePreviewActivity
import com.example.finalproject.databinding.FragmentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class DashboardFragment : Fragment() {

private var _binding: FragmentDashboardBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var moviesLiked = mutableListOf<Map<String, Any>>()

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = db.collection("users").document(auth.currentUser!!.uid)
    private lateinit var  photoAdapter: LikedAdapter


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {


        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root


        GlobalScope.launch(Dispatchers.Main) {
            setMovieList()
        }


        //Get random movie
        binding.shuffleButton.setOnClickListener{

            if(moviesLiked.size > 0) {

                var data = moviesLiked.get((0..moviesLiked.size - 1).random());

                var intent = Intent(this.context, MoviePreviewActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("title", data["title"].toString())
                intent.putExtra("overview", data["overview"].toString())
                intent.putExtra("rating", data["rating"].toString())
                intent.putExtra("release_date", data["release_date"].toString())
                intent.putExtra("poster", data["poster"].toString())
                intent.putExtra("backdrop", data["backdrop"].toString())
                intent.putExtra("country", data["country"].toString())
                intent.putExtra("genres", data["genres"].toString())
                intent.putExtra("runtime", data["runtime"].toString())
                startActivity(intent)
            }
        }

        return root
  }

    override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
    }


    private suspend fun getLikedMovies(): MutableList<String> {

        var movieList = mutableListOf<String>()

        currentUser.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    movieList.addAll(document["moviesLiked"] as Collection<String>)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }.await()

        return movieList
    }

    private suspend fun setMovieList() {

        val movieList = getLikedMovies();

        for(movie in movieList){
            var url = "https://api.themoviedb.org/3/movie/${movie}?api_key=63d93b08a5c17f9bbb9d8205524f892f&language=en-US"

            try {
                getDataFromServer(url) //Here we call the function that will connect to the server
            } catch (e: IOException) { //The getDataFromServer function can "throw" an IOException if there is an error. We have to "catch" that here, otherwise our entire app will crash.
                Log.d(ContentValues.TAG, "get failed with ", e)
                return
            }
        }
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
     * Method that takes a string containing JSON, and extracts the movie from API, and adds the image to list.
     * @param rawJson A string containing JSON encoded daa
     */
    fun readJSONMovie(rawJson: String) {
        activity?.runOnUiThread(java.lang.Runnable { //This section has to happen on the same thread as the user interface.
            try {
                var json = JSONObject(rawJson)
                var posterpath = json.getString("poster_path")
                var backdroppath = json.getString("backdrop_path")

                fun setList(list: JSONArray): String {
                    var genres = "";
                    for (i in 0 until list.length()) {
                        val movie = list.getJSONObject(i)
                        genres += movie.getString("name") + ", "
                    }
                    return genres.substring(0, genres.length-2);
                }

                var movie = mapOf(
                    "title" to json.getString("title"),
                    "overview" to json.getString("overview"),
                    "rating" to json.getString("vote_average"),
                    "release_date" to json.getString("release_date"),
                    "poster" to "https://image.tmdb.org/t/p/w500/${posterpath}",
                    "backdrop" to "https://image.tmdb.org/t/p/w500/${backdroppath}",
                    "country" to json.getJSONArray("production_countries").getJSONObject(0).getString("iso_3166_1"),
                    "genres" to setList(json.getJSONArray("genres")),
                    "runtime" to json.getString("runtime")
                )
                //Add movie to list
                moviesLiked.add(movie)

                //Hide loading screem
                binding.likedLoading.visibility = View.INVISIBLE;

                binding.gridView.layoutManager = GridLayoutManager(context,2)
                photoAdapter = LikedAdapter(requireActivity().applicationContext)
                binding.gridView.adapter = photoAdapter
                photoAdapter.setDataList(moviesLiked)

            } catch (e: JSONException) {
                Log.d(ContentValues.TAG, "get failed with ", e)
            }
        })
    }
}