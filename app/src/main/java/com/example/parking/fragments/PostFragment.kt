package com.example.parking.fragments

import android.content.Intent
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
import com.example.parking.databinding.FragmentPostBinding
import com.example.parking.objects.DBManager
import com.example.parking.objects.Post
import com.example.parking.utils.AuthUtils
import com.google.android.gms.maps.SupportMapFragment
import java.util.*

class PostFragment : Fragment() , ImageUriCallBack {

    private lateinit var binding: FragmentPostBinding
   // private val newPostCallBack: newPostCallBack? = null
    private var photoUri: Uri? = null
    private var dbManager: DBManager? = null
    private lateinit var homeActivity: HomeActivity
   // private val mapManagerPost: MapManagerPost? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container , false)



        initValues()
      //  activateMap()
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
//            if (newPostCallBack != null) {

//                newPostCallBack.activateLinearProgressBar()
//            }
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

//        if (mapManagerPost.getLocation() != null) {
//            post.setLatitude(java.lang.String.valueOf(mapManagerPost.getLocation().getLatitude()))
//            post.setLongitude(java.lang.String.valueOf(mapManagerPost.getLocation().getLongitude()))
//        }
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

//     activate map with current location
//    private fun activateMap() {
//        val supportMapFragment =
//            this.childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
//        mapManagerPost = MapManagerPost(context!!.applicationContext, supportMapFragment)
//        mapManagerPost.activateMapWithCurrentLocation()
//    }

    // get image uri callBack to save in post
    override fun imageUriCallBack(imageUri: String) {
        Log.d("DSFfdfd" , "Dfdssfd" + imageUri)
        saveNewPostInFireStore(imageUri)
    }


    private fun saveNewPostInFireStore(imageUri: String) {
        val post: Post = getNewPostObject(imageUri)
        dbManager?.saveNewPostInFireStore(post)
    }
    
//    fun initNewPOstFragmentCallBack(newPostCallBack: newPostCallBack?) {
//        this.newPostCallBack = newPostCallBack
//    }



}