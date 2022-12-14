package com.example.finalproject.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.finalproject.MoviePreviewActivity
import com.example.finalproject.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 Adapter was based on this source:

 Laksara, P. (2021). RecyclerView with Grid Layout in Kotlin. [online]
 Medium. Available at: https://pasindulaksara.medium.com/recyclerview
 -with-grid-layout-in-kotlin-414d780c47ae [Accessed 25 Nov. 2022].

 */
class LikedAdapter(var context: Context) : RecyclerView.Adapter<LikedAdapter.ViewHolder>() {

    private var dataList = emptyList<Map<String, Any>>()

    internal fun setDataList(dataList: MutableList<Map<String, Any>>) {
        this.dataList = dataList
    }

    // Provide a direct reference to each of the views with data items
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView
        var button: FloatingActionButton

        init {
            image = itemView.findViewById(R.id.posterView)
            button = itemView.findViewById(R.id.likedButton)
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikedAdapter.ViewHolder {

        // Inflate the custom layout
        var view = LayoutInflater.from(parent.context).inflate(R.layout.liked_grid_item, parent, false)
        return ViewHolder(view)
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(holder: LikedAdapter.ViewHolder, position: Int) {

        // Get the data model based on position
        var data = dataList[position]

        Glide.with(this.context)
            .load(data["poster"])
            .into(holder.image)


        holder.button.setOnClickListener{

            var intent = Intent(this.context, MoviePreviewActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("title", data["title"].toString())
            intent.putExtra("overview", data["overview"].toString())
            intent.putExtra("rating", data["rating"].toString())
            intent.putExtra("release_date", data["release_date"].toString())
            intent.putExtra("poster", data["poster"].toString())
            intent.putExtra("backdrop", data["backdrop"].toString())
            intent.putExtra("genres", data["genres"].toString())
            intent.putExtra("runtime", data["runtime"].toString())
            startActivity(this.context, intent, Bundle())
        }
    }

    //  total count of items in the list
    override fun getItemCount() = dataList.size
}