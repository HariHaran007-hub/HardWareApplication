package com.rcappstudio.indoorfarming.views.fragments

import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.adapters.PlantsListAdapter
import com.rcappstudio.indoorfarming.connectivity.ConnectivityObserver
import com.rcappstudio.indoorfarming.connectivity.NetworkConnectivityObserver
import com.rcappstudio.indoorfarming.databinding.FragmentManagePlantsBinding
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel
import com.rcappstudio.indoorfarming.utils.Constants
import com.rcappstudio.indoorfarming.utils.LoadingDialog
import com.rcappstudio.indoorfarming.utils.isConnected
import com.rcappstudio.indoorfarming.views.activities.AddPlantActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ManagePlantsFragment : Fragment() {

    private lateinit var plantsListAdapter: PlantsListAdapter
    private lateinit var binding: FragmentManagePlantsBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var isdialog: AlertDialog
    private lateinit var inflater: LayoutInflater
    private lateinit var connectivityObserver: ConnectivityObserver


    private var databaseReference = FirebaseDatabase.getInstance()
        .getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        this.inflater = inflater
        binding = FragmentManagePlantsBinding.inflate(LayoutInflater.from(requireContext()))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appBar.toolbar.title = "Manage plants"
        loadingDialog = LoadingDialog(requireActivity(), "Loading please wait.....")
        createNetworkDialog()
        connectivityObserver = NetworkConnectivityObserver(requireContext())
        CoroutineScope(Dispatchers.Main).launch {
            observeNetworkChanges()
        }
        clickListeners()
    }


    private fun clickListeners() {

        binding.fabAddNewPlant.setOnClickListener {
            startActivity(Intent(requireContext(), AddPlantActivity::class.java))
            requireActivity().onBackPressed()
        }
    }


    private fun fetchData() {
        loadingDialog.startLoading()
        databaseReference
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val plantsList = mutableListOf<PlantModel>()
                    for (p in snapshot.children) {
                        val plant = p.getValue(PlantModel::class.java)
                        plantsList.add(plant!!)
                    }
                    initRecyclerView(plantsList)
                    loadingDialog.isDismiss()
                } else {
                    loadingDialog.isDismiss()
                    Toast.makeText(requireActivity(), "No plant are there.", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    private fun initRecyclerView(plantsList: MutableList<PlantModel>) {
            binding.rvPlantsList.layoutManager = LinearLayoutManager(requireContext())
            binding.rvPlantsList.setHasFixedSize(true)
            plantsListAdapter = PlantsListAdapter(requireContext(), plantsList) { item, _ ->
                requireContext().getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE).edit()
                    .apply {
                        putString(Constants.PLANT_KEY, item.key)
                        switchToFragment(R.id.homeFragment)
                    }.apply()
            }
            binding.rvPlantsList.adapter = plantsListAdapter

            val swipeToDeleteCallback = object : SwipeToDeleteCallback() {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    if (plantsList.size == 1) {
                        fetchData()
                        Snackbar.make(
                            binding.root,
                            "Cannot delete the plant minimum should be one",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        FirebaseDatabase.getInstance().getReference("${Constants.MAC_ADDRESS}/${plantsList[position].key}")
                            .get().addOnSuccessListener {
                                if(it.exists()){
                                    Toast.makeText(requireContext(), "Cannot remove master plant!!", Toast.LENGTH_LONG).show()
                                    fetchData()
                                } else{
                                    showAlertDialog(plantsList[position])
                                }
                            }
                    }
                }
            }
            val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
            itemTouchHelper.attachToRecyclerView(binding.rvPlantsList)

    }

    private fun removePlant(plant: PlantModel) {
        databaseReference.child("${plant.key}")
            .removeValue()
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

    private fun createNetworkDialog() {

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

    private fun init() {
        if (isConnected(requireContext())) {
            fetchData()
            isdialog.dismiss()
        } else {
            isdialog.show()
        }
    }

    private fun showAlertDialog(plant: PlantModel) {

        AlertDialog.Builder(requireContext()).setMessage("Please confirm to delete this plant")
            .setPositiveButton("Confirm")
            { _, _ ->
                removePlant(plant)

            }.setNegativeButton("Cancel")
            { dialog, _ ->
                fetchData()
                dialog.dismiss()
            }.show()

    }

    private suspend fun observeNetworkChanges() {
        connectivityObserver.observe().collect {
            if (it == ConnectivityObserver.Status.Available) {
                isdialog.dismiss()
                fetchData()
            } else {
                isdialog.show()
            }
        }
    }
}