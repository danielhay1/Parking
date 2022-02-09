package com.example.parking.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.parking.databinding.FragmentProfileBinding
import com.example.parking.objects.User
import com.example.parking.utils.AuthUtils
import com.example.parking.utils.FirestoreManager



class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val PERMISSION_CODE_READ = 20
    var mGetContent = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ActivityResultCallback {
            Log.d("profile_fragment", "uploadImage: uri=$it")
            setImageUserWithUriGlide(it)
            FirestoreManager().savePhotoToStorageUri(
                it,
                object : FirestoreManager.ImageReadyCallback {
                    override fun imageUriCallback(imageUri: String) {
                        Log.d("profile_fragment", "imageUriCallback: ")
                        FirestoreManager().updateUserPhotoInFireStore(imageUri)
                    }
                })
        })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater)
        initViews()
        displayUserDetails()
        AuthUtils.getCurrentUser()?.let {
            FirestoreManager().getUserPhotoFromFireStore(it,object: FirestoreManager.CurrentUserImageCallBack{
                override fun currentUserImageCallBack(photo: String?) {
                    photo?.let {
                        if(!it.equals("")) {
                            setImageUserWithUriGlide(it)
                        }
                    }
                }
            })
        }

        return binding?.root
    }

    private fun initViews() {
        binding.profileFABEditImage.setOnClickListener {
            uploadImage()
        }
        binding.profileBTNLogout.setOnClickListener {
            AuthUtils.logout(requireActivity())
        }
    }

    private fun displayUserDetails() {
        AuthUtils.getCurrentUser()?.let {
            FirestoreManager().getUserFromFireStore(it,object :
                FirestoreManager.UserReadyCallback {
                override fun onUserReady(user: User) {
                    binding.profileTVUsername.text = user.fullName
                    binding.profileTVEmail.text = user.email
                    Log.d("profile_fragment", "onUserReady: user: $user updated!")
                }
            })
        }
    }

    private fun checkPermission(): Boolean {
        val result = checkSelfPermission(
            this.requireActivity(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return if (result == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            false
        }
    }

    //Requesting permission
    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_CODE_READ
        )
    }

    fun uploadImage() {
        if(!checkPermission()) {
            Log.d("profile_fragment", "uploadImage: no permissions")
            requestStoragePermission()
        } else {
            mGetContent.launch("image/*")
        }
    }
    private fun openIntentActionImageCapture() {

    }

    private fun setImageUserWithUriGlide(uri: Uri) {
        Log.d("profile_fragment", "setImageUserWithUriGlide: ")
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.profileIMG)
    }
    private fun setImageUserWithUriGlide(stringImg: String) {
        Log.d("profile_fragment", "setImageUserWithUriGlide: ")
        Glide.with(this)
            .load(stringImg)
            .centerCrop()
            .into(binding.profileIMG)
    }
}