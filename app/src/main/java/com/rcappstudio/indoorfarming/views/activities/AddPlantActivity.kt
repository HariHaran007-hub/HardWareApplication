package com.rcappstudio.indoorfarming.views.activities

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.models.dbModel.HealthLogModel
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel
import com.rcappstudio.indoorfarming.models.imageprocessingModel.Data
import com.rcappstudio.indoorfarming.models.imageprocessingModel.ImageProcessingResponseData
import com.rcappstudio.indoorfarming.utils.Constants
import com.rcappstudio.indoorfarming.utils.ExampleDialog
import com.rcappstudio.indoorfarming.utils.LoadingDialog
import com.rcappstudio.placesapi.RetrofitInstance
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.IOException

class AddPlantActivity : AppCompatActivity(), ExampleDialog.ExampleDialogListener {

    private lateinit var macAddress: String
    private lateinit var loadingDialog : LoadingDialog

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent: Uri = result.uriContent!!
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uriContent)
            loadingDialog.startLoading()
            imageProcessingApiCall(uriContent,bitmap)
        } else {
            // an error occurred
            val exception = result.error
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_plant)
        loadingDialog = LoadingDialog(this, "Loading please wait....")

        openDialog()
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

    private fun storeToDatabase(imageUrl : String, responseData: ImageProcessingResponseData?){
        //TODO: Currently we have done hard coded future it will be generalized

//        TODO: Fetch from api and integrate the response data to the below entity
//        maxWaterMoistureLevel = 11,
//        minWaterMoistureLevel = 12,
//
//        maxEnvironmentTemperature = 25.00,
//        minEnvironmentTemperature = 22.0,
//
//        maxSoilPh = 14,
//        minSoilPh = 7,
//
//        maxLuminousIntensity = 50,
//        minLuminousIntensity = 25,
//
//        maxEnvironmentHumidity = 50,
//        minEnvironmentHumidity = 20

/*        val plantModel = PlantModel(
            plantName = responseData!!.plant,
            plantImageUrl = imageUrl,
            pumpWater = false,

            waterMoistureLevel = 28,
            environmentTemperature = 23.3,
            soilPh = 14,
            luminousIntensity = 28,
            environmentHumidity = 18,
            lastWateredTimeStamp =1664874488,
            key = this.macAddress,

            maxWaterMoistureLevel = 11,
            minWaterMoistureLevel = 12,

            maxEnvironmentTemperature = 25.00,
            minEnvironmentTemperature = 22.0,

            maxSoilPh = 14,
            minSoilPh = 7,

            maxLuminousIntensity = 50,
            minLuminousIntensity = 25,

            maxEnvironmentHumidity = 50,
            minEnvironmentHumidity = 20
        )*/

        FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/Plants/${macAddress}")
            .setValue( PlantModel(
                plantName = responseData!!.plant,
                plantImageUrl = imageUrl,
                pumpWater = false,

                waterMoistureLevel = 28,
                environmentTemperature = 23.3,
                soilPh = 12.38,
                luminousIntensity = 28,
                environmentHumidity = 6.4,
                lastWateredTimeStamp =1664874488,
                key = this.macAddress,

                maxWaterMoistureLevel = 11,
                minWaterMoistureLevel = 12,

                maxEnvironmentTemperature = 25.00,
                minEnvironmentTemperature = 22.0,

                maxSoilPh = 13.2,
                minSoilPh = 7.4,

                maxLuminousIntensity = 50,
                minLuminousIntensity = 25,

                maxEnvironmentHumidity = 50.4,
                minEnvironmentHumidity = 20.3
            )).addOnSuccessListener {
                if(intent.getIntExtra("from", 0) == 1){

                    FirebaseDatabase.getInstance().getReference("${Constants.MAC_ADDRESS}/$macAddress")
                        .setValue(FirebaseAuth.getInstance().uid).addOnCompleteListener {
                            if(it.isSuccessful){

                                FirebaseDatabase.getInstance()
                                    .getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}/$macAddress/${Constants.HEALTH_LOG}")
                                    .push().setValue(HealthLogModel(imageUrl, responseData.disease, responseData.remedy ))
                                    .addOnSuccessListener {
                                        getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE).edit().apply {
                                            putString(Constants.PLANT_KEY, macAddress)

                                        }.apply()
                                        loadingDialog.isDismiss()
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }
                            } else{
                                Toast.makeText(this, "Some error occured please try again later..", Toast.LENGTH_LONG).show()
                                loadingDialog.isDismiss()
                            }
                        }
                } else{
                    FirebaseDatabase.getInstance()
                        .getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}/${macAddress}/${Constants.HEALTH_LOG}")
                        .push().setValue(HealthLogModel(imageUrl, responseData.disease, responseData.remedy ))
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE).edit()
                                    .apply {
                                        putString(Constants.PLANT_KEY, macAddress)
                                        loadingDialog.isDismiss()
                                        onBackPressed()
                                    }.apply()
                            } else {
                                Toast.makeText(this, "Some error occured please try again later..", Toast.LENGTH_LONG).show()
                                loadingDialog.isDismiss()
                            }
                        }
                }
            }
    }

    private fun permissionChecker() {
        Dexter.withContext(this)
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
                        this@AddPlantActivity, "You have denied!! camera permissions",
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
        AlertDialog.Builder(this).setMessage("Please enable the required permissions")
            .setPositiveButton("GO TO SETTINGS")
            { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", this.packageName, null)
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

    private fun encodeImage(bm: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }
    private fun openDialog() {
        val exampleDialog = ExampleDialog()
        exampleDialog.show(supportFragmentManager, "Enter mac address")
    }

    override fun applyTexts(macAddress: String?) {
        if(macAddress != null){
            this.macAddress = macAddress
            permissionChecker()
        }
    }


}