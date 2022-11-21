package com.example.finalproject

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.finalproject.databinding.ActivityBottomNavBinding
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class BottomNavActivity : AppCompatActivity() {

private lateinit var binding: ActivityBottomNavBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBottomNavBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_bottom_nav)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        navView.setupWithNavController(navController)

    }

    /**
     * This is the method attached to the button onClick property
     */
    fun getFact(view: View){
        //Set fact
        var url = "https://catfact.ninja/fact"

        try{
            getDataFromServer(url) //Here we call the function that will connect to the server
        }
        catch (e: IOException){ //The getDataFromServer function can "throw" an IOException if there is an error. We have to "catch" that here, otherwise our entire app will crash.
//            setFact("Error connecting to server")
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
        client.newCall(request).enqueue(object : Callback{ //This is an inner class that will be used to handle the response.

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

    /**
     * Method that takes a string containing JSON, and extracts the Cat Fact.
     * @param rawJson A string containing JSON encoded daa
     */
    fun readJSONFact(rawJson:String){
        runOnUiThread(java.lang.Runnable { //This section has to happen on the same thread as the user interface.
            try {
                var json = JSONObject(rawJson) //Convert the string into a JSONObject
                var fact = json.getString("fact") //Extract the fact from the property "fact", and put it in a string
//                setFact(fact) //call the method that will set the text on screen to show the fact
            }
            catch (e: JSONException){ //Handle any issues where the JSON is badly formed or invalid
//                setFact("Invalid JSON text")
            }
        })
    }
}
