package com.example.parking.activities

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment

import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController

import com.example.parking.R


import com.example.parking.databinding.ActivityHomeBinding
import com.example.parking.services.GPSTracker
import com.example.parking.utils.MyLocationServices
import com.example.parking.utils.MySignal
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView


class HomeActivity : AppCompatActivity() {
    companion object {
        // PERMISSION ASK REQUEST CODES:
        const val MY_PERMISSIONS_REQUEST_LOCATION = 1
    }

    private lateinit var binding: ActivityHomeBinding
    private var isLocationTrakerOn = false
    private var gpsEnabled = false
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    val fm: FragmentManager = supportFragmentManager
    // fragments:


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initValues()
        Log.d("home_activity", "onCreate: ")
        if (!MyLocationServices.getInstance(this).checkLocationPermission()) {
            requestPermission()

        }
        gpsEnabled = MyLocationServices.getInstance(this).isGpsEnabledRequest(this)
        EnableMyLocationServices()

    }


    private fun initValues() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.home_FCV) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(
            binding.bottomNav,
            navController
        )


    }


    private fun EnableMyLocationServices() {
        // Bind to LocalService
        if (MyLocationServices.getInstance(this).checkLocationPermission()) {
            if (MyLocationServices.getInstance(this).isGpsEnabled()) {
                isLocationTrakerOn = true
                Log.d("home_activity", "EnableMyLocationServices: ")
                val intent = Intent(this, GPSTracker::class.java)
                startService(intent)
            }
        }
    }

    private fun disableMyLocationServices() {
        Log.d("home_activity", "disableMyLocationServices: ")
        isLocationTrakerOn = false
        stopService(Intent(this, GPSTracker::class.java))
    }

    private fun requestPermission() {
        /**
         * Asks the user do accept location permission.
         */
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    override fun onStart() {
        super.onStart()
        Log.d("home_activity", "onStart: ")
        if (!isLocationTrakerOn) {
            EnableMyLocationServices()
        }

    }

    override fun onStop() {
        super.onStop()
        Log.d("home_activity", "onStop: ")
        disableMyLocationServices()
    }

    fun visibility(visibility: Int) {
        binding.bottomNav.visibility = visibility


    }

    override fun onBackPressed() {
        val currentFragment = navController.currentDestination?.id
        if (currentFragment == R.id.postFragment) {
            visibility(View.VISIBLE)
        }
        super.onBackPressed()
    }
}