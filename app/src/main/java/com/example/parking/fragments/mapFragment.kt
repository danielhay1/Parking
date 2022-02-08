package com.example.parking.fragments

import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.parking.databinding.FragmentMapBinding
import com.example.parking.receivers.LocationReceiver
import com.example.parking.utils.ImgLoader
import com.example.parking.utils.MyLocationServices
import com.example.parking.utils.MySignal
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class mapFragment : Fragment() {
    private lateinit var binding: FragmentMapBinding
    private var gMap: GoogleMap? = null
    private var locationChangedListener: OnLocationChangedListener? = null
    private var locationReceiver: LocationReceiver? = null
    private var myCurrentLatLng : LatLng? = null
    private val NORMAL_SCALE = 16
    private var autoFocusCurrentLocation = false


    public interface NewPostCallBack {  // TODO: Transfer to post fragment
        public fun onPostCreated(latLng: LatLng, notes: String)  //TODO: need to be adjust to post
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    init {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        var supportMapFragment = FragmentMapBinding.inflate(inflater, container, false)
        supportMapFragment?.let {
            val supportMapFragment = childFragmentManager.findFragmentById(it.gMap.id) as SupportMapFragment?
            supportMapFragment?.let{
                it.getMapAsync { map -> //When map is loaded
                    gMap = map
                    Log.d("map_fragment", "Map ready: ")
                    initMapFragment()
                    initViews()
//                    initViews()
//                    getUser()
//                    gpsEnabled = isGpsEnabled() //Ask to enable gps sensor if needed
//                    if (!MyLocationServices.getInstance().checkLocationPermission()) {
//                        requestPermision()
//                    } else {
//                        initMapFragment()
//                    }
                }
            }
        }
        return binding?.root
    }

    private fun initMapFragment() {
        gMap?.let {
            if (isGpsEnabled()) {
                Log.d("map_fragment", "initMapFragment: gpsEnabled")
                setupMap(it)
                setLocationSource(it)   //TODO: DEBUG
            }
        }
    }

    private fun initViews() {
        binding.mapBTNFollowMyCurrentLocation.setOnClickListener {
            autoFocusCurrentLocation = !autoFocusCurrentLocation
            Log.d("map_fragment", "autoFocusCurrentLocation = $autoFocusCurrentLocation")
            if (autoFocusCurrentLocation == false) {
                ImgLoader.getInstance(requireContext())
                    .loadImg("icon_mylocation_focus_gray", it as ImageView)
            } else {
                ImgLoader.getInstance(requireContext())
                    .loadImg("icon_mylocation_focus", it as ImageView)
            }
        }

    }

    fun addMarkerToMap(
        map: GoogleMap,
        currentLocation: LatLng,
        titleName: String,
        iconName: String?
    ): Marker? {
        /**
         * add marker to map, receive map, latlang and icon to set for marker and set it on map.
         * if marker received is null use default marker icon.
         */
        Log.d("pttt", "addMarkerToMap: (title=$titleName), (LatLng=$currentLocation)")
        val marker = MarkerOptions()
                .position(LatLng(currentLocation.latitude, currentLocation.longitude))
                .title(titleName)
        if (iconName != null) {
            val icon: Int = ImgLoader.getInstance(requireContext()).getImgIdentifier(iconName) //load icon
            val bitmapdraw = resources.getDrawable(icon, requireContext().applicationContext.theme) as BitmapDrawable
            val b = bitmapdraw.bitmap
            val smallMarker = Bitmap.createScaledBitmap(b, 150, 150, false)
            marker.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
        }
        return map.addMarker(marker)
    }

    private fun setLocationSource(map: GoogleMap) {
        map.setLocationSource(object : LocationSource {
            override fun activate(onLocationChangedListener: OnLocationChangedListener) {
                locationChangedListener = onLocationChangedListener
                registerLocationReceiver()
                Log.d("map_fragment", "activate: Tracking location")
            }

            override fun deactivate() {
                locationChangedListener = null
                Log.d("map_fragment", "deactivate: stop tracking location")
                MyLocationServices.getInstance(requireContext()).stopLocationUpdate(object :
                    MyLocationServices.CallBack_Location {
                    override fun locationReady(location: Location?) {
                        unRegisterLocationReceiver(locationReceiver)
                    }

                    override fun onError(error: String?) {
                        Log.d("map_fragment", "onError: $error")
                    }
                })
            }
        })
    }


    private fun isGpsEnabled(): Boolean {
        if (!MyLocationServices.getInstance(this.requireActivity().applicationContext).isGpsEnabled()) {
            MySignal.getInstance().alertDialog(this.requireActivity(),
                "GPS IS DISABLED",
                "Press \'Enable GPS\' to open gps settings",
                "Enable GPS",
                "Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    requireActivity().startActivity(
                        Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        )
                    )
                })
            return false
        }
        return true
    }

    private fun initCurrentLocation() {
        MyLocationServices.getInstance(this.requireContext()).setLastBestLocation(object :
            MyLocationServices.CallBack_Location {
            override fun locationReady(location: Location?) {
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    if (latLng.latitude != 0.0 || latLng.longitude != 0.0) {
                        Log.d("map_fragment", "locationReady: currentLocation = $latLng")
                        locationChangedListener?.let { it.onLocationChanged(location) }
                        setFocus(latLng, NORMAL_SCALE.toFloat())
                    }
                }

            }

            override fun onError(error: String?) {
                Log.d("map_fragment", "locationReady: onError = $error")
            }

        })
    }

    fun setFocus(focusLatlng: LatLng?, scale: Float) {
        val center = CameraUpdateFactory.newLatLng(focusLatlng!!)
        if (focusLatlng == null) {
            Log.d("map_fragment", "setFocus: NULL")
        } else {
            Log.d(
                "map_fragment",
                "setFocus: \tLatlng= " + focusLatlng.toString() + "Zoom scale=" + scale
            )
            val zoom = CameraUpdateFactory.zoomTo(scale)
            gMap?.moveCamera(center)
            gMap?.animateCamera(zoom)
        }
    }

    private fun setupMap(map: GoogleMap) {
        Log.d("map_fragment", "Setting up map")
        if (MyLocationServices.getInstance(requireContext()).checkLocationPermission()) {
            Log.d("map_fragment", "locationReady: setMyLocationTrue")
            map.setMyLocationEnabled(true)
        }
        initCurrentLocation()
    }



    private fun registerLocationReceiver() {
        locationReceiver = LocationReceiver(object : LocationReceiver.CallBack_LatLngUpdate {
            override fun latLngUpdate(latLng: LatLng?) {
                latLng?.let {
                    val location = Location("")
                    location.latitude = it.latitude
                    location.longitude = it.longitude
                    locationChangedListener?.onLocationChanged(location)
                    myCurrentLatLng = it
                    Log.d("map_fragment", "latLngUpdate: ${myCurrentLatLng}")
                    if (autoFocusCurrentLocation) {
                        setFocus(myCurrentLatLng, NORMAL_SCALE.toFloat())
                    }
                }
            }
        })

        val intentFilter = IntentFilter()
        intentFilter.addAction(LocationReceiver.CURRENT_LOCATION)
        this.requireActivity().registerReceiver(locationReceiver, intentFilter)
    }

    private fun unRegisterLocationReceiver(locationReceiver: LocationReceiver?) {
        locationReceiver?.let { this.requireActivity().unregisterReceiver(it) }

    }

    override fun onStop() {
        super.onStop()
        Log.d("map_fragment", "MAP FRAGMENT- onStop: ")
        unRegisterLocationReceiver(locationReceiver)
    }

}