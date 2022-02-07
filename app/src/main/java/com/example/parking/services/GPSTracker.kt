package com.example.parking.services

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.os.IBinder
import android.util.Log
import com.example.parking.receivers.LocationReceiver
import com.example.parking.utils.MyLocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson

class GPSTracker : Service() {
    private var currentLocation: LatLng? = null
    private val isTracking = false
    private val locationListener =
        LocationListener { location ->
            currentLocation = LatLng(location.latitude, location.longitude)
            currentLocation?.let {
                Log.d("gps_tracker", "GPS Traker: Location= " + it.toString())
                sendCurrentLocation(it)
            }
        }
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val location: Location = locationResult.getLastLocation()
            if (location.latitude != 0.0 || location.longitude != 0.0) {
                if (locationListener != null) {
                    locationListener.onLocationChanged(location)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("gps_tracker", "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        Thread {
            Log.d("gps_tracker", "Working Thread: " + Thread.currentThread().name)
            updateCurrentLocation()
        }.start()
        return START_STICKY
    }

    private fun sendCurrentLocation(latLng: LatLng) {
        val gson = Gson()
        val currentLoc: String = gson.toJson(latLng)
        val intent = Intent(LocationReceiver.CURRENT_LOCATION)
        intent.putExtra(LocationReceiver.LOCATION, currentLoc)
        sendBroadcast(intent)
    }

    private fun updateCurrentLocation() {
        if (MyLocationServices.getInstance(applicationContext).isGpsEnabled()) {
            MyLocationServices.getInstance(applicationContext).startLocationUpdateds(object :
                MyLocationServices.CallBack_Location {
                override fun locationReady(location: Location?) {
                }

                override fun onError(error: String?) {
                    Log.d("gps_tracker", "onError: $error")
                }
            },locationCallback)
        }
    }

    fun stopUpdateCurrentLocation() {
        Log.d("gps_tracker", "stopUpdateCurrentLocation")
        MyLocationServices.getInstance(applicationContext).stopLocationUpdate(object :
            MyLocationServices.CallBack_Location {
            override fun locationReady(location: Location?) {
            }

            override fun onError(error: String?) {
                Log.d("gps_tracker", "onError: $error")
            }

        })
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun stopService(name: Intent?): Boolean {
        return super.stopService(name)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopUpdateCurrentLocation()
    }
}