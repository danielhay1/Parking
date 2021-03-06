package com.example.parking.recyclers

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.parking.R
import com.example.parking.databinding.PostBinding
import com.example.parking.objects.DBManager
import com.example.parking.objects.MapManagerRecycler
import com.example.parking.objects.Post
import com.example.parking.objects.User
import com.example.parking.utils.AuthUtils
import com.example.parking.utils.FirestoreManager
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.like.LikeButton
import com.like.OnLikeListener
import java.math.RoundingMode.valueOf

class RecyclerPosts(var postList: ArrayList<Post>,private var navigateBtnCallback: NavigateBtnCallback) : RecyclerView.Adapter<RecyclerPosts.ViewHolder>() {

    interface NavigateBtnCallback {
        fun onNavigateBtnClick(latLng: LatLng)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding,navigateBtnCallback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(postList[position])

    }

    override fun getItemCount(): Int {
        return postList.size
    }


    //the class is hodling the list view
    class ViewHolder(private var binding: PostBinding,private var navigateBtnCallback: NavigateBtnCallback) : RecyclerView.ViewHolder(binding.root) {
        private val dbManager = DBManager()
        fun bindItems(post: Post) {
            binding.POSTTVTitle.text = post.locationParking
            setImageUserWithUriGlide(post.imageParking, binding.POSTIMGParking)
            binding.POSTTVDescription.text = post.infoParking
            initMap(binding.map, post.latitude, post.longitude)
            setUsername(post.currentUserId)
            setUserImage(post.currentUserId)
            binding.amountOfLikes.text = post.numLiking.toString()
            isCurrentUserMakeLike(binding.POSTTVLikes,binding.likeBtn, post)
            likeBtnListener(binding.amountOfLikes, binding.POSTTVLikes, binding.likeBtn, post)
            binding.postBTNNavigate.setOnClickListener({
                var latLng: LatLng = LatLng(post.latitude.toDouble(),post.longitude.toDouble())
                navigateBtnCallback.onNavigateBtnClick(latLng)
            })

        }


        private fun initMap(map: MapView, latitude: String, longitude: String) {
            MapManagerRecycler(map, latitude, longitude)
        }

        private fun setUsername(uid: String) {
            FirestoreManager().getUserFromFireStore(uid, object :
                FirestoreManager.UserReadyCallback {
                override fun onUserReady(user: User) {
                    binding.postTVUserName.setText(user.fullName)
                    Log.d("profile_fragment", "onUserReady: user: $user updated!")
                }
            })
        }

        private fun setUserImage(uid: String) {
            FirestoreManager().getUserPhotoFromFireStore(uid,
                object : FirestoreManager.CurrentUserImageCallBack {
                    override fun currentUserImageCallBack(photo: String?) {
                        photo?.let {
                            if (it != "") {
                                setImageUserWithUriGlide(it, binding.postIMGUserImage)
                            }
                        }
                    }
                })
        }

        private fun setImageUserWithUriGlide(stringImg: String, view: ImageView) {
            Log.d("profile_fragment", "setImageUserWithUriGlide: ")
            Glide.with(view.context)
                .load(stringImg)
                .centerCrop()
                .into(view)
        }

        private fun likeBtnListener(textViewNumLikes: TextView, textViewLike: TextView , likeBtn:LikeButton, post: Post) {
            likeBtn.setOnLikeListener(object : OnLikeListener {
                override fun liked(likeButton: LikeButton) {
                    textViewLike.setTextColor(ContextCompat.getColor(textViewLike.context,R.color.application_dark_blue))
                    post.numLiking += 1
                    AuthUtils.getCurrentUser()?.let { post.addWhoMakeLike(it) }
                    dbManager.updateLike(post)
                    textViewNumLikes.text = post.numLiking.toString()
                }

                override fun unLiked(likeButton: LikeButton) {
                    textViewLike.setTextColor(ContextCompat.getColor(textViewLike.context,R.color.gray))
                    if (post.numLiking > 0) {
                        post.numLiking -= 1
                        AuthUtils.getCurrentUser()?.let { post.deleteWhoDeleteLike(it) }
                        textViewNumLikes.text = post.numLiking.toString()
                    } else {
                        textViewNumLikes.text = 0.toString()
                    }
                    dbManager.deleteLikeFromFireStore(post)
                }
            })
        }
        private fun isCurrentUserMakeLike(
            textViewLike: TextView ,
            likeBtn:LikeButton,
            post: Post
        ) {
            if (post.isCurrentUserMakeLike()) {
                textViewLike.setTextColor(ContextCompat.getColor(textViewLike.context,R.color.application_dark_blue))
                likeBtn.isLiked = true
            } else {
                textViewLike.setTextColor(ContextCompat.getColor(textViewLike.context,R.color.gray))
                likeBtn.isLiked = false
            }
        }

        private fun navigate(source: LatLng?, dest: LatLng?) : Boolean {
            if(source!=null && dest!=null) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?saddr="+source.latitude+","+source.longitude+"&daddr="+dest.latitude+","+dest.longitude)
                )
                //startActivity(intent)
                return true
            }   else {
                return false
            }

        }
    }
}