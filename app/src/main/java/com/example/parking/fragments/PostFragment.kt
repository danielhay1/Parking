package com.example.parking.fragments

import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aviadmini.quickimagepick.PickCallback
import com.aviadmini.quickimagepick.PickSource
import com.aviadmini.quickimagepick.QiPick
import com.bumptech.glide.Glide
import com.example.parking.InterFaces.ImageUriCallBack
import com.example.parking.R
import com.example.parking.activities.HomeActivity
import com.example.parking.databinding.FragmentMapBinding
import com.example.parking.databinding.FragmentPostBinding
import com.example.parking.objects.DBManager
import com.example.parking.objects.Post
import com.example.parking.receivers.LocationReceiver
import com.example.parking.utils.AuthUtils
import com.example.parking.utils.MyLocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.*

class PostFragment : Fragment() , ImageUriCallBack {

    private val LARGE_SCALE = 18
    private lateinit var binding: FragmentPostBinding
    private var photoUri: Uri? = null
    private var dbManager: DBManager? = null
    private lateinit var homeActivity: HomeActivity
    private var locationChangedListener: LocationSource.OnLocationChangedListener? = null
    private var locationReceiver: LocationReceiver? = null
    private var myCurrentLatLng : LatLng? = null
    //private val mapManagerPost: MapManagerPost? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container , false)



        initValues()
        initMap(inflater,container)
        initListeners()
        initObjects()
        initCallBacks()


        return binding.root
    }

    private fun initValues() {
        dbManager = DBManager()
        photoUri = null
        homeActivity = activity as HomeActivity
    }


    private fun initCallBacks() {
        dbManager?.initImageUriCallBack(this)
    }

    private fun initObjects() {
        dbManager = DBManager()
    }

    private fun initListeners() {
        binding.cameraIcon.setOnClickListener(takePhotoParking)
        binding.postBtn.setOnClickListener(postBtn)

    }


    private val postBtn =
        View.OnClickListener { v: View? -> handleRequestPublishNewPost() }


    private fun handleRequestPublishNewPost() {
        val emptyLocationParking = checkAndHandleEmptyLocationParking()
        val photoIsEmpty = checkAndHandleEmptyPhoto()
        if (!emptyLocationParking && !photoIsEmpty) {
            //save the photo in storage DB and return from call back the uri photo
            photoUri?.let { dbManager?.savePhotoToStorageUri(it) }
            homeActivity.onBackPressed()
        }
    }

    private fun checkAndHandleEmptyPhoto(): Boolean {
        return if (photoUri == null) {
            binding.errorMsgImageRequired.visibility = View.VISIBLE
            true
        } else {
            binding.errorMsgImageRequired.visibility = View.INVISIBLE
            false
        }
    }


    private fun checkAndHandleEmptyLocationParking(): Boolean {
        var emptyLocationParking = false
        if (getLocationParking() == "") {
            emptyLocationParking = true
            setAndEnabledErrorMsgParkingLocation()
        } else {
            binding.locationParking.isErrorEnabled = false
        }
        return emptyLocationParking
    }

    private fun setAndEnabledErrorMsgParkingLocation() {
        binding.locationParking.isErrorEnabled = true
        binding.locationParking.error = getString(R.string.location_required)
    }


    private fun getNewPostObject(imageUri: String): Post {
        val post = Post()
            post.locationParking = getLocationParking()
            post.imageParking = imageUri
            post.infoParking  = getMoreInfo()
            post.currentUserId = AuthUtils.getCurrentUser().toString()
            post.docId = System.currentTimeMillis().toString()

        myCurrentLatLng?.let {
            post.latitude = it.latitude.toString()
            post.longitude = it.longitude.toString()
        }
        return post
    }



    private fun getMoreInfo(): String {
        return binding.textMoreInfo.editText?.text.toString()
    }

    private fun getLocationParking(): String {
        return binding.locationParking.editText?.text.toString()
    }


    private val takePhotoParking =
        View.OnClickListener { v: View? -> dispatchTakePictureIntent() }


    private fun dispatchTakePictureIntent() {
        QiPick.`in`(this)
            .fromCamera()
    }


    private val mCallback: PickCallback = object : PickCallback {
        override fun onImagePicked(pPickSource: PickSource, pRequestType: Int, pImageUri: Uri) {
            photoUri = pImageUri
            glide(pImageUri)
            binding.cameraIcon.visibility = View.GONE
        }

        override fun onMultipleImagesPicked(pRequestType: Int, pImageUris: List<Uri>) {
            onImagePicked(PickSource.DOCUMENTS, pRequestType, pImageUris[0])
        }

        override fun onError(pPickSource: PickSource, pRequestType: Int, pErrorString: String) {}
        override fun onCancel(pPickSource: PickSource, pRequestType: Int) {}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        QiPick.handleActivityResult(
            context!!.applicationContext, requestCode, resultCode, data,
            mCallback
        )
    }

    private fun glide(imageUri: Uri) {
        Glide.with(this)
            .load(imageUri)
            .centerCrop()
            .into(binding.imageParking)
    }

    private fun initMap(inflater: LayoutInflater,container: ViewGroup?) {
        var supportMapFragment = FragmentPostBinding.inflate(inflater, container, false)
        supportMapFragment?.let {
            val supportMapFragment = childFragmentManager.findFragmentById(it.map.id) as SupportMapFragment?
            supportMapFragment?.let{
                it.getMapAsync { map -> //When map is loaded
                    Log.d("post_fragment", "Map ready: ")
                    if(MyLocationServices.getInstance(requireContext()).checkLocationPermission()) {    // check permission
                        if(MyLocationServices.getInstance(requireContext()).isGpsEnabledRequest(requireActivity())) {   //  check GPS enabled
                            map.setMyLocationEnabled(true)
                            setLocationSource(map)
                        }

                    }
                }
            }
        }
    }

    private fun setLocationSource(map: GoogleMap) {
        Log.d("post_fragment", "setLocationSource: ")
        map.setLocationSource(object : LocationSource {
            override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener) {
                locationChangedListener = onLocationChangedListener
                registerLocationReceiver(map)
                Log.d("post_fragment", "activate: Tracking location")
            }

            override fun deactivate() {
                locationChangedListener = null
                Log.d("post_fragment", "deactivate: stop tracking location")
                MyLocationServices.getInstance(requireContext()).stopLocationUpdate(object :
                    MyLocationServices.CallBack_Location {
                    override fun locationReady(location: Location?) {
                        unRegisterLocationReceiver()
                    }

                    override fun onError(error: String?) {
                        Log.d("post_fragment", "onError: $error")
                    }
                })
            }
        })
    }

    private fun registerLocationReceiver(map: GoogleMap) {
        locationReceiver = LocationReceiver(object : LocationReceiver.CallBack_LatLngUpdate {
            override fun latLngUpdate(latLng: LatLng?) {
                latLng?.let {
                    val location = Location("")
                    location.latitude = it.latitude
                    location.longitude = it.longitude
                    locationChangedListener?.onLocationChanged(location)
                    if (myCurrentLatLng == null) {
                        myCurrentLatLng = it
                        setFocus(map, it, LARGE_SCALE.toFloat())
                    } else {
                        myCurrentLatLng = it
                    }
                    Log.d("post_fragment", "latLngUpdate: ${myCurrentLatLng}")
                }
            }
        })

        val intentFilter = IntentFilter()
        intentFilter.addAction(LocationReceiver.CURRENT_LOCATION)
        this.requireActivity().registerReceiver(locationReceiver, intentFilter)
        if(MyLocationServices.getInstance(requireContext()).checkLocationPermission()) {
            Log.d("post_fragment", "registerLocationReceiver: mapSetLocationEnabled")
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

    fun setFocus(map: GoogleMap,focusLatlng: LatLng?, scale: Float) {
        val center = CameraUpdateFactory.newLatLng(focusLatlng!!)
        if (focusLatlng == null) {
            Log.d("post_fragment", "setFocus: NULL")
        } else {
            Log.d(
                "post_fragment",
                "setFocus: \tLatlng= " + focusLatlng.toString() + "Zoom scale=" + scale
            )
            val zoom = CameraUpdateFactory.zoomTo(scale)
            map.moveCamera(center)
            map.animateCamera(zoom)
        }
    }

    // get image uri callBack to save in post
    override fun imageUriCallBack(imageUri: String) {
        Log.d("DSFfdfd" , "Dfdssfd" + imageUri)
        saveNewPostInFireStore(imageUri)
    }


    private fun saveNewPostInFireStore(imageUri: String) {
        val post: Post = getNewPostObject(imageUri)
        dbManager?.saveNewPostInFireStore(post)
    }

    override fun onStart() {
        super.onStart()
        Log.d("post_fragment", "POST FRAGMENT- onStop: ")
    }

    override fun onStop() {
        super.onStop()
        Log.d("post_fragment", "POST FRAGMENT- onStop: ")
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