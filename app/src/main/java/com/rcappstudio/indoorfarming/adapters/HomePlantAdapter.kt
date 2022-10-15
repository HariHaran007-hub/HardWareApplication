package com.rcappstudio.indoorfarming.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.databinding.HomePlantRvBinding
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel
import com.rcappstudio.indoorfarming.utils.getDateTime
import com.squareup.picasso.Picasso

class HomePlantAdapter(
    private val context: Context,
    private var plantList: MutableList<PlantModel>,
    val onClick: (PlantModel, Int) -> Unit
) : RecyclerView.Adapter<HomePlantAdapter.ViewHolder>() {

    private lateinit var binding: HomePlantRvBinding

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePlantAdapter.ViewHolder {
        binding = HomePlantRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val plant = plantList[position]

        binding.rvPlantName.text = plant.plantName

        Picasso.get()
            .load(plant.plantImageUrl)
            .fit().centerCrop()
            .into(binding.rvPlantImageView)
/*

        if (
            (plant.minEnvironmentHumidity!! < plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            (plant.minSoilPh!! < plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            (plant.minLuminousIntensity!! < plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            (plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            (plant.minEnvironmentTemperature!! < plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)

        ) {
            binding.rvPlantStatTextView.text = "Plant is healthy \uD83D\uDE0A"
            binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.greenLight2))
        } else if (
            (plant.minSoilPh!! < plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            (plant.minLuminousIntensity!! < plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            (plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            (plant.minEnvironmentTemperature!! < plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)
        ) {
            binding.rvPlantStatTextView.text = "Environment humidity: ${plant.environmentHumidity}"

        } else if (
            (plant.minEnvironmentHumidity!! < plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            (plant.minLuminousIntensity!! < plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            (plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            (plant.minEnvironmentTemperature!! < plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)
        ) {
            //If Except soil ph all are dropped
            binding.rvPlantStatTextView.text = "Soi PH: ${plant.soilPh}"

        } else if (
            (plant.minEnvironmentHumidity!! < plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            (plant.minSoilPh!! < plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            (plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            (plant.minEnvironmentTemperature!! < plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)
        ) {
            binding.rvPlantStatTextView.text = "Luminous intensity: ${plant.luminousIntensity}"
        } else if (
            (plant.minEnvironmentHumidity!! < plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            (plant.minSoilPh!! < plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            (plant.minLuminousIntensity!! < plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            (plant.minEnvironmentTemperature!! < plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)
        ) {
            binding.rvPlantStatTextView.text = "Moisture level: ${plant.waterMoistureLevel}"
        } else if (
            (plant.minEnvironmentHumidity!! < plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            (plant.minSoilPh!! < plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            (plant.minLuminousIntensity!! < plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            (plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!)
        ) {
            binding.rvPlantStatTextView.text = "Temperature: ${plant.environmentTemperature}"
        } else if (
            (plant.minEnvironmentHumidity!! < plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            (plant.minSoilPh!! < plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            (plant.minLuminousIntensity!! < plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!)
        ) {
            binding.rvPlantStatTextView.text =
                "Moisture level: ${plant.waterMoistureLevel}\nTemperature: ${plant.environmentTemperature}"
        } else if (
            (plant.minSoilPh!! < plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            (plant.minLuminousIntensity!! < plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            (plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!)
        ) {
            binding.rvPlantStatTextView.text =
                "Humidity: ${plant.environmentHumidity}\nTemperature: ${plant.environmentTemperature}"
        } else if (
            (plant.minLuminousIntensity!! < plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            (plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            (plant.minEnvironmentTemperature!! < plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)
        ) {
            binding.rvPlantStatTextView.text =
                "Humidity: ${plant.environmentHumidity}\nSoil PH: ${plant.soilPh}"
        } else if (
            (plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            (plant.minEnvironmentTemperature!! < plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!) &&
            (plant.minEnvironmentHumidity!! < plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!)
        ) {
            binding.rvPlantStatTextView.text =
                "Soil PH: ${plant.soilPh}\nTemperature: ${plant.environmentTemperature}"
        }  else if (
            (plant.minEnvironmentHumidity!! < plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            (plant.minSoilPh!! < plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!)
        ) {
            binding.rvPlantStatTextView.text =
                "Light Intensity: ${plant.luminousIntensity}\nMoisture level: ${plant.waterMoistureLevel}\nTemperature: ${plant.environmentTemperature}"
        } else if (
            (plant.minSoilPh!! < plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            (plant.minLuminousIntensity!! < plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!)
        ) {
            binding.rvPlantStatTextView.text =
                "Humidity: ${plant.environmentHumidity}\nMoisture level: ${plant.waterMoistureLevel}\nTemperature: ${plant.environmentTemperature}"
        } else if (
            (plant.minLuminousIntensity!! < plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            (plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!)
        ) {
            binding.rvPlantStatTextView.text =
                "Humidity: ${plant.environmentHumidity}\nSoil PH: ${plant.soilPh}\nTemperature: ${plant.environmentTemperature}"

        } else if (
            (plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            (plant.minEnvironmentTemperature!! < plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)
        ) {
            binding.rvPlantStatTextView.text =
                "Humidity: ${plant.environmentHumidity}\nSoil PH: ${plant.soilPh}\nLight Intensity: ${plant.luminousIntensity}"
        } else if (
            (plant.minEnvironmentHumidity!! < plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            (plant.minEnvironmentTemperature!! < plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)
        ) {
            binding.rvPlantStatTextView.text =
                "Water moisture: ${plant.waterMoistureLevel}\nSoil PH: ${plant.soilPh}\nLight Intensity: ${plant.luminousIntensity}"
        }

        else if ((plant.minEnvironmentHumidity!! < plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!)) {
            binding.rvPlantStatTextView.text =
                "Soil PH: ${plant.soilPh}\nLight Intensity: ${plant.luminousIntensity}\nMoisture level: ${plant.waterMoistureLevel}\nTemperature: ${plant.environmentTemperature}"
        } else if ((plant.minSoilPh!! < plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!)) {
            binding.rvPlantStatTextView.text =
                "Humidity: ${plant.environmentHumidity}\nLight Intensity: ${plant.luminousIntensity}\nMoisture level: ${plant.waterMoistureLevel}\nTemperature: ${plant.environmentTemperature}"

        } else if ((plant.minLuminousIntensity!! < plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!)) {
            binding.rvPlantStatTextView.text =
                "Humidity: ${plant.environmentHumidity}\nSoil PH: ${plant.soilPh}\nMoisture level: ${plant.waterMoistureLevel}\nTemperature: ${plant.environmentTemperature}"

        } else if ((plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!)) {
            binding.rvPlantStatTextView.text =
                "Humidity: ${plant.environmentHumidity}\nSoil PH: ${plant.soilPh}\nLight Intensity: ${plant.luminousIntensity}\nTemperature: ${plant.environmentTemperature}"
        } else if ((plant.minEnvironmentTemperature!! < plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)) {
            binding.rvPlantStatTextView.text =
                "Humidity: ${plant.environmentHumidity}\nSoil PH: ${plant.soilPh}\nLight Intensity: ${plant.luminousIntensity}\nTemperature: ${plant.environmentTemperature}"
        } else {
            binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.greenLight2))
            binding.rvPlantStatTextView.text = "Plant is healthty  \uD83D\uDE0A"
        }
*/

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
        humidityMap.put("Humidity level", plant.environmentHumidity.toInt())

        val soilPhMap  = HashMap<String, Any?>()
        soilPhMap.put("Soil Ph level", plant.soilPh.toInt())

        val waterMoistureMap  = HashMap<String, Any?>()
        waterMoistureMap.put("Water moisture level", plant.waterMoistureLevel.toInt())

        val temperatureMap  = HashMap<String, Any?>()
        temperatureMap.put("Temperature level", plant.environmentTemperature.toInt())

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
            binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.greenLight2))
        } else {
            binding.rvPlantStatTextView.text = string
            binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLight2))
        }



        binding.rvLastWatered.text = getDateTime(plant.lastWateredTimeStamp!!)

        binding.root.setOnClickListener {
            onClick.invoke(plant,position)
        }

//        binding.rvBtnClickToWater.setOnClickListener {
//            onClick.invoke(plant, 0)
//        }
//
//        binding.rvPlantImageView.setOnClickListener {
//            onClick.invoke(plant,1)
//        }
    }

    override fun getItemCount(): Int {
        return plantList.size
    }

}