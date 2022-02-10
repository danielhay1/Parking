package com.example.parking.fragments

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.parking.databinding.FragmentMapBinding
import com.example.parking.objects.Post
import com.example.parking.receivers.LocationReceiver
import com.example.parking.utils.FirestoreManager
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

    private val NORMAL_SCALE = 16
    private val PARKING_ICON = "car_png_marker"

    private lateinit var binding: FragmentMapBinding
    private var gMap: GoogleMap? = null
    private var locationChangedListener: OnLocationChangedListener? = null
    private var myCurrentLatLng : LatLng? = null
    private var autoFocusCurrentLocation = false

    private var locationReceiver = LocationReceiver(object : LocationReceiver.CallBack_LatLngUpdate {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("map_fragment", "onCreateView:")
        //initLocationListener()
        binding = FragmentMapBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        var supportMapFragment = FragmentMapBinding.inflate(inflater, container, false)
        supportMapFragment?.let {
            val supportMapFragment = childFragmentManager.findFragmentById(it.gMap.id) as SupportMapFragment?
            supportMapFragment?.let{
                it.getMapAsync { map -> //When map is loaded
                    gMap = map
                    Log.d("map_fragment", "Map ready: ")
                    initViews(map)
                    initMapFragment()
                    initAllPosts()
                }
            }
        }
        return binding.root
    }

    private fun initMapFragment() {
        gMap?.let {
            if (MyLocationServices.getInstance(this.requireActivity()).isGpsEnabledRequest(this.requireActivity())) {
                Log.d("map_fragment", "initMapFragment: gpsEnabled")
                setupMap(it)
                setLocationSource(it)   //TODO: DEBUG
            }
        }
    }

    private fun initViews(map: GoogleMap) {
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
        map.setOnMarkerClickListener {
            var markerLatLng = LatLng(it.position.latitude,it.position.longitude)
            navigate(myCurrentLatLng,markerLatLng)
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
        Log.d("map_fragment", "addMarkerToMap: (title=$titleName), (LatLng=$currentLocation)")
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
                //registerLocationReceiver()
                Log.d("map_fragment", "activate: Tracking location")
            }

            override fun deactivate() {
                locationChangedListener = null
                Log.d("map_fragment", "deactivate: stop tracking location")
                MyLocationServices.getInstance(requireContext()).stopLocationUpdate(object :
                    MyLocationServices.CallBack_Location {
                    override fun locationReady(location: Location?) {
                        //unRegisterLocationReceiver()
                    }

                    override fun onError(error: String?) {
                        Log.d("map_fragment", "onError: $error")
                    }
                })
            }
        })
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
            initCurrentLocation()
        }
    }

    private fun registerLocationReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(LocationReceiver.CURRENT_LOCATION)
        try {
            this.requireContext().registerReceiver(locationReceiver, intentFilter)
            //LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationReceiver!!, intentFilter)
            Log.d("map_fragment", "registerLocationReceiver: $locationReceiver")

        }   catch (e: IllegalArgumentException) {
            // already registered
            Log.e("map_fragment", "registerLocationReceiver: LocationReceiver already register")
        }

    }

    private fun unRegisterLocationReceiver() {
        locationReceiver?.let {
            try {
                this.requireContext().unregisterReceiver(it)
                //LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(it)
                Log.d("map_fragment", "unRegisterLocationReceiver: LocationListener unregistered.")
            } catch (e: IllegalArgumentException) {
                Log.e("map_fragment", "unRegisterLocationReceiver: unRegisterLocationReceiver call before registerLocationReceiver")

            }
        }
    }

    private fun addPostToMap(post: Post) {
        gMap?.let {
            /**
             * Callback method that call by click on element from list view in ParkingHistoryFragment, method set marker of the history parking on map.
             */
            val latLng = LatLng(post.latitude.toDouble(), post.longitude.toDouble())
            addMarkerToMap(it, latLng, post.locationParking, PARKING_ICON)
        }
    }

    private fun initAllPosts() {
        FirestoreManager().getPostListFromDB(object : FirestoreManager.AllPostsCallback {
            override fun postListReady(list: MutableList<Post>) {
                Log.d("map_fragment", "postListReady: list= $list")
                for (post in list) {
                    addPostToMap(post)
                }
            }
        })
    }

    private fun navigate(source: LatLng?,dest: LatLng?) : Boolean {
        if(source!=null && dest!=null) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr="+source.latitude+","+source.longitude+"&daddr="+dest.latitude+","+dest.longitude)
            )
            startActivity(intent)
            return true
        }   else {
            return false
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("map_fragment", "MAP FRAGMENT- onDestroy: ")
    }


    override fun onStop() {
        super.onStop()
        Log.d("map_fragment", "MAP FRAGMENT- onStop: ")
    }


    override fun onStart() {
        super.onStart()
        Log.d("map_fragment", "MAP FRAGMENT- onStart: ")
    }

    override fun onResume() {
        super.onResume()
        Log.d("map_fragment", "MAP FRAGMENT- onResume: ")
        registerLocationReceiver()
    }

    override fun onPause() {
        super.onPause()
        Log.d("map_fragment", "MAP FRAGMENT- onPause: ")
        unRegisterLocationReceiver()
    }
}