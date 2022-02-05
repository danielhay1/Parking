package com.example.parking.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class MyLocationServices(private val appContext: Context) {
    private var req: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    public interface CallBack_Location {
        fun locationReady(location: Location?)
        fun onError(error: String?)
    }

    companion object {
        @Volatile private var instance : MyLocationServices? = null
        @Volatile private var locationManager: LocationManager? = null
        @Volatile private var fusedLocationProviderClient: FusedLocationProviderClient? = null
        @Synchronized
        fun getInstance(context: Context) : MyLocationServices {
            if (instance == null) {
                instance = MyLocationServices(context.applicationContext)   // Avoid leaking activites
                locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

            }
            return instance as MyLocationServices
        }

    }
    init {
        if (!isGpsEnabled()) {
            //GPS is not enabled !!
            Log.d("pttt", "GPS is not enabled")
        }
    }
    public fun isGpsEnabled(): Boolean {
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun setLocationCallBack(locationCallback: LocationCallback) {
        this.locationCallback = locationCallback
    }

    fun startLocationUpdateds(callBack_location: CallBack_Location?,locationCallback: LocationCallback?) {
        if (!checkLocationPermission()) {
            callBack_location?.onError("LOCATION PERMISSION IS NOT ALLOW")
            return
        }
        locationCallback?.let {
            setLocationCallBack(it)
            if (req == null) {
                req = LocationRequest.create().apply {
                    interval = 4000
                    fastestInterval = 2000
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    maxWaitTime = 4000
                }
                fusedLocationProviderClient?.requestLocationUpdates(
                    req!!,
                    it,
                    Looper.getMainLooper()
                )
            } else {
                callBack_location!!.onError("Device is already receiving location updates")
            }
        }
    }

    fun setLastBestLocation(callBack_location: CallBack_Location?) {
        /**
         * Method sample current location and use callBack_location when location is ready
         */
        if (!checkLocationPermission()) {
            callBack_location?.onError("LOCATION PERMISSION IS NOT ALLOW")
            return
        }
        fusedLocationProviderClient?.let {
            val locationTask = it.lastLocation
            locationTask.addOnSuccessListener { location -> //Location found
                locationSuccess(callBack_location, location)
            }.addOnFailureListener { e -> locationFailure(callBack_location, e) }
        }
    }

    fun checkLocationPermission(): Boolean {
        val res = (ActivityCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED)
        Log.d("pttt", "permission=$res")
        return res
    }

    private fun locationSuccess(callBack_location: CallBack_Location?, location: Location?) {
        location?.let {
            callBack_location?.locationReady(it)
        }   ?: run {
            callBack_location?.onError("Location is null")
        }
    }

    private fun locationFailure(callBack_location: CallBack_Location?, e: Exception) {
        callBack_location?.onError(e.localizedMessage)
    }

    fun stopLocationUpdate(callBack_location: CallBack_Location) {
        if (fusedLocationProviderClient != null && req != null) {
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback!!)
            req = null
            locationCallback = null
        } else {
            callBack_location.onError("Location update not requested")
        }
    }
}


