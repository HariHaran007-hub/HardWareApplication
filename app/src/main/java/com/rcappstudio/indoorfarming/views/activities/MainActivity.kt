package com.rcappstudio.indoorfarming.views.activities

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.databinding.ActivityMainBinding
import com.rcappstudio.indoorfarming.utils.Constants
import com.rcappstudio.indoorfarming.utils.MyService


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkService()
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomBar.setItemSelected(R.id.home)

        binding.bottomBar.setOnItemSelectedListener { item ->
            when (item) {
                R.id.home ->
                    switchToFragment(R.id.homeFragment)
                R.id.managePlants ->
                    switchToFragment(R.id.managePlantsFragment)
                R.id.scanAndRecommendation ->
                    switchToFragment(R.id.scanAndRecommendationFragment)
                R.id.settings -> {
                    switchToFragment(R.id.settingsFragment)
                }
            }
        }
    }


    private fun checkService() {

        val serviceValue = getSharedPreferences(
            Constants.SHARED_PREF,
            Context.MODE_PRIVATE
        ).getBoolean(Constants.SERVICE_PROVIDER, false)
        if (serviceValue) {
            if (!isMyServiceRunning(MyService::class.java)) {
                startService(Intent(this, MyService::class.java))
            }
        }
    }

    private fun isMyServiceRunning(mClass: Class<MyService>): Boolean {
        val manager: ActivityManager = getSystemService(
            Context.ACTIVITY_SERVICE
        ) as ActivityManager

        for (service: ActivityManager.RunningServiceInfo in manager.getRunningServices(Integer.MAX_VALUE)) {

            if (mClass.name.equals(service.service.className)) {
                return true
            }
        }
        return false
    }

    private fun getNavController(): NavController {
        return (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController
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

    override fun onBackPressed() {
        super.onBackPressed()
        binding.bottomBar.setItemSelected(R.id.home)

    }
}