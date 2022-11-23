package com.example.finalproject.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.finalproject.R

class LikedAdapter(var context: Context) : RecyclerView.Adapter<LikedAdapter.ViewHolder>() {

    private var dataList = emptyList<String>()

    internal fun setDataList(dataList: MutableList<String>) {
        this.dataList = dataList
    }

    // Provide a direct reference to each of the views with data items
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView

        init {
            image = itemView.findViewById(R.id.posterView)
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
            .load(data)
            .into(holder.image)

        holder.image.setOnClickListener{

        }

    }

    //  total count of items in the list
    override fun getItemCount() = dataList.size
}