package com.example.parking.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson

class LocationReceiver(private val callBack_latLngUpdate: CallBack_LatLngUpdate) : BroadcastReceiver() {
    companion object {
        val CURRENT_LOCATION:String?  = "CURRENT_LOCATION"
        val LOCATION :String? = "Location"
    }

    public interface CallBack_LatLngUpdate {
        fun latLngUpdate(latLng: LatLng?)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val latLng: String? = it.getStringExtra(LOCATION)
            latLng?.let {
                val gson = Gson()
                val currentLocation = gson.fromJson(it, LatLng::class.java)
                Log.d("location_receiver", "onReceive: sending latlng=$latLng")
                if (callBack_latLngUpdate != null && currentLocation != null) {
                    Log.d("location_receiver", "onReceive: sending latlng=$latLng")
                    callBack_latLngUpdate!!.latLngUpdate(currentLocation!!)
                }
            }
        }
    }

}