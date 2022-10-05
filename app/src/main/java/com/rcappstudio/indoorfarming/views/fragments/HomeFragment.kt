package com.rcappstudio.indoorfarming.views.fragments

import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.databinding.FragmentHomeBinding
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel
import com.rcappstudio.indoorfarming.utils.Constants
import com.rcappstudio.indoorfarming.utils.LoadingDialog
import com.rcappstudio.indoorfarming.utils.isConnected
import com.rcappstudio.placesapi.RetrofitInstance
import retrofit2.HttpException
import java.io.IOException

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var loadingDialog : LoadingDialog
    private lateinit var isdialog : AlertDialog

    private lateinit var inflater : LayoutInflater

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
        createNetworkDialog()
        init()
    }

    private fun fetchData(){
        loadingDialog.startLoading()
        val pref = requireContext().getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE)
        val plant =pref.getString("Plant_Key", null)

        FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${FirebaseAuth.getInstance().currentUser!!.uid}/${Constants.PLANTS}/$plant")
            .get().addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    val plant = snapshot.getValue(PlantModel::class.java)!!
                    initProgressView(plant)
                    loadingDialog.isDismiss()
                } else{
                    Toast.makeText(requireActivity(), "No data present!!", Toast.LENGTH_LONG).show()
                    loadingDialog.isDismiss()
                }
            }
    }

    private fun initProgressView(plantModel: PlantModel){
        //TODO: Yet to add last watered

        binding.progressSoilPh.progress.setProgress(plantModel.soilPh!!.toInt())
        binding.progressSoilPh.progressTitle.text = "Soil PH"

        binding.progressLuminosity.progress.setProgress(plantModel.luminousIntensity!!)
        binding.progressLuminosity.progressTitle.text = "Light Intensity"

        binding.progressWaterMoistureLevel.progress.setProgress(plantModel.waterMoistureLevel!!)
        binding.progressWaterMoistureLevel.progressTitle.text = "Moisture level"

        binding.progressEnvironmentTemperature.progress.setProgress(plantModel.environmentTemperature!!.toInt())
        binding.progressEnvironmentTemperature.progressTitle.text = "Temperature"

        binding.progressEnvironmentHumidity.progress.setProgress(plantModel.environmentHumidity!!.toInt())
        binding.progressEnvironmentHumidity.progressTitle.text = "Humidity"
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

    private fun init(){
        if(isConnected(requireContext())){
            fetchData()
            isdialog.dismiss()
        } else{
            isdialog.show()
            Log.d("networkState", "onViewCreated: no internet")
        }
    }
}