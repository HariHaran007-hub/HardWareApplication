package com.rcappstudio.indoorfarming.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rcappstudio.indoorfarming.databinding.ItemPlantHealthLogBinding
import com.rcappstudio.indoorfarming.databinding.ItemPlantHealthOverviewBinding
import com.rcappstudio.indoorfarming.models.dbModel.HealthLogModel
import com.squareup.picasso.Picasso

class PlantsHealthLogAdapter(
    private val context: Context,
    private var plantHealthLogList : MutableList<HealthLogModel>,
    val onClick : (HealthLogModel, Int) ->Unit
) : RecyclerView.Adapter<PlantsHealthLogAdapter.ViewHolder>() {

    private lateinit var binding: ItemPlantHealthLogBinding

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemPlantHealthLogBinding.inflate(LayoutInflater.from(context), parent , false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = plantHealthLogList[position]
        Picasso.get()
            .load(log.imageUrl)
            .fit().centerCrop()
            .into(binding.rvYoutubeImageview)

        if(log.diseaseName == "nil"){
            binding.rvYoutubeTitleTextView.text = "No disease detected"
        } else {
            binding.rvYoutubeTitleTextView.text = "${log.diseaseName}:\n${log.inference}"
        }



        binding.root.setOnClickListener {
            onClick.invoke(log, position)
        }
    }

    override fun getItemCount(): Int {
        return plantHealthLogList.size
    }


}