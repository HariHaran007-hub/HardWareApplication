package com.rcappstudio.indoorfarming.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.makeramen.roundedimageview.RoundedImageView
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.databinding.PlantsListBinding
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel
import com.rcappstudio.indoorfarming.utils.Constants
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
        val imageView = itemView.findViewById<RoundedImageView>(R.id.rvPlantImageView)!!
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
        var HUMIDITY_FLAG = plant.minEnvironmentHumidity!!.toInt() < plant.environmentHumidity!!.toInt() && plant.environmentHumidity!!.toInt() < plant.maxEnvironmentHumidity!!.toInt()
        var SOILPH_FLAG = plant.minSoilPh!!.toInt() < plant.soilPh!!.toInt() && plant.soilPh.toInt() < plant.maxSoilPh!!.toInt()
        var WATERMOISTURE_FLAG = plant.minWaterMoistureLevel!!.toInt() < plant.waterMoistureLevel!!.toInt() && plant.waterMoistureLevel.toInt() < plant.maxWaterMoistureLevel!!.toInt()
        var TEMPERATURE_FLAG = plant.minEnvironmentTemperature!!.toInt() <plant.environmentTemperature!!.toInt() && plant.environmentTemperature.toInt() < plant.maxEnvironmentTemperature!!.toInt()


        //0 -> Humidity
        //1 -> Soil ph
        //2 -> Water moisture
        //3 -> Temperature

        val validateMap = HashMap<HashMap<String,Any?>,Boolean>()
        val humidityMap  = HashMap<String, Any?>()
        humidityMap["Humidity level"] = plant.environmentHumidity

        val soilPhMap  = HashMap<String, Any?>()
        soilPhMap["Soil Ph level"] = plant.soilPh

        val waterMoistureMap  = HashMap<String, Any?>()
        waterMoistureMap.put("Water moisture level", plant.waterMoistureLevel)

        val temperatureMap  = HashMap<String, Any?>()
        temperatureMap.put("Temperature level", plant.environmentTemperature)

        validateMap[humidityMap] = HUMIDITY_FLAG
        validateMap[soilPhMap] = SOILPH_FLAG
        validateMap[waterMoistureMap] = WATERMOISTURE_FLAG
        validateMap[temperatureMap] = TEMPERATURE_FLAG

        Log.d("DisplayData", "onBindViewHolder: $HUMIDITY_FLAG , $SOILPH_FLAG, $WATERMOISTURE_FLAG, $TEMPERATURE_FLAG ")
        var string = ""
        for(c in validateMap){
            if(c.value == false){
                string += "${c.key.keys.toMutableList()[0]}: ${c.key.values.toMutableList()[0]}\n"
            }
        }
        if(string.isEmpty()){
            binding.rvPlantStatTextView.text = "Plant is healthy  \uD83D\uDE0A"
            binding.rvCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.greenLight2))
        } else {
            binding.rvPlantStatTextView.text = string
            binding.rvCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLight2))
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