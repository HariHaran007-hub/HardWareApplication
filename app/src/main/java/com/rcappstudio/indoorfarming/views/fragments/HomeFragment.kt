package com.rcappstudio.indoorfarming.views.fragments

import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.adapters.HomePlantAdapter
import com.rcappstudio.indoorfarming.connectivity.ConnectivityObserver
import com.rcappstudio.indoorfarming.connectivity.NetworkConnectivityObserver
import com.rcappstudio.indoorfarming.databinding.FragmentHomeBinding
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel
import com.rcappstudio.indoorfarming.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var changesEvenListener: ValueEventListener
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var binding: FragmentHomeBinding
    private lateinit var loadingDialog : LoadingDialog
    private lateinit var isdialog : AlertDialog
    private lateinit var waterPumpDialog : AlertDialog
    private lateinit var inflater : LayoutInflater
    private lateinit var wateringTextView : TextView

    private var databaseReference = FirebaseDatabase.getInstance()
        .getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.inflater = inflater
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(requireContext()))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity(), "Loading please wait")
        connectivityObserver = NetworkConnectivityObserver(requireContext())

        createNetworkDialog()
        if(isConnected(requireContext())) {
            CoroutineScope(Dispatchers.Main).launch {
                observeNetworkChanges()
            }
        }else {
            isdialog.show()
            }

        createWaterPumpDialog()
    }

    private fun fetchData(){
        loadingDialog.startLoading()

       databaseReference
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val plantList = mutableListOf<PlantModel>()
                        for(c in snapshot.children){
                            val plant = c.getValue(PlantModel::class.java)!!
                            plantList.add(plant)
                            if(plant.pumpWater!!){
                                listenBackForChanges(plant)
                            }
                        }
                        initProgressView(plantList)
                        setupRecyclerView(plantList)
                        loadingDialog.isDismiss()
                    } else{
                        Toast.makeText(requireActivity(), "Please select plant in plant management bar.", Toast.LENGTH_LONG).show()
                        loadingDialog.isDismiss()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun initProgressView(plantList: MutableList<PlantModel>){
        if(activity != null){
            val pref = requireActivity().getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
            val plantKey =pref.getString("Plant_Key", null)
            var plantModel : PlantModel?= null
            plantList.forEach {
                if(it.key == plantKey){
                    plantModel = it
                }
            }

            if(plantModel != null){
                binding.dashLastWatered.text = "Last watered: ${getDateTime(plantModel!!.lastWateredTimeStamp!!)}"
                binding.progressSoilPh.progress.setProgress(plantModel!!.soilPh!!.toInt())
                binding.progressSoilPh.progressTitle.text = "Soil PH"

                binding.progressLuminosity.progress.setProgress(plantModel!!.luminousIntensity!!)
                binding.progressLuminosity.progressTitle.text = "Light Intensity"

                binding.progressWaterMoistureLevel.progress.setProgress(plantModel!!.waterMoistureLevel!!)
                binding.progressWaterMoistureLevel.progressTitle.text = "Moisture level"

                binding.progressEnvironmentTemperature.progress.setProgress(plantModel!!.environmentTemperature!!.toInt())
                binding.progressEnvironmentTemperature.progressTitle.text = "Temperature"

                binding.progressEnvironmentHumidity.progress.setProgress(plantModel!!.environmentHumidity!!.toInt())
                binding.progressEnvironmentHumidity.progressTitle.text = "Humidity"

                binding.progressAirQuality.progress.setProgress(plantModel!!.airQualityLevel!!.toInt())
                binding.progressAirQuality.progressTitle.text = "Air quality"

                binding.dashPlantName.text = plantModel!!.plantName.toString() + "Plant Monitor"
            }
        }
    }

    private fun createNetworkDialog(){

        val dialogView = inflater.inflate(R.layout.no_internet_dialog, null)
        val builder = AlertDialog.Builder(requireContext())
        val retryButton = dialogView.findViewById<Button>(R.id.retry)
        retryButton.setOnClickListener {
            init()
        }
        builder.setView(dialogView)
        builder.setCancelable(false)
        isdialog = builder.create()
    }

    private fun setupRecyclerView(plantList : MutableList<PlantModel>){
        binding.homeRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.homeRecyclerView.setHasFixedSize(true)
        binding.homeRecyclerView.adapter = HomePlantAdapter(requireContext(), plantList){item , position ->
            showAlertDialog(item)
        }
    }

    private fun init() {
        if (isConnected(requireContext())) {
            fetchData()
            isdialog.dismiss()
        } else {
            isdialog.show()
        }
    }

    private fun showAlertDialog(plant: PlantModel){

        AlertDialog.Builder(requireContext()).setMessage("Please confirm to water the plant!!")
            .setTitle("Confirmation")
            .setPositiveButton("Confirm")
            { _,_ ->
                startWaterPump(plant)

            }.setNegativeButton("Cancel")
            { dialog, _ ->
                fetchData()
                dialog.dismiss()
            }.show()
    }

    private fun  startWaterPump(plant  :PlantModel){

       databaseReference.child("${plant.key}/${Constants.PUMP_WATER}")
            .setValue(true)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    listenBackForChanges(plant)
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Unexpected error occrd", Toast.LENGTH_LONG).show()
            }
    }



    private fun listenBackForChanges(plant: PlantModel){

        changesEvenListener = databaseReference.child("${plant.key}/${Constants.PUMP_WATER}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        if(snapshot!!.getValue(Boolean::class.java)!!){
                            wateringTextView.text = "Watering ${plant.plantName} plant....."
                            waterPumpDialog.show()
                        } else {
                            waterPumpDialog.dismiss()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun createWaterPumpDialog(){

        val dialogView = inflater.inflate(R.layout.water_pump_dialog, null)
        val builder = AlertDialog.Builder(requireContext())
        wateringTextView = dialogView.findViewById(R.id.textView)
        builder.setView(dialogView)
        builder.setCancelable(false)
        waterPumpDialog = builder.create()
    }

    private suspend fun observeNetworkChanges(){
        connectivityObserver.observe().collect{
            if(it == ConnectivityObserver.Status.Available){
                isdialog.dismiss()
                fetchData()
            } else {
                isdialog.show()
            }
        }
    }
}