package com.example.parking.activities

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment

import com.example.parking.R


import com.example.parking.databinding.ActivityHomeBinding
import com.example.parking.services.GPSTracker
import com.example.parking.utils.MyLocationServices
import com.example.parking.utils.MySignal
import com.google.android.material.navigation.NavigationBarView


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
        gpsEnabled = isGpsEnabled()
        EnableMyLocationServices()
        initListeners()


    }

    private fun initValues() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.home_FCV) as NavHostFragment
        navController = navHostFragment.navController

    }

    private fun initListeners() {
        binding.bottomNav.setOnItemSelectedListener(itemSelected)

    }


    private val itemSelected = NavigationBarView.OnItemSelectedListener { item ->

        val currentFragment = navController.currentDestination?.id
        val itemId = item.itemId

        if (currentFragment == R.id.homeFragment2) {
            when (itemId) {
                R.id.mapPage -> replaceFragment(R.id.action_homeFragment2_to_mapFragment)
                R.id.myPostsPage -> replaceFragment(R.id.action_homeFragment2_to_myPostsFragment)
                R.id.profilePage -> replaceFragment(R.id.action_homeFragment2_to_profileFragment)
            }
        }

        if (currentFragment == R.id.myPostsFragment) {
            when (itemId) {
                R.id.mapPage -> replaceFragment(R.id.action_myPostsFragment_to_mapFragment)
                R.id.postsPage -> replaceFragment(R.id.action_myPostsFragment_to_homeFragment2)
                R.id.profilePage -> replaceFragment(R.id.action_myPostsFragment_to_profileFragment)
            }
        }
        if (currentFragment == R.id.mapFragment) {
            when (itemId) {
                R.id.myPostsPage -> replaceFragment(R.id.action_mapFragment_to_myPostsFragment)
                R.id.postsPage -> replaceFragment(R.id.action_mapFragment_to_homeFragment2)
                R.id.profilePage -> replaceFragment(R.id.action_mapFragment_to_profileFragment)
            }


        }
        if (currentFragment == R.id.profileFragment) {
            when (itemId) {
                R.id.myPostsPage -> replaceFragment(R.id.action_profileFragment_to_myPostsFragment)
                R.id.postsPage -> replaceFragment(R.id.action_profileFragment_to_homeFragment2)
                R.id.mapPage -> replaceFragment(R.id.action_profileFragment_to_mapFragment)
            }


        }
        //else if()


//        Log.d("dsffdsfdsfsdfsd" , "FDfdssfd" + navHostFragment!!.childFragmentManager.fragments[0].id)
//        Log.d("FDfsdfs" , "dfssfd " + R.id.mapFragment)
//        if(navHostFragment is R.id.mapFragment) {
//
//
//        }
//        Log.d("FDfsdfs" , "dfssfd " )
//        val navHost = supportFragmentManager.findFragmentById(R.id.bottomNav)
//        navHost?.let { navFragment ->
//            navFragment.childFragmentManager.primaryNavigationFragment?.let {fragment->
//
//            }
//        }
//        val navHostFragment: Fragment? =
//            supportFragmentManager.findFragmentById(R.id.home_FCV)
//        navHostFragment?.childFragmentManager?.fragments?.get(0)
//        Log.d("FDfsdfs" , "dfssfd " +navHostFragment?.id)
//        Log.d("FDfsdfs" , "dfssfd " + R.id.mapFragment)

//        val host = NavHostFragment.create(R.navigation.nav_internal)
//        supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment,host).setPrimaryNavigationFragment(host).commit()

        // val currentFragment = NavHostFragment.findNavController(R.navigation.nav_internal).currentDestination?.id


        when (item.itemId) {


        }
        true
    }

    private fun replaceFragment(action: Int) {
        val options = NavOptions.Builder()
            .setPopUpTo(navController.currentDestination!!.id, true)
            .setLaunchSingleTop(true)
            .build()
        navController.navigate(action, null, options)

    }


    private fun passToAnotherFragment(action: Int) {


//        val host = NavHostFragment.create(action)
//        supportFragmentManager.beginTransaction().replace(R.id.homeFragment2, host).setPrimaryNavigationFragment(host).commit()
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

    private fun isGpsEnabled(): Boolean {
        if (!MyLocationServices.getInstance(this.applicationContext).isGpsEnabled()) {
            MySignal.getInstance().alertDialog(this,
                "GPS IS DISABLED",
                "Press \'Enable GPS\' to open gps settings",
                "Enable GPS",
                "Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    this.startActivity(
                        Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        )
                    )
                })
            return false
        }
        return true
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


}