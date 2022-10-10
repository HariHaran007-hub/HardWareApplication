package com.rcappstudio.indoorfarming.utils

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel
import com.rcappstudio.indoorfarming.views.activities.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap


class MyService  : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private var plantWaterKeyString = mutableListOf<String>()
    val plantKeyList = mutableListOf<String>()
    private val notificationPlantData = HashMap<String, String>()
    private var isAutomaticWater = false

    override fun onCreate() {
        super.onCreate()
        initDb()
        createNotificationChannel()

        CoroutineScope(Dispatchers.Main).launch {
//            observeNetworkChanges()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isAutomaticWater= getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE).getBoolean(Constants.AUTOMATIC_WATER_PUMP, false)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun showNotification(contentData : String){
        var str = ""
        if(isAutomaticWater){
            str = "\n\nAutomatic water pump is on...."
        }
        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE
        )
        val notificationView =RemoteViews(packageName, R.layout.custom_notification_layout)
        notificationView.setTextViewText(R.id.notificationPlantName, "Monitoring plant health....." + str)
        notificationView.setTextViewText(R.id.plantDescription, contentData)

        val notification = NotificationCompat
            .Builder(this , Constants.CHANNEL_ID)
            .setSmallIcon(R.drawable.flora_aid_logo)
            .setContentIntent(pendingIntent)
            .setCustomBigContentView(notificationView)
            .build()

        startForeground(Constants.ALERT_NOTIFICATION_ID, notification)
    }


    private fun initDb(){

        FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}").get()
            .addOnSuccessListener {
                if(it.exists()){
                    for(c in it.children){
                        plantKeyList.add(c.key!!)
                    }

                }
                attachListener(plantKeyList)
            }
    }

    private fun attachListener(list : MutableList<String>){
        Log.d("NotificationData", "attachListener: Automatic ${isAutomaticWater}")
        for(c in list){
            FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}/${c}").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val plant = snapshot.getValue(PlantModel::class.java)
                        evaluateData(plant!!)
                        if(isAutomaticWater){
                            Log.d("NotificationData", "onDataChange time difference: ${getTimeDifference(plant.lastWateredTimeStamp!!)}")
                            if(find(plant.lastWateredTimeStamp!!) > 4)
                                evaluateWaterPump(plant)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val serviceChannel = NotificationChannel(
                Constants.CHANNEL_ID,
                "My Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(serviceChannel)
        }
    }


    private fun evaluateData(plant : PlantModel){


        if (
            (plant.minEnvironmentHumidity!! < plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            (plant.minSoilPh!! < plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            (plant.minLuminousIntensity!! < plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            (plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            (plant.minEnvironmentTemperature!! < plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)

        ) {
            notificationPlantData[plant.key!!] = "${plant.plantName}Plant is happy \uD83D\uDE42"
            /*Log.d("NotificationData", "evaluateData: " +
                    "${(plant.minEnvironmentHumidity!! < plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!)}" +
                    "${(plant.minSoilPh!! < plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) }" +
                    "${(plant.minLuminousIntensity!! < plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!)}" +
                    "${(plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!)}" +
                    "${(plant.minEnvironmentTemperature!! < plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)}"
*/
//            )
        }

        /*else if (
            !(plant.minSoilPh!! > plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            !(plant.minLuminousIntensity!! > plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            !(plant.minWaterMoistureLevel!! > plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            !(plant.minEnvironmentTemperature!! > plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)
        ) {
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"
//            showNotification(plant, "Plant not happy \nEnvironment humidity: ${plant.environmentHumidity}", "\uD83D\uDE1E")

        } else if (
            !(plant.minEnvironmentHumidity!! > plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            !(plant.minLuminousIntensity!! > plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            !(plant.minWaterMoistureLevel!! > plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            !(plant.minEnvironmentTemperature!! > plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)
        ) {
            //If Except soil ph all are dropped
//            notificationPlantData[plant.plantName!!] = "Plant not happy \nSoi PH: ${plant.soilPh}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nSoi PH: ${plant.soilPh}", "\uD83D\uDE1E")

        } else if (
            !(plant.minEnvironmentHumidity!! > plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            !(plant.minSoilPh!! > plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            !(plant.minWaterMoistureLevel!! > plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            !(plant.minEnvironmentTemperature!! > plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)
        ) {
//            notificationPlantData[plant.plantName!!] = "Plant not happy \nLuminous intensity: ${plant.luminousIntensity}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nLuminous intensity: ${plant.luminousIntensity}", "\uD83D\uDE1E")
        } else if (
            !(plant.minEnvironmentHumidity!! > plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            !(plant.minSoilPh!! > plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            !(plant.minLuminousIntensity!! > plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            !(plant.minEnvironmentTemperature!! > plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)
        ) {
//            notificationPlantData[plant.plantName!!] = "Plant not happy \nMoisture level: ${plant.waterMoistureLevel}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nMoisture level: ${plant.waterMoistureLevel}", "\uD83D\uDE1E")
        } else if (
            !(plant.minEnvironmentHumidity!! > plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            !(plant.minSoilPh!! > plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            !(plant.minLuminousIntensity!! > plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            !(plant.minWaterMoistureLevel!! > plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!)
        ) {
//            notificationPlantData[plant.plantName!!] = "Plant not happy \nTemperature: ${plant.environmentTemperature}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nTemperature: ${plant.environmentTemperature}", "\uD83D\uDE1E")
        } else if (
            !(plant.minEnvironmentHumidity!! > plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            !(plant.minSoilPh!! > plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!)
        ) {
//            notificationPlantData[plant.plantName!!] = "Plant not happy \nLight Intensity: ${plant.luminousIntensity}\n" +
//                    "Moisture level: ${plant.waterMoistureLevel}\n" +
//                    "Temperature: ${plant.environmentTemperature}"

            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"


//            showNotification(plant, "Plant not happy \nLight Intensity: ${plant.luminousIntensity}\n" +
//                    "Moisture level: ${plant.waterMoistureLevel}\n" +
//                    "Temperature: ${plant.environmentTemperature}", "\uD83D\uDE1E")
        } else if (
            !(plant.minSoilPh!! > plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            !(plant.minLuminousIntensity!! > plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!)
        ) {
//            notificationPlantData[plant.plantName!!] = "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Moisture level: ${plant.waterMoistureLevel}\n" +
//                    "Temperature: ${plant.environmentTemperature}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Moisture level: ${plant.waterMoistureLevel}\n" +
//                    "Temperature: ${plant.environmentTemperature}", "\uD83D\uDE1E")
        } else if (
            !(plant.minLuminousIntensity!! > plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            !(plant.minWaterMoistureLevel!! > plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!)
        ) {
//            notificationPlantData[plant.plantName!!] = "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Soil PH: ${plant.soilPh}\n" +
//                    "Temperature: ${plant.environmentTemperature}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Soil PH: ${plant.soilPh}\n" +
//                    "Temperature: ${plant.environmentTemperature}", "\uD83D\uDE1E")
        } else if (
            !(plant.minWaterMoistureLevel!! > plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            !(plant.minEnvironmentTemperature!! > plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)
        ) {
//            notificationPlantData[plant.plantName!!] = "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Soil PH: ${plant.soilPh}\n" +
//                    "Light Intensity: ${plant.luminousIntensity}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E2"

//            showNotification(plant, "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Soil PH: ${plant.soilPh}\n" +
//                    "Light Intensity: ${plant.luminousIntensity}", "\uD83D\uDE1E")
        } else if (
            !(plant.minEnvironmentHumidity!! > plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            !(plant.minEnvironmentTemperature!! > plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)
        ) {
//            notificationPlantData[plant.plantName!!] = "Plant not happy \nWater moisture: ${plant.waterMoistureLevel}\n" +
//                    "Soil PH: ${plant.soilPh}\n" +
//                    "Light Intensity: ${plant.luminousIntensity}
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nWater moisture: ${plant.waterMoistureLevel}\n" +
//                    "Soil PH: ${plant.soilPh}\n" +
//                    "Light Intensity: ${plant.luminousIntensity}", "\uD83D\uDE1E")
        } else if (
            !(plant.minEnvironmentHumidity!! > plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!) &&
            !(plant.minSoilPh!! > plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            !(plant.minLuminousIntensity!! > plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!)
        ) {
//            notificationPlantData[plant.plantName!!] = "Plant not happy \nMoisture level: ${plant.waterMoistureLevel}\n" +
//                    "Temperature: ${plant.environmentTemperature}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nMoisture level: ${plant.waterMoistureLevel}\n" +
//                    "Temperature: ${plant.environmentTemperature}", "\uD83D\uDE1E")
        } else if (
            !(plant.minSoilPh!! > plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!) &&
            !(plant.minLuminousIntensity!! > plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            !(plant.minWaterMoistureLevel!! > plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!)
        ) {
//            notificationPlantData[plant.plantName!!] = "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Temperature: ${plant.environmentTemperature}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Temperature: ${plant.environmentTemperature}", "\uD83D\uDE1E")
        } else if (
            !(plant.minLuminousIntensity!! > plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!) &&
            !(plant.minWaterMoistureLevel!! > plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            !(plant.minEnvironmentTemperature!! > plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)
        ) {
//            notificationPlantData[plant.plantName!!] = "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Soil PH: ${plant.soilPh}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Soil PH: ${plant.soilPh}", "\uD83D\uDE1E")
        } else if (
            !(plant.minWaterMoistureLevel!! > plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            !(plant.minEnvironmentTemperature!! > plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!) &&
            !(plant.minEnvironmentHumidity!! > plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!)
        ) {
//            notificationPlantData[plant.plantName!!] = "Plant not happy \nSoil PH: ${plant.soilPh}\n" +
//                    "Temperature: ${plant.environmentTemperature}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nSoil PH: ${plant.soilPh}\n" +
//                    "Temperature: ${plant.environmentTemperature}", "\uD83D\uDE1E")
        } else if (!(plant.minEnvironmentHumidity!! > plant.environmentHumidity!! && plant.environmentHumidity < plant.maxEnvironmentHumidity!!)) {

//            notificationPlantData[plant.plantName!!] = "Plant not happy \nSoil PH: ${plant.soilPh}\n" +
//                    "Light Intensity: ${plant.luminousIntensity}\n" +
//                    "Moisture level: ${plant.waterMoistureLevel}\n" +
//                    "Temperature: ${plant.environmentTemperature}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nSoil PH: ${plant.soilPh}\n" +
//                    "Light Intensity: ${plant.luminousIntensity}\n" +
//                    "Moisture level: ${plant.waterMoistureLevel}\n" +
//                    "Temperature: ${plant.environmentTemperature}", "\uD83D\uDE1E")
        } else if (!(plant.minSoilPh!! > plant.soilPh!!.toDouble() && plant.soilPh.toDouble() < plant.maxSoilPh!!)) {

//            notificationPlantData[plant.plantName!!] = "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Light Intensity: ${plant.luminousIntensity}\n" +
//                    "Moisture level: ${plant.waterMoistureLevel}\n" +
//                    "Temperature: ${plant.environmentTemperature}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Light Intensity: ${plant.luminousIntensity}\n" +
//                    "Moisture level: ${plant.waterMoistureLevel}\n" +
//                    "Temperature: ${plant.environmentTemperature}", "\uD83D\uDE1E")
        } else if (!(plant.minLuminousIntensity!! > plant.luminousIntensity!! && plant.luminousIntensity < plant.maxLuminousIntensity!!)) {

//            notificationPlantData[plant.plantName!!] = "Plant not happy \nTemperature: ${plant.environmentTemperature}"
//            "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Soil PH: ${plant.soilPh}\n" +
//                    "Moisture level: ${plant.waterMoistureLevel}\n" +
//                    "Temperature: ${plant.environmentTemperature}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Soil PH: ${plant.soilPh}\n" +
//                    "Moisture level: ${plant.waterMoistureLevel}\n" +
//                    "Temperature: ${plant.environmentTemperature}", "\uD83D\uDE1E")
        } else if (!(plant.minWaterMoistureLevel!! > plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!)) {

//            notificationPlantData[plant.plantName!!] = "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Soil PH: ${plant.soilPh}\n" +
//                    "Light Intensity: ${plant.luminousIntensity}\n" +
//                    "Temperature: ${plant.environmentTemperature}"
            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Soil PH: ${plant.soilPh}\n" +
//                    "Light Intensity: ${plant.luminousIntensity}\n" +
//                    "Temperature: ${plant.environmentTemperature}", "\uD83D\uDE1E")
        } else if (!(plant.minEnvironmentTemperature!! > plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)) {

//            notificationPlantData[plant.plantName!!] = "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Soil PH: ${plant.soilPh}\n" +
//                    "Light Intensity: ${plant.luminousIntensity}\n" +
//                    "Temperature: ${plant.environmentTemperature}"

            notificationPlantData[plant.plantName!!] = "Plant not happy \uD83D\uDE1E"

//            showNotification(plant, "Plant not happy \nHumidity: ${plant.environmentHumidity}\n" +
//                    "Soil PH: ${plant.soilPh}\n" +
//                    "Light Intensity: ${plant.luminousIntensity}\n" +
//                    "Temperature: ${plant.environmentTemperature}", "\uD83D\uDE1E")*/
         else {
            notificationPlantData[plant.key!!] = "${plant.plantName}: Plant is not happy\uD83D\uDE1E"


//            showNotification(plant, "Plant is healthy", "\uD83D\uDE42")
        }
//        Log.d("NotificationData", "updateNotificationView: ${notificationPlantData.keys.size} , ${plantKeyList.size}")

        if(notificationPlantData.keys.size == plantKeyList.size) {
            updateNotificationView()
        }
    }

    private fun updateNotificationView(){
        var string = ""
        if(notificationPlantData.keys.size == plantKeyList.size){
            for(c in notificationPlantData){
                string += "${c.value}\n\n"
                Log.d("NotificationData", "updateNotificationView: $string")
            }
            showNotification(string)
        }
    }

    private fun evaluateWaterPump(plant: PlantModel){


        if(!(plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!)) {
            Log.d("NotificationData", "evaluateWaterPump: Evaluated ")
            plantWaterKeyString.add(plant.key!!)
            turnOnWaterPump()
        }

    }

    private fun turnOnWaterPump(){
        if(plantWaterKeyString.isNotEmpty()){
            var count = 0
            for(key in plantWaterKeyString){

                FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}/$key/${Constants.PUMP_WATER}")
                    .setValue(true)
                    .addOnCompleteListener {
                        if(it.isSuccessful){

                            updateTimeStamp(key)
                        }
                    }
                count ++
                if(count == plantWaterKeyString.size){
                    plantWaterKeyString = mutableListOf()
                }
            }
        }
    }

    private fun updateTimeStamp(key : String){
        FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}/$key/lastWateredTimeStamp")
                                .setValue(Calendar.getInstance().timeInMillis)
    }

//    private fun watchForWaterPumpChanges(plantKeyString: String){
//        object : CountDownTimer(60000, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                Log.d("TAGTimerData", "onTick:${millisUntilFinished / 1000} ")
//
//            }
//
//            override fun onFinish() {
////                mTextField.setText("done!")
//                FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}/$plantKeyString/${Constants.PUMP_WATER}")
//                    .setValue(false)
//                    .addOnCompleteListener {
//                        if(it.isSuccessful){
//
//
//                        }
//                    }
//            }
//        }.start()
//    }


}