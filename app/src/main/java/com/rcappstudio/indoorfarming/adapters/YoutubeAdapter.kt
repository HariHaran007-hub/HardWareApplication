package com.rcappstudio.indoorfarming.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.databinding.ItemYoutubeThumbnailBinding
import com.rcappstudio.indoorfarming.databinding.PlantsListBinding
import com.rcappstudio.placesapi.youtubeDataModel.Item
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class YoutubeAdapter(
    private val context : Context,
    private var videoList : List<Item>,
    val onClick: (String , Int)-> Unit
): RecyclerView.Adapter<YoutubeAdapter.ViewHolder>() {

    private lateinit var binding : ItemYoutubeThumbnailBinding

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //TODO: Yet to implement id's to views
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {
        binding = ItemYoutubeThumbnailBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = videoList[position]
        Picasso.get()
            .load(item.snippet!!.thumbnails!!.high!!.url)
            .fit()
            .centerCrop()
            .into(binding.rvYoutubeImageview)

        binding.rvYoutubeTitleTextView.text = item.snippet!!.title.toString()

        binding.root.setOnClickListener {
            onClick.invoke(item.id!!.videoId.toString(),position)
        }
    }

    override fun getItemCount(): Int {
        return videoList.size
    }
}