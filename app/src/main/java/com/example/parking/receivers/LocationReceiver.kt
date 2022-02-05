package com.example.parking.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson

class LocationReceiver : BroadcastReceiver() {
    companion object {
        val CURRENT_LOCATION:String?  = "CURRENT_LOCATION"
        val LOCATION :String? = "Location"
    }
    private var callBack_latLngUpdate: LocationReceiver.CallBack_LatLngUpdate? = null

    public interface CallBack_LatLngUpdate {
        fun latLngUpdate(latLng: LatLng?)
    }

    fun setLocationReceiver(callBack_latLngUpdate: CallBack_LatLngUpdate?) {
        this.callBack_latLngUpdate = callBack_latLngUpdate
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val latLng: String? = it.getStringExtra(LOCATION)
            latLng?.let {
                val gson = Gson()
                val currentLocation = gson.fromJson(it, LatLng::class.java)
                if (callBack_latLngUpdate != null && currentLocation != null) {
                    callBack_latLngUpdate!!.latLngUpdate(currentLocation!!)
                }
            }
        }
    }

}