package com.rcappstudio.indoorfarming.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.databinding.PlantsListBinding
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel
import com.rcappstudio.indoorfarming.utils.getDateTime
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PlantsListAdapter(
    private val context: Context,
    private var plantsList: MutableList<PlantModel>,
    val onClick: (PlantModel, Int) -> Unit
) : RecyclerView.Adapter<PlantsListAdapter.ViewHolder>() {

    private lateinit var binding: PlantsListBinding


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<CircleImageView>(R.id.rvPlantImageView)!!
        val plantName = itemView.findViewById<TextView>(R.id.rvPlantName)!!
        val plantStateTextView = itemView.findViewById<TextView>(R.id.rvPlantStatTextView)!!
        val lastWatered = itemView.findViewById<TextView>(R.id.rvLastWatered)!!
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        binding = PlantsListBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val plant = plantsList[position]

        //TODO: Yet to add complete check
        if (plant.waterMoistureLevel!! < 25 && plant.environmentTemperature!! < 21
            && plant.soilPh!! < 25 && plant.luminousIntensity!! < 25
            && plant.environmentHumidity!! < 25
        ) {
            holder.plantStateTextView.text =
                "Moisture level: ${plant.waterMoistureLevel}\nEnvironment temp: ${plant.environmentTemperature}\nSoil ph: ${plant.soilPh}\nLuminous Intensity: ${plant.luminousIntensity}\nEnvironment Humidity: ${plant.environmentHumidity}"
            binding.rvCardView.strokeColor = ContextCompat.getColor(context, R.color.red)
            binding.rvCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLight))
        } else if (plant.waterMoistureLevel!! < 25) {
            holder.plantStateTextView.text = "Water moisture level: ${plant.waterMoistureLevel}"
            binding.rvCardView.strokeColor = ContextCompat.getColor(context, R.color.red)
            binding.rvCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLight))
        } else if (plant.environmentTemperature!! < 21) {
            holder.plantStateTextView.text = "Environment Temp: ${plant.environmentTemperature}"
            binding.rvCardView.strokeColor = ContextCompat.getColor(context, R.color.red)
            binding.rvCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLight))
        } else if (plant.soilPh!! < 25) {
            holder.plantStateTextView.text = "Soil PH: ${plant.soilPh}"
            binding.rvCardView.strokeColor = ContextCompat.getColor(context, R.color.red)
            binding.rvCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLight))
        } else if (plant.luminousIntensity!! < 25) {
            holder.plantStateTextView.text = "Soil PH: ${plant.luminousIntensity}"
            binding.rvCardView.strokeColor = ContextCompat.getColor(context, R.color.red)
            binding.rvCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLight))
        } else {
            binding.rvCardView.strokeColor = ContextCompat.getColor(context, R.color.green)
            binding.rvCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.greenLight))
        }


        Picasso.get()
            .load(plant.plantImageUrl)
            .fit()
            .centerCrop()
            .into(holder.imageView)

        holder.plantName.text = plant.plantName
        holder.lastWatered.text = "Last watered: " + getDateTime(plant.lastWateredTimeStamp!!)

        //Recycler view Click - Listener
        binding.root.setOnClickListener {
            onClick.invoke(plant,position)
        }

    }

    override fun getItemCount(): Int {
        return plantsList.size
    }
}