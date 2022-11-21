package com.example.finalproject.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.finalproject.databinding.FragmentHomeBinding
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        getMovie()

//    val textView: TextView = binding.textHome
//    homeViewModel.text.observe(viewLifecycleOwner) {
//      textView.text = it
//    }


        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    /**
     * This method changes the textview to show the cat fact passed as a parameter.
     * @param factText This variable contains the String of the fact, to be shown in the textview
     */



    private fun getMovie() {
        binding.movieTitle.text = "Loading..."
        var url = "https://api.themoviedb.org/3/movie/634649?api_key=63d93b08a5c17f9bbb9d8205524f892f&language=en-US"

        try {
            getDataFromServer(url) //Here we call the function that will connect to the server
        } catch (e: IOException) { //The getDataFromServer function can "throw" an IOException if there is an error. We have to "catch" that here, otherwise our entire app will crash.
            binding.movieTitle.text = "Server Error!"
            return
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setMovie(movie: Map<String, Any>) {
        binding.movieTitle.text = movie["title"].toString()
        binding.movieOverview.text = movie["overview"].toString();
        binding.movieRating.rating = movie["rating"].toString().toFloat() / 2
        binding.movieYear.text = "(${movie["release_date"].toString().substring(0, 4)})"
        binding.movieDetails.text =
            "${movie["release_date"].toString()} - (${movie["country"].toString()}) - ${movie["runtime"]}min\n"
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
     * Method that takes a string containing JSON, and extracts the Cat Fact.
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

}

