package com.rcappstudio.indoorfarming.utils

import android.content.Context
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.ParseException
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import java.util.concurrent.TimeUnit


fun getDateTime(s: Long): String? {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val netDate = Date(s)
        sdf.format(netDate)
    } catch (e: Exception) {
        e.toString()
    }
}

fun getTimeDifference(s : Long) : Int{
    val sdf = SimpleDateFormat("hh:mm:ss aa")
    val systemDate = Calendar.getInstance().timeInMillis
    val myDate = sdf.format(systemDate)
    val pastDate = sdf.format(s)
//                  txtCurrentTime.setText(myDate);

    //                  txtCurrentTime.setText(myDate);
//    Log.d("valueData", "getTimeDifference: ${pastDate}")
    val Date1 = sdf.parse(myDate)
    val Date2 = sdf.parse(pastDate)

    val millse = Date1.time - Date2.time
    val mills = Math.abs(millse)

    val Hours = (mills / (1000 * 60 * 60)).toInt()
    val Mins = (mills / (1000 * 60)).toInt() % 60
    val Secs = ((mills / 1000).toInt() % 60).toLong()
    val diff = "$Hours:$Mins:$Secs"

//    Log.d("valueData", "getTimeDifference Hours: $Hours")


   return Hours
}
fun getTimeDifference2(long  : Long) : Int{
    val sdf = SimpleDateFormat("yy/MM/dd HH:mm:ss")
    val f: DateFormat = SimpleDateFormat("yy/MM/dd HH:mm:s")
    val endDate = f.format(System.currentTimeMillis())
    val startDate = f.format(long)
    var d1: Date? = null
    var d2: Date? = null

    try{
        d1 = sdf.parse(startDate)
        d2 = sdf.parse(endDate)
    } catch (e : ParseException){

    }

    var diff = d2!!.time - d1!!.time

//    val diffSeconds = diff / 1000
//    val diffMinutes = diff / (60 * 1000)
//    val diffHours = diff / (60 * 60 * 1000)

    val days: Long = TimeUnit.MILLISECONDS.toDays(diff)
    val remainingHoursInMillis: Long = diff - TimeUnit.DAYS.toMillis(days)
    val hours: Long = TimeUnit.MILLISECONDS.toHours(remainingHoursInMillis)
    val remainingMinutesInMillis: Long = remainingHoursInMillis - TimeUnit.HOURS.toMillis(hours)
    val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(remainingMinutesInMillis)
    val remainingSecondsInMillis: Long =
        remainingMinutesInMillis - TimeUnit.MINUTES.toMillis(minutes)
    val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(remainingSecondsInMillis)

    return hours.toInt()
}

fun difference(long : Long){
    val sdf = SimpleDateFormat("hh:mm")
    val currentDate = sdf.format(System.currentTimeMillis())
    val pastDate = sdf.format(long)

    val d1 = sdf.parse(pastDate)
    val d2 = sdf.parse(currentDate)

    val diff = (d2.time - d1.time)/(1000*60*60)
    Log.d("tagData", "difference: ${diff}")

}


fun isConnected(context : Context): Boolean {
    val cm = context
        .getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
        ?: return false
    /* NetworkInfo is deprecated in API 29 so we have to check separately for higher API Levels */return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val network = cm.activeNetwork ?: return false
        val networkCapabilities = cm.getNetworkCapabilities(network) ?: return false
        val isInternetSuspended =
            !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)
        (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                && !isInternetSuspended)
    } else {
        val networkInfo = cm.activeNetworkInfo
        networkInfo != null && networkInfo.isConnected
    }
}

fun find(s : Long) : Int{
    // Create an instance of the SimpleDateFormat class
    val obj = SimpleDateFormat("MM-dd-yyyy HH:mm:ss")
    val systemDate = Calendar.getInstance().timeInMillis
    val join_date = obj.format(systemDate)
    val leave_date = obj.format(s)
    // In the try block, we will try to find the difference

        // Use parse method to get date object of both dates
        val date1 = obj.parse(join_date)
        val date2 = obj.parse(leave_date)

        // Calucalte time difference in milliseconds
        val time_difference = date1.time - date2.time

        // Calucalte time difference in days using TimeUnit class
        val days_difference = TimeUnit.MILLISECONDS.toDays(time_difference) % 365

        // Calculate time difference in years using TimeUnit class
        val years_difference = TimeUnit.MILLISECONDS.toDays(time_difference) / 365L
        // Calucalte time difference in seconds using TimeUnit class
        val seconds_difference = TimeUnit.MILLISECONDS.toSeconds(time_difference) % 60
        // Calucalte time difference in minutes using TimeUnit class
        val minutes_difference = TimeUnit.MILLISECONDS.toMinutes(time_difference) % 60
        // Calucalte time difference in hours using TimeUnit class
        val hours_difference = TimeUnit.MILLISECONDS.toHours(time_difference) % 24
        // Show difference in years, in days, hours, minutes, and seconds
        print(
            "Difference "
                    + "between two dates is: "
        )
        Log.d("valueData", "find: "+
            hours_difference
                .toString() + " hours, "
                    + minutes_difference
                    + " minutes, "
                    + seconds_difference
                    + " seconds, "
                    + years_difference
                    + " years, "
                    + days_difference
                    + " days")



    var finalHours  = hours_difference + (days_difference * 24)
    Log.d("valueData", "find: $finalHours")
    return finalHours.toInt()
}