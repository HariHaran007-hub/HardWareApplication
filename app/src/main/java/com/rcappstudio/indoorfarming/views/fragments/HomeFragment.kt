package com.rcappstudio.indoorfarming.views.fragments

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.adapters.HomePlantAdapter
import com.rcappstudio.indoorfarming.adapters.YoutubeAdapter
import com.rcappstudio.indoorfarming.api.RetrofitInstance
import com.rcappstudio.indoorfarming.connectivity.ConnectivityObserver
import com.rcappstudio.indoorfarming.connectivity.NetworkConnectivityObserver
import com.rcappstudio.indoorfarming.databinding.FragmentHomeBinding
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel
import com.rcappstudio.indoorfarming.utils.*
import com.rcappstudio.placesapi.youtubeDataModel.YoutubeResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class HomeFragment : Fragment() {

    private lateinit var changesEvenListener: ValueEventListener
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
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
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.root)
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
//                binding.dashLastWatered.text = "Last watered: ${getDateTime(plantModel!!.lastWateredTimeStamp!!)}"
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

                binding.dashPlantName.text = plantModel!!.plantName.toString() + " Plant Monitor"

                binding.tvTemperature.text = plantModel!!.environmentTemperature!!.toInt().toString()+ "°C"

                val intialLightIntensity = plantModel!!.luminousIntensity!!.toFloat()
                Log.d("NewData", "initProgressView: intialLightIntensity: $intialLightIntensity")
                var min = 0
                var max = 255
                var val1 = (intialLightIntensity - min)
                Log.d("NewData", "initProgressView: val1: $val1")
                var val2 = (max - min)
                Log.d("NewData", "initProgressView: val2: $val2")
                var output = val1 / val2
                Log.d("NewData", "initProgressView: output: $output.")

                var finalOutput = output * 100

                binding.tvIntensity.text = finalOutput.toInt().toString() + "%"
                binding.airQualityLevel.text = Math.abs(plantModel!!.airQualityLevel!!).toInt().toString() + "ppm"

                binding.tvHumidityLevel.text = "Humidity level \n${plantModel!!.environmentHumidity!!.toInt().toString()}%"
                binding.tvMoistureLevel.text = "Moisture level \n${plantModel!!.waterMoistureLevel!!.toInt().toString()}%"
                binding.tvSoilPhLevel.text = "Soil PH ${plantModel!!.soilPh}"

                binding.tvHumidity.text = plantModel!!.environmentHumidity!!.toInt().toString() + "%"
                binding.tvSoilPh.text = plantModel!!.soilPh!!.toInt().toString()


                val soil = plantModel!!.waterMoistureLevel!!.toFloat()
                var min1 = 2575
                var max1 = 4095
                    var val11 = (soil - min1)
                Log.d("NewData", "initProgressView: val11: $val11")
                var val22 = (max1 - min1)
                Log.d("NewData", "initProgressView: val22: $val22")
                var output1 = val11 / val22
                Log.d("NewData", "initProgressView: output: $output1")

                var finalOutput1 = output1 * 100
                Log.d("NewData", "initProgressView: finalOutput1: $finalOutput1")
                Log.d("NewData", "initProgressView: finalOutput1: ${100-finalOutput1}")

                binding.tvWaterMoisture.text = (100 - finalOutput1).toFloat().toInt().toString()+ "%"

                binding.tvLastWatered.text = "Last watered: ${getDateTime(plantModel!!.lastWateredTimeStamp!!)}"
                binding.btnClickToWater.setOnClickListener {
                    showAlertDialog(plantModel!!)
                }

                binding.youtubeRecommendation.setOnClickListener {
                    val state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                        BottomSheetBehavior.STATE_COLLAPSED
                    } else {
                        //TODO : Setup api call and recycler view adapter
                        initBottomSheet("Tips to grow ${plantModel!!.plantName}")
                        BottomSheetBehavior.STATE_EXPANDED
                    }
                    bottomSheetBehavior.state = state
                }

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
            requireContext().getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE).edit()
                .apply {
                    putString(Constants.PLANT_KEY, item.key)
                    fetchData()
                }.apply()

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

    private fun initBottomSheet(dataString : String){
        Log.d("YouTubeData", "initBottomSheet: data accessed")
        lifecycleScope.launchWhenStarted {
            val response = try {
                RetrofitInstance.api.getYoutubeResults("snippet", dataString, Constants.YOUTUBE_API_KEY)
            } catch (e: IOException) {

                return@launchWhenStarted

            } catch (e: HttpException) {
                return@launchWhenStarted
            }

            if (response.isSuccessful && response.body() != null) {
                setUpBottomSheetRecyclerView(response.body()!!)
            }
        }
    }

    private fun watchYoutubeVideo(id: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://www.youtube.com/watch?v=$id")
        )
        try {
            startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            startActivity(webIntent)
        }
    }

    private fun setUpBottomSheetRecyclerView(youtubeResults: YoutubeResults){
        binding.bottomSheet.rvYoutubeThumbnail.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.bottomSheet.rvYoutubeThumbnail.setHasFixedSize(true)
        binding.bottomSheet.rvYoutubeThumbnail.adapter = YoutubeAdapter(requireContext(), youtubeResults.items!!){item,pos->
            watchYoutubeVideo(item)
        }
    }
}