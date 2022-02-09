package com.example.parking.recyclers

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.parking.databinding.PostBinding
import com.example.parking.objects.MapManagerRecycler
import com.example.parking.objects.Post
import com.example.parking.objects.User
import com.example.parking.utils.AuthUtils
import com.example.parking.utils.FirestoreManager
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.SupportMapFragment

class RecyclerPosts(var postList: ArrayList<Post>): RecyclerView.Adapter<RecyclerPosts.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(postList[position])

    }

    override fun getItemCount(): Int {
        return postList.size
    }


    //the class is hodling the list view
    class ViewHolder(private var binding: PostBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(post: Post) {
            binding.POSTTVTitle.text = post.locationParking
            setImageUserWithUriGlide(post.imageParking , binding.POSTIMGParking)
            binding.POSTTVDescription.text = post.infoParking
            initMap(binding.map , post.latitude , post.longitude)
            setUsername(post.currentUserId)
            setUserImage(post.currentUserId)
        }




        private fun initMap(map: MapView, latitude: String, longitude: String) {
            MapManagerRecycler(map , latitude , longitude)
        }

        private fun setUsername(uid: String) {
            FirestoreManager().getUserFromFireStore(uid,object :
                FirestoreManager.UserReadyCallback {
                override fun onUserReady(user: User) {
                    binding.postTVUserName.setText(user.fullName)
                    Log.d("profile_fragment", "onUserReady: user: $user updated!")
                }
            })
        }

        private fun setUserImage(uid: String) {
            FirestoreManager().getUserPhotoFromFireStore(uid,object: FirestoreManager.CurrentUserImageCallBack{
                override fun currentUserImageCallBack(photo: String?) {
                    photo?.let {
                        if(!it.equals("")) {
                            setImageUserWithUriGlide(it,binding.postIMGUserImage)
                        }
                    }
                }
            })
        }

        private fun setImageUserWithUriGlide(stringImg: String , view:ImageView) {
            Log.d("profile_fragment", "setImageUserWithUriGlide: ")
            Glide.with(view.context)
                .load(stringImg)
                .centerCrop()
                .into(view)
        }
    }




}