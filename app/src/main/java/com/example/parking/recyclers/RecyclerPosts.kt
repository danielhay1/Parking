package com.example.parking.recyclers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.parking.databinding.PostBinding
import com.example.parking.objects.MapManagerRecycler
import com.example.parking.objects.Post
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
            binding.POSTTVTitle.text = post.typeParking
            initMap(binding.map , post.latitude , post.longitude)

        }


        private fun initMap(map: MapView, latitude: String, longitude: String) {
            MapManagerRecycler(map , latitude , longitude)
        }
    }


}