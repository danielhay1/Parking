package com.example.parking.objects

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapManagerRecycler(mapView: MapView, val latitude:String, val longitude:String):
    OnMapReadyCallback {

    init {
        mapView.onCreate(null)
        mapView.getMapAsync(this)
        mapView.onResume()

    }

    companion object {
        private const val ZOOM = 18

    }


    override fun onMapReady(p0: GoogleMap) {
        val sydney = LatLng(latitude.toDouble(), longitude.toDouble())
        p0.addMarker(
            MarkerOptions()
                .position(sydney)
        )
        p0.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,ZOOM.toFloat()))

        p0.uiSettings.isScrollGesturesEnabled = false
    }
}