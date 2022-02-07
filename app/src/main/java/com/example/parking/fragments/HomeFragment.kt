package com.example.parking.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parking.R
import com.example.parking.databinding.FragmentHomeBinding
import com.example.parking.objects.Post
import com.example.parking.recyclers.RecyclerPosts
import java.util.ArrayList


class HomeFragment : Fragment() {

    private lateinit var binding:FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container , false)

        val allpost: ArrayList<Post> = ArrayList()
        allpost.add(Post("Fdsfsd"))
        val obj_adapter = RecyclerPosts(allpost)
        
        binding.homeREVPosts.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.homeREVPosts.adapter = obj_adapter


        return binding.root
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
}