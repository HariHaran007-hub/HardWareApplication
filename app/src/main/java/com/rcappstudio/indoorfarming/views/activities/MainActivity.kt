package com.rcappstudio.indoorfarming.views.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.database.FirebaseDatabase
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomBar.setItemSelected(R.id.home)

        binding.bottomBar.setOnItemSelectedListener { item->

            Log.d("TAGData", "onCreate: $item")
            when(item){
                R.id.home ->
                    switchToFragment(R.id.homeFragment)
                R.id.managePlants ->
                    switchToFragment(R.id.managePlantsFragment)
                R.id.scanAndRecommendation ->
                    switchToFragment(R.id.scanAndRecommendationFragment)
            }
        }
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