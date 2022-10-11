package com.rcappstudio.indoorfarming.views.fragments

import android.app.ActivityManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.rcappstudio.indoorfarming.R
import com.rcappstudio.indoorfarming.databinding.FragmentSettingsBinding
import com.rcappstudio.indoorfarming.utils.Constants
import com.rcappstudio.indoorfarming.utils.MyService

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            FragmentSettingsBinding.inflate(LayoutInflater.from(requireContext()), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appBar.toolbar.title = "Settings"
        checkService()
        clickListener()
    }

    private fun checkService() {
        val serviceValue = requireContext().getSharedPreferences(
            Constants.SHARED_PREF,
            Context.MODE_PRIVATE
        ).getBoolean(Constants.SERVICE_PROVIDER, false)
        if (serviceValue) {
            requireActivity().startService(Intent(requireActivity(), MyService::class.java))
        }
        binding.offlineNotificationService.isChecked = serviceValue

        val automaticWateringValue = requireContext().getSharedPreferences(
            Constants.SHARED_PREF,
            MODE_PRIVATE
        ).getBoolean(Constants.AUTOMATIC_WATER_PUMP, false)

        if (automaticWateringValue) {
            binding.automaticWaterPump.isChecked = true
        }


    }

    private fun clickListener() {

        binding.offlineNotificationService.setOnClickListener {
            if (binding.offlineNotificationService.isChecked) {
                requireContext().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
                    .edit()
                    .apply {
                        putBoolean(Constants.SERVICE_PROVIDER, true)
                        requireActivity().startService(
                            Intent(
                                requireActivity(),
                                MyService::class.java
                            )
                        )
                    }.apply()

            } else {
                requireContext().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
                    .edit()
                    .apply {
                        putBoolean(Constants.SERVICE_PROVIDER, false)
                        requireActivity().stopService(
                            Intent(
                                requireActivity(),
                                MyService::class.java
                            )
                        )
                    }.apply()

            }
        }

        binding.automaticWaterPump.setOnClickListener {
            if (binding.automaticWaterPump.isChecked) {
                requireContext().getSharedPreferences(Constants.SHARED_PREF, MODE_PRIVATE).edit()
                    .apply {
                        if (binding.offlineNotificationService.isChecked) {
                            if (isMyServiceRunning(MyService::class.java)) {
                                requireContext().stopService(
                                    Intent(
                                        requireActivity(),
                                        MyService::class.java
                                    )
                                )
                                requireContext().startService(
                                    Intent(
                                        requireActivity(),
                                        MyService::class.java
                                    )
                                )
                            }
                            putBoolean(Constants.AUTOMATIC_WATER_PUMP, true)
                        } else {
                            binding.automaticWaterPump.isChecked = false
                            Snackbar.make(
                                binding.root,
                                "Please enable background notification service",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }.apply()
            } else {
                requireContext().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
                    .edit()
                    .apply {
                        putBoolean(Constants.AUTOMATIC_WATER_PUMP, false)
                    }.apply()

                if (isMyServiceRunning(MyService::class.java)) {
                    requireContext().stopService(
                        Intent(
                            requireActivity(),
                            MyService::class.java
                        )
                    )
                    requireContext().startService(
                        Intent(
                            requireActivity(),
                            MyService::class.java
                        )
                    )
                }
            }
        }

    }

    private fun isMyServiceRunning(mClass: Class<MyService>): Boolean {
        val manager: ActivityManager = requireContext().getSystemService(
            Context.ACTIVITY_SERVICE
        ) as ActivityManager

        for (service: ActivityManager.RunningServiceInfo in manager.getRunningServices(Integer.MAX_VALUE)) {

            if (mClass.name.equals(service.service.className)) {
                return true
            }
        }
        return false
    }
}