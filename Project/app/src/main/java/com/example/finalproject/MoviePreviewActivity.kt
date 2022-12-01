package com.example.finalproject

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.bumptech.glide.Glide

class MoviePreviewActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_preview)

        findViewById<TextView>(R.id.movieTitle).text = intent.getStringExtra("title")
        findViewById<RatingBar>(R.id.movieRating).rating = intent.getStringExtra("rating").toString().toFloat() / 2
        findViewById<TextView>(R.id.movieOverview).text = intent.getStringExtra("overview")
        findViewById<TextView>(R.id.movieYear).text = "(${intent.getStringExtra("release_date").toString().substring(0,4)})"
        findViewById<TextView>(R.id.movieDetails).text = "${intent.getStringExtra("release_date")} " +
                            "- ${intent.getStringExtra("runtime")}min\n${intent.getStringExtra("genres")}"



        Glide.with(this)
            .load(intent.getStringExtra("backdrop"))
            .into(findViewById<ImageView>(R.id.movieBanner))

        Glide.with(this)
            .load(intent.getStringExtra("poster"))
            .into(findViewById<ImageView>(R.id.moviePoster))

        findViewById<View>(R.id.previewBack).setOnClickListener{
            val intent = Intent(this, BottomNavActivity::class.java)
            startActivity(intent)
        }
    }


}