package com.rcappstudio.indoorfarming.views.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.adapters.PlantHealOverViewAdapter
import com.rcappstudio.indoorfarming.adapters.YoutubeAdapter
import com.rcappstudio.indoorfarming.connectivity.ConnectivityObserver
import com.rcappstudio.indoorfarming.connectivity.NetworkConnectivityObserver
import com.rcappstudio.indoorfarming.databinding.FragmentScanAndRecommendationBinding
import com.rcappstudio.indoorfarming.models.dbModel.HealthLogModel
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel
import com.rcappstudio.indoorfarming.models.imageprocessingModel.Data
import com.rcappstudio.indoorfarming.models.imageprocessingModel.ImageProcessingResponseData
import com.rcappstudio.indoorfarming.utils.Constants
import com.rcappstudio.indoorfarming.utils.LoadingDialog
import com.rcappstudio.indoorfarming.utils.isConnected
import com.rcappstudio.indoorfarming.api.RetrofitInstance
import com.rcappstudio.placesapi.youtubeDataModel.YoutubeResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.IOException

class ScanAndRecommendationFragment : Fragment() {


    private lateinit var binding : FragmentScanAndRecommendationBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var isdialog : AlertDialog
    private lateinit var inflater : LayoutInflater
    private  var macAddress : String ?= null
    private lateinit var connectivityObserver: ConnectivityObserver

    private var databaseReference = FirebaseDatabase.getInstance()
        .getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}")

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent: Uri = result.uriContent!!
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uriContent)
            imageProcessingApiCall(uriContent,bitmap)
            loadingDialog.startLoading()
        } else {
            val exception = result.error
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        this.inflater = inflater
        binding = FragmentScanAndRecommendationBinding.inflate(LayoutInflater.from(requireContext()), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appBar.toolbar.title = "Health log"
        loadingDialog = LoadingDialog(requireActivity(), "Loading please wait....")
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.root)
        createNetworkDialog()
        connectivityObserver = NetworkConnectivityObserver(requireContext())
        CoroutineScope(Dispatchers.Main).launch {
            observeNetworkChanges()
        }

    }

    private fun fetchData(){
        val hashMap = HashMap<Int, MutableList<HealthLogModel>>()
        val plantNameList = mutableListOf<String>()
        loadingDialog.startLoading()
        var count = 0
        databaseReference.get()
            .addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    for(p in snapshot.children){
                        val plant = p.getValue(PlantModel::class.java)!!
                        val healthLogList = mutableListOf<HealthLogModel>()
                        for(h in plant.healthLog!!.values)
                            healthLogList.add(h)
                        hashMap[count] = healthLogList
                        plantNameList.add(plant.plantName!!)
                        count++
                    }
                    setRvAdapter(hashMap, plantNameList)
                    loadingDialog.isDismiss()
                } else{
                    Toast.makeText(requireContext(), "No health log detected", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setRvAdapter(plantList : HashMap<Int, MutableList<HealthLogModel>>, plantNameList : MutableList<String>){
        binding.rvPlantHealthLog.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlantHealthLog.setHasFixedSize(true)
        binding.rvPlantHealthLog.adapter = PlantHealOverViewAdapter(requireContext(), plantList, plantNameList){item, pos->

            if(pos == 0){
                val state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {

                    BottomSheetBehavior.STATE_COLLAPSED
                } else {
                    //TODO : Setup api call and recycler view adapter
                    initBottomSheet(item.diseaseName!!)
                    BottomSheetBehavior.STATE_EXPANDED
                }
                bottomSheetBehavior.state = state
            }  else{

                this.macAddress = item.macAddress.toString()
                if(this.macAddress != null){
                    permissionChecker()
                }
            }
        }
    }

    private fun initBottomSheet(dataString : String){
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

    private fun imageProcessingApiCall(uri: Uri ,bitmap : Bitmap?){
        val dataImage = encodeImage(bitmap!!)!!
        lifecycleScope.launchWhenStarted{
            val response = try {
                RetrofitInstance.aiApi.getHealthLog(Data(dataImage))
            } catch (e : IOException){

                return@launchWhenStarted

            } catch (e: HttpException){
                return@launchWhenStarted

            }

            if(response.isSuccessful && response.body() != null){
                storeImageToCloud(bitmap , response.body(), uri)
            } else{
                //TODO: Show some error occured
            }
        }
    }

    private fun storeImageToCloud(bitmap: Bitmap?, responseData : ImageProcessingResponseData?, uri: Uri){
        // TODO:  Currently the response data will be mannual in future we will automate it

        FirebaseStorage.getInstance()
            .getReference("${Constants.USER_FILES}/${FirebaseAuth.getInstance().uid}/${uri}")
            .putFile(uri).addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { url->
                    storeToDatabase(url.toString().trim(), responseData)
                }
            }
    }

    private fun storeToDatabase(url : String , responseData: ImageProcessingResponseData?){
        //TODO: Yet to manipulate the response data
       databaseReference.child("${this.macAddress}/${Constants.HEALTH_LOG}")
            .push().setValue(
                HealthLogModel(
                    url,
                    responseData!!.disease,
                    responseData.remedy,
                    macAddress
                )
            ).addOnCompleteListener {
                if(it.isSuccessful){
                    loadingDialog.isDismiss()
                    requireActivity().onBackPressed()
                }
            }
    }


    private fun setUpBottomSheetRecyclerView(youtubeResults: YoutubeResults){
        binding.bottomSheet.rvYoutubeThumbnail.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.bottomSheet.rvYoutubeThumbnail.setHasFixedSize(true)
        binding.bottomSheet.rvYoutubeThumbnail.adapter = YoutubeAdapter(requireContext(), youtubeResults.items!!){item,pos->
            watchYoutubeVideo(item)
        }
    }

    private fun encodeImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private fun permissionChecker() {
        Dexter.withContext(requireContext())
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {

                    cropImage.launch(options { setGuidelines(CropImageView.Guidelines.ON)
                        setCropShape(CropImageView.CropShape.RECTANGLE_HORIZONTAL_ONLY)
                        setAspectRatio(5,8)
                        setAutoZoomEnabled(true)
                        setFixAspectRatio(true)
                        setScaleType(CropImageView.ScaleType.CENTER_CROP)
                    })
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Toast.makeText(
                        requireContext(), "You have denied!! camera permissions",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(requireContext()).setMessage("Please enable the required permissions")
            .setPositiveButton("GO TO SETTINGS")
            { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", requireContext().packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel")
            { dialog, _ ->
                dialog.dismiss()
            }.show()
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
        }
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

}