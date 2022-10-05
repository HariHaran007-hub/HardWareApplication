package com.rcappstudio.indoorfarming.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.rcappstudio.indoorfarming.databinding.ItemPlantHealthOverviewBinding
import com.rcappstudio.indoorfarming.models.dbModel.HealthLogModel
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel

class PlantHealOverViewAdapter(
    private val context : Context,
    private var plantList : HashMap<Int, MutableList<HealthLogModel>>,
    val onClick : (HealthLogModel, Int) ->Unit
) : RecyclerView.Adapter<PlantHealOverViewAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    }

    private lateinit var binding : ItemPlantHealthOverviewBinding


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       binding = ItemPlantHealthOverviewBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val plant = plantList.get(position)
//        binding.tvPlantNameHealthLog.text = plant.plantName!!
        setHealthLogAdapter(plant!!)
    }

    private fun setHealthLogAdapter(healthLogList: MutableList<HealthLogModel>){
        Log.d("TAGData", "setHealthLogAdapter: $healthLogList")
        binding.rvPlantHealthLog.setHasFixedSize(true)
        binding.rvPlantHealthLog.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvPlantHealthLog.adapter = PlantsHealthLogAdapter(context,healthLogList){item, pos->
           onClick.invoke(item, pos)
        }
    }

    override fun getItemCount(): Int {
        return plantList.size
    }


}