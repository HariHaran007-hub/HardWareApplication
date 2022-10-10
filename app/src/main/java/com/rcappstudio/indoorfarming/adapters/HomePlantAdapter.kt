package com.rcappstudio.indoorfarming.adapters

import android.content.Context
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

        if (
            (plant.minEnvironmentHumidity!! < plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            (plant.minSoilPh!! < plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            (plant.minLuminousIntensity!! < plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            (plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            (plant.minEnvironmentTemperature!! < plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)

        ) {
            binding.rvPlantStatTextView.text = "Plant is healthy \uD83D\uDE0A"
            binding.cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.greenLight2))
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
            binding.cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.greenLight2))
            binding.rvPlantStatTextView.text = "Plant is healthty  \uD83D\uDE0A"
        }

        binding.rvLastWatered.text = getDateTime(plant.lastWateredTimeStamp!!)

        binding.rvBtnClickToWater.setOnClickListener {
            onClick.invoke(plant, position)
        }
    }

    override fun getItemCount(): Int {
        return plantList.size
    }
}