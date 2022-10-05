package com.rcappstudio.indoorfarming.utils

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rcappstudio.indoorfarming.views.activities.MainActivity
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.models.dbModel.PlantModel

class MyService  : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        initDb()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    private fun showNotification(msg : String){
        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE
        )

        val notification = Notification
            .Builder(this , Constants.CHANNEL_ID)
            .setContentText(msg)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(Constants.ALERT_NOTIFICATION_ID, notification)
    }


    private fun initDb(){
        val plantKeyList = mutableListOf<String>()
        FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}").get()
            .addOnSuccessListener {
                if(it.exists()){
                    for(c in it.children){
                        Log.d("TAGData", "initDb: ${c.key}")
                        plantKeyList.add(c.key!!)
                    }

                }
                attachListener(plantKeyList)
            }
    }

    private fun attachListener(list : MutableList<String>){
        for(c in list){
            FirebaseDatabase.getInstance().getReference("${Constants.USERS}/${FirebaseAuth.getInstance().uid}/${Constants.PLANTS}/${c}").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val plant = snapshot.getValue(PlantModel::class.java)
                        //TODO: Yet to implement the logic for particular data change
                        showNotification(plant!!.plantName!!)
                        Log.d("ServiceData", "onDataChange: ${plant!!.plantName}")
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


    private fun getData(){
        val pref = this.getSharedPreferences("SHARED_PREF", MODE_PRIVATE)
        Log.d("ServiceData", "getData: ${pref.getString("Plant_Key", null)}")
    }


}