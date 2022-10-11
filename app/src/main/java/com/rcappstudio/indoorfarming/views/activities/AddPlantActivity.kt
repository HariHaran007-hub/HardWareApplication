package com.rcappstudio.indoorfarming.views.activities

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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budiyev.android.codescanner.*
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.rcappstudio.indoorfarming.databinding.ActivityAddPlantBinding
import com.rcappstudio.indoorfarming.models.dbModel.HealthLogModel
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel
import com.rcappstudio.indoorfarming.models.imageprocessingModel.AddPlantImageProcessingResponse
import com.rcappstudio.indoorfarming.models.imageprocessingModel.Data
import com.rcappstudio.indoorfarming.utils.Constants
import com.rcappstudio.indoorfarming.utils.ExampleDialog
import com.rcappstudio.indoorfarming.utils.LoadingDialog
import com.rcappstudio.indoorfarming.api.RetrofitInstance
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.IOException

class AddPlantActivity : AppCompatActivity(), ExampleDialog.ExampleDialogListener {

    private lateinit var macAddress: String
    private lateinit var loadingDialog: LoadingDialog

    private var databaseReference = FirebaseDatabase.getInstance()
        .getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}")

    private lateinit var codeScanner: CodeScanner
    private lateinit var binding: ActivityAddPlantBinding

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent: Uri = result.uriContent!!
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uriContent)
            loadingDialog.startLoading()
            imageProcessingApiCall(uriContent, bitmap)
            codeScanner.stopPreview()
        } else {
            val exception = result.error
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivityAddPlantBinding.inflate(layoutInflater)
        loadingDialog = LoadingDialog(this, "Loading please wait....")
        setContentView(binding.root)
        checkSelfPermissions()
        codeScanner()
    }

    private fun imageProcessingApiCall(uri: Uri, bitmap: Bitmap?) {
        Log.d("TAGData", "imageProcessingApiCall: base64 bit ${encodeImage(bitmap!!)}")
        val dataImage = encodeImage(bitmap)!!
        lifecycleScope.launchWhenStarted {
            val response = try {
                RetrofitInstance.aiApi.addPlantGetData(Data(dataImage))
            } catch (e: IOException) {

                return@launchWhenStarted

            } catch (e: HttpException) {
                return@launchWhenStarted

            }
            //TODO: Change com.rcappstudio.indoorfarming.api.Api and implement AI API
            if (response.isSuccessful && response.body() != null) {
                //TODO: Store to firebase and Move to Main activity
                //TODO: Show Confirmation Dialog for Plant Name
                Log.d("AIDATA", "imageProcessingApiCall: ${response.body()}")
                storeImageToCloud(bitmap, response.body(), uri)
            } else {
                Log.d("AIDATA", "imageProcessingApiCall: ${response.code()}")
            }
        }
    }

    private fun storeImageToCloud(
        bitmap: Bitmap?,
        responseData: AddPlantImageProcessingResponse?,
        uri: Uri
    ) {
        // TODO:  Currently the response data will be manual in future we will automate it

        FirebaseStorage.getInstance()
            .getReference("${Constants.USER_FILES}/${FirebaseAuth.getInstance().uid}/${uri}")
            .putFile(uri).addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { url ->
                    storeToDatabase(url.toString().trim(), responseData)
                }
            }
    }

    private fun storeToDatabase(imageUrl: String, responseData: AddPlantImageProcessingResponse?) {

        databaseReference.child("$macAddress")
            .setValue(
                PlantModel(
                    plantName = responseData!!.plantName,
                    plantImageUrl = imageUrl,
                    pumpWater = false,

                    waterMoistureLevel = 0,
                    environmentTemperature = 00.00,
                    soilPh = 00.00,
                    luminousIntensity = 0,
                    environmentHumidity = 00.00,
                    lastWateredTimeStamp = System.currentTimeMillis(),
                    key = this.macAddress,

                    maxWaterMoistureLevel = responseData.maxWaterMoistureLevel!!.toInt(),
                    minWaterMoistureLevel = responseData.minWaterMoistureLevel!!.toInt(),

                    maxEnvironmentTemperature = responseData.maxEnvironmentTemperature,
                    minEnvironmentTemperature = responseData.minEnvironmentTemperature,

                    maxSoilPh = responseData.maxSoilPh,
                    minSoilPh = responseData.minSoilPh,

                    maxLuminousIntensity = responseData.maxLuminousIntensity,
                    minLuminousIntensity = responseData.minLuminousIntensity!!.toInt(),

                    maxEnvironmentHumidity = responseData.maxEnvironmentHumidity,
                    minEnvironmentHumidity = responseData.minEnvironmentHumidity,
                    airQualityLevel = 00.00
                )
            ).addOnSuccessListener {
                if (intent.getIntExtra("from", 0) == 1) {

                    FirebaseDatabase.getInstance()
                        .getReference("${Constants.MAC_ADDRESS}/$macAddress")
                        .setValue(FirebaseAuth.getInstance().uid).addOnCompleteListener {
                            if (it.isSuccessful) {

                                databaseReference.child("$macAddress/${Constants.HEALTH_LOG}")
                                    .push()
                                    .setValue(
                                        HealthLogModel(
                                            imageUrl,
                                            responseData.disease,
                                            "Please log another data for the health log.",
                                            macAddress
                                        )
                                    )
                                    .addOnSuccessListener {
                                        getSharedPreferences(
                                            Constants.SHARED_PREF,
                                            MODE_PRIVATE
                                        ).edit().apply {
                                            putString(Constants.PLANT_KEY, macAddress)
                                            loadingDialog.isDismiss()
                                            startActivity(
                                                Intent(
                                                    applicationContext,
                                                    MainActivity::class.java
                                                )
                                            )
                                            finish()
                                        }.apply()


                                    }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Some error occured please try again later..",
                                    Toast.LENGTH_LONG
                                ).show()
                                loadingDialog.isDismiss()
                            }
                        }
                } else {
                    databaseReference.child("${macAddress}/${Constants.HEALTH_LOG}")
                        .push().setValue(
                            HealthLogModel(
                                imageUrl,
                                responseData.disease,
                                "Please log another data for health log.",
                                macAddress
                            )
                        )
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE).edit()
                                    .apply {
                                        putString(Constants.PLANT_KEY, macAddress)
                                        loadingDialog.isDismiss()
                                        finish()
                                    }.apply()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Some error occured please try again later..",
                                    Toast.LENGTH_LONG
                                ).show()
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

                    cropImage.launch(options {
                        setGuidelines(CropImageView.Guidelines.ON)
                        setCropShape(CropImageView.CropShape.RECTANGLE_HORIZONTAL_ONLY)
                        setAspectRatio(5, 8)
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

    private fun codeScanner() {
        codeScanner = CodeScanner(this, binding.qrScannerView)

        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                runOnUiThread {
                    applyTexts(it.text.toString().trim())
                }
            }

            errorCallback = ErrorCallback {
                runOnUiThread {
                    Log.e("ErrorMain", "codeScanner: ${it.message}")
                }
            }
        }

        binding.qrScannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()

    }

    private fun openDialog(plantName: String) {
        val exampleDialog = ExampleDialog()
        exampleDialog.show(supportFragmentManager, "Confirm the plant name $plantName")
    }

    override fun applyTexts(macAddress: String?) {
        if (macAddress != null) {
            this.macAddress = macAddress
            permissionChecker()
        }
    }

    private fun checkSelfPermissions() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {

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
}