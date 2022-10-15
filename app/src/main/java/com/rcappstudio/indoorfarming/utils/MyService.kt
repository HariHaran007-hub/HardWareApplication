package com.rcappstudio.indoorfarming.utils

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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
                            if(find(plant.lastWateredTimeStamp!!) >= 4)
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
            (plant.minWaterMoistureLevel!! < plant.waterMoistureLevel!! && plant.waterMoistureLevel < plant.maxWaterMoistureLevel!!) &&
            (plant.minEnvironmentTemperature!! < plant.environmentTemperature!! && plant.environmentTemperature < plant.maxEnvironmentTemperature!!)

        ) {
            notificationPlantData[plant.key!!] = "${plant.plantName}Plant is happy \uD83D\uDE42"
        }

         else {
            notificationPlantData[plant.key!!] = "${plant.plantName}: Plant is not happy\uD83D\uDE1E"

        }
/*
        var HUMIDITY_FLAG = plant.minEnvironmentHumidity!!.toInt() < plant.environmentHumidity!!.toInt() && plant.environmentHumidity!!.toInt() < plant.maxEnvironmentHumidity!!.toInt()
        var SOILPH_FLAG = plant.minSoilPh!!.toInt() < plant.soilPh!!.toInt() && plant.soilPh.toInt() < plant.maxSoilPh!!.toInt()
        var WATERMOISTURE_FLAG = plant.minWaterMoistureLevel!!.toInt() < plant.waterMoistureLevel!!.toInt() && plant.waterMoistureLevel.toInt() < plant.maxWaterMoistureLevel!!.toInt()
        var TEMPERATURE_FLAG = plant.minEnvironmentTemperature!!.toInt() <plant.environmentTemperature!!.toInt() && plant.environmentTemperature.toInt() < plant.maxEnvironmentTemperature!!.toInt()


        //0 -> Humidity
        //1 -> Soil ph
        //2 -> Water moisture
        //3 -> Temperature

        val validateMap = HashMap<HashMap<String,Any?>,Boolean>()
        val humidityMap  = HashMap<String, Any?>()
        humidityMap["Humidity level"] = plant.environmentHumidity

        val soilPhMap  = HashMap<String, Any?>()
        soilPhMap["Soil Ph level"] = plant.soilPh

        val waterMoistureMap  = HashMap<String, Any?>()
        waterMoistureMap.put("Water moisture level", plant.waterMoistureLevel)

        val temperatureMap  = HashMap<String, Any?>()
        temperatureMap.put("Temperature level", plant.environmentTemperature)

        validateMap[humidityMap] = HUMIDITY_FLAG
        validateMap[soilPhMap] = SOILPH_FLAG
        validateMap[waterMoistureMap] = WATERMOISTURE_FLAG
        validateMap[temperatureMap] = TEMPERATURE_FLAG

        Log.d("DisplayData", "onBindViewHolder: $HUMIDITY_FLAG , $SOILPH_FLAG, $WATERMOISTURE_FLAG, $TEMPERATURE_FLAG ")
        var string = ""
        for(c in validateMap){
            if(c.value == false){
                string += "${c.key.keys.toMutableList()[0]}: ${c.key.values.toMutableList()[0]}\n"
            }
        }
        if(string.isEmpty()){
            binding.rvPlantStatTextView.text = "Plant is healthy  \uD83D\uDE0A"
            binding.rvCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.greenLight2))
        } else {
            binding.rvPlantStatTextView.text = string
            binding.rvCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.redLight2))
        }*/

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

/*    private fun watchForWaterPumpChanges(plantKeyString: String){
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("TAGTimerData", "onTick:${millisUntilFinished / 1000} ")

            }

            override fun onFinish() {
//                mTextField.setText("done!")
                FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}/$plantKeyString/${Constants.PUMP_WATER}")
                    .setValue(false)
                    .addOnCompleteListener {
                        if(it.isSuccessful){


                        }
                    }
            }
        }.start()
    }*/


}