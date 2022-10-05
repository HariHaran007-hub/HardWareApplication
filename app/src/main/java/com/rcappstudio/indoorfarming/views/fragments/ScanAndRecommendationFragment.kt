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
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
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
import com.rcappstudio.indoorfarming.databinding.FragmentScanAndRecommendationBinding
import com.rcappstudio.indoorfarming.models.dbModel.HealthLogModel
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel
import com.rcappstudio.indoorfarming.models.imageprocessingModel.Data
import com.rcappstudio.indoorfarming.models.imageprocessingModel.ImageProcessingResponseData
import com.rcappstudio.indoorfarming.utils.Constants
import com.rcappstudio.indoorfarming.utils.LoadingDialog
import com.rcappstudio.indoorfarming.utils.isConnected
import com.rcappstudio.placesapi.RetrofitInstance
import com.rcappstudio.placesapi.youtubeDataModel.YoutubeResults
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.IOException

class ScanAndRecommendationFragment : Fragment() {

    //TODO: Replace all with auth id

    private lateinit var binding : FragmentScanAndRecommendationBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var isdialog : AlertDialog
    private lateinit var inflater : LayoutInflater


    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent: Uri = result.uriContent!!
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uriContent)
            imageProcessingApiCall(uriContent,bitmap)
        } else {
            // an error occurred
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
        loadingDialog = LoadingDialog(requireActivity(), "Loading please wait....")
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.root)
        createNetworkDialog()
        init()
    }

    private fun fetchData(){

        loadingDialog.startLoading()
        FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}").get()
            .addOnSuccessListener { snapshot->
                if(snapshot.exists()){
                    val plantList = mutableListOf<PlantModel>()
                    for(p in snapshot.children){
                        plantList.add(p.getValue(PlantModel::class.java)!!)
                    }
                   getHealThLogList(plantList)
                } else{
                    Toast.makeText(requireContext(), "No health log detected", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun getHealThLogList(plantList: MutableList<PlantModel>){
        val hashMap = HashMap<Int, MutableList<HealthLogModel>>()
        var count = 0
        for(plant in plantList){
            Log.d("TAGData", "getHealThLogList: ${plant.key}")
            FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}/${plant.key}/${Constants.HEALTH_LOG}").get()
                .addOnSuccessListener { snapshot->
                    if(snapshot.exists()){

                        val plantList = mutableListOf<HealthLogModel>()
                        for(p in snapshot.children){
                            plantList.add(p.getValue(HealthLogModel::class.java)!!)
                            Log.d("TAGData", "getHealThLogList: ${p.getValue(HealthLogModel::class.java)!!}")
                        }
                        hashMap[count] = plantList

                        if(count == plantList.size - 1){
                            setRvAdapter(hashMap)
                        }
                        count++
                        loadingDialog.isDismiss()
                    } else {
                        Toast.makeText(requireContext(), "No health log detected", Toast.LENGTH_LONG).show()
                        loadingDialog.isDismiss()
                    }
                }
        }
    }

    private fun setRvAdapter(plantList : HashMap<Int, MutableList<HealthLogModel>>){
        binding.rvPlantHealthLog.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlantHealthLog.setHasFixedSize(true)
        binding.rvPlantHealthLog.adapter = PlantHealOverViewAdapter(requireContext(), plantList){item, pos->

            val state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {

                BottomSheetBehavior.STATE_COLLAPSED
            } else {
                //TODO : Setup api call and recycler view adapter
                initBottomSheet(item.diseaseName!!)
                BottomSheetBehavior.STATE_EXPANDED
            }
            bottomSheetBehavior.state = state
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
                Log.d("TAGData", "onCreate: ${response.body()}")
            }
        }
    }

    private fun imageProcessingApiCall(uri: Uri ,bitmap : Bitmap?){
        Log.d("TAGData", "imageProcessingApiCall: base64 bit ${encodeImage(bitmap!!)}")
        val dataImage = encodeImage(bitmap)!!
        lifecycleScope.launchWhenStarted{
            val response = try {
                RetrofitInstance.aiApi.getTodos(Data(dataImage))
            } catch (e : IOException){

                return@launchWhenStarted

            } catch (e: HttpException){
                return@launchWhenStarted

            }

            if(response.isSuccessful && response.body() != null){
                //TODO: Store to firebase and Move to Main activity
//                val intent = Intent(applicationContext, ResultActivity::class.java)
//                intent.putExtra("responseData", Gson().toJson(response.body()))
//                startActivity(intent)
                Log.d("AIDATA", "imageProcessingApiCall: ${response.body()}")
                storeImageToCloud(bitmap , response.body(), uri)
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
//        FirebaseDatabase.getInstance()
//            .getReference("${Constants./}")
    }


    private fun setUpBottomSheetRecyclerView(youtubeResults: YoutubeResults){
        binding.bottomSheet.rvYoutubeThumbnail.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.bottomSheet.rvYoutubeThumbnail.setHasFixedSize(true)
        binding.bottomSheet.rvYoutubeThumbnail.adapter = YoutubeAdapter(requireContext(), youtubeResults.items!!){item,pos->

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
            Log.d("networkState", "onViewCreated: no internet")
        }
    }

}