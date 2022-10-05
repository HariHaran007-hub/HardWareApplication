package com.rcappstudio.indoorfarming.views.fragments

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.adapters.PlantsListAdapter
import com.rcappstudio.indoorfarming.databinding.FragmentManagePlantsBinding
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel
import com.rcappstudio.indoorfarming.utils.Constants
import com.rcappstudio.indoorfarming.utils.LoadingDialog
import com.rcappstudio.indoorfarming.utils.MyService
import com.rcappstudio.indoorfarming.utils.isConnected
import com.rcappstudio.indoorfarming.views.activities.AddPlantActivity


class ManagePlantsFragment : Fragment() {

    private lateinit var plantsListAdapter : PlantsListAdapter
    private lateinit var binding : FragmentManagePlantsBinding
    private lateinit var loadingDialog : LoadingDialog
    private lateinit var isdialog : AlertDialog
    private lateinit var inflater : LayoutInflater


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        this.inflater = inflater
        binding = FragmentManagePlantsBinding.inflate(LayoutInflater.from(requireContext()))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity(), "Loading please wait.....")
        createNetworkDialog()
        init()
        checkService()
        clickListeners()
    }

    private fun checkService(){
        val serviceValue = requireContext().getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE).getBoolean(Constants.SERVICE_PROVIDER, false)
        if(serviceValue){
            requireActivity().startService(Intent(requireActivity(), MyService::class.java))
        }
        binding.serviceProvider.isChecked = serviceValue
    }

    private fun clickListeners(){

        binding.fabAddNewPlant.setOnClickListener {
            startActivity(Intent(requireContext(), AddPlantActivity::class.java ))
        }

        binding.serviceProvider.setOnClickListener {
            if(binding.serviceProvider.isChecked){
                requireContext().getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE).edit()
                    .apply {
                        putBoolean(Constants.SERVICE_PROVIDER, true)
                        requireActivity().startService(Intent(requireActivity(), MyService::class.java))
                    }.apply()

            } else {
                requireContext().getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE).edit()
                    .apply {
                        putBoolean(Constants.SERVICE_PROVIDER, false)
                        requireActivity().stopService(Intent(requireActivity(), MyService::class.java))
                    }.apply()

            }
        }
    }

    private fun startStopService(){
        if(isMyServiceRunning(MyService::class.java)){
            Toast.makeText(requireContext(), "Service is Stopped", Toast.LENGTH_LONG).show()

            requireActivity().stopService(Intent(requireActivity(), MyService::class.java))
        } else {
            Toast.makeText(requireContext(), "Service is started......", Toast.LENGTH_LONG).show()

            requireActivity().startService(Intent(requireActivity(), MyService::class.java))

        }
    }


    private fun isMyServiceRunning(mClass: Class<MyService>) : Boolean{
        val manager : ActivityManager = requireActivity().getSystemService(
            Context.ACTIVITY_SERVICE
        ) as ActivityManager

        for (service: ActivityManager.RunningServiceInfo in manager.getRunningServices(Integer.MAX_VALUE)){

            if(mClass.name.equals(service.service.className)){
                return true
            }
        }
        return false
    }

    private fun fetchData(){

        //TODO: Yet to add complete path after authentication
        loadingDialog.startLoading()
        FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}")
            .get().addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    val plantsList = mutableListOf<PlantModel>()
                    for(p in snapshot.children){
                        val plant = p.getValue(PlantModel::class.java)
                        Log.d("TAGData", "fetchData: $plant")
                        plantsList.add(plant!!)
                    }
                    initRecyclerView(plantsList)
                    loadingDialog.isDismiss()
                } else {
                    loadingDialog.isDismiss()
                    Toast.makeText(requireActivity(), "No data present!!", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun initRecyclerView(plantsList : MutableList<PlantModel>){
        binding.rvPlantsList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlantsList.setHasFixedSize(true)
        plantsListAdapter = PlantsListAdapter(requireContext(),plantsList){item, pos->
            //TODO: Yet to show alert dialog
            requireContext().getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE).edit().apply {
                putString(Constants.PLANT_KEY, item.key)
                switchToFragment(R.id.homeFragment)
            }.apply()
        }
        binding.rvPlantsList.adapter = plantsListAdapter
    }


    private fun getNavController(): NavController {
        return (requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController
    }

    private fun switchToFragment(destinationId: Int) {
        if (isFragmentInBackStack(destinationId)) {
            getNavController().popBackStack(destinationId, false)
        } else {
            getNavController().navigate(destinationId)
        }
    }

    private fun isFragmentInBackStack(destinationId: Int) =
        try {
            getNavController().getBackStackEntry(destinationId)
            true
        } catch (e: Exception) {
            false
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