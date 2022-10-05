package com.rcappstudio.indoorfarming.utils

import android.icu.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity

fun getDateTime(s: Long): String? {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val netDate = Date(s)
        sdf.format(netDate)
    } catch (e: Exception) {
        e.toString()
    }
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