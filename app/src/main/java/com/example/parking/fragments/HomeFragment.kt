package com.example.parking.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parking.InterFaces.GetAllPostCallBack
import com.example.parking.R
import com.example.parking.activities.HomeActivity
import com.example.parking.comparables.postComparator
import com.example.parking.databinding.FragmentHomeBinding
import com.example.parking.objects.DBManager
import com.example.parking.objects.Post
import com.example.parking.recyclers.RecyclerPosts
import com.example.parking.utils.MyLocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : Fragment() , GetAllPostCallBack {

    private lateinit var binding:FragmentHomeBinding
    private lateinit var homeActivity:HomeActivity
    private lateinit var dbManager:DBManager
    private lateinit var  allPost:ArrayList<Post>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container , false)
        listeners()
        initValues()
        initCallBack()
        getPostFromDB()
        initRecycler()
        visibilityProgressBarAllPost(View.VISIBLE)
        return binding.root
    }

    private fun visibilityProgressBarAllPost(visible: Int) {
        binding.homePRBAllPosts.visibility = visible
    }


    private fun initRecycler() {
        val adapter = RecyclerPosts(allPost,object : RecyclerPosts.NavigateBtnCallback {
            override fun onNavigateBtnClick(latLng: LatLng) {
                MyLocationServices.getInstance(requireContext()).navigate(requireActivity(),latLng)
            }
        })
        binding.homeREVPosts.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.homeREVPosts.adapter = adapter
    }

    private fun initCallBack() {
        dbManager.initGetAllPostCallBack(this)
    }

    private fun getPostFromDB() {
        dbManager.getAllPost()
    }

    private fun initValues() {
        homeActivity = activity as HomeActivity
        dbManager = DBManager()
        allPost = ArrayList()
    }

    private fun listeners() {
        binding.includeHomeParking.topAppBar.setOnMenuItemClickListener(clickItem)
    }

    private val clickItem = Toolbar.OnMenuItemClickListener { item: MenuItem ->
        val id = item.itemId
        if (id == R.id.postFragment) {
            passToAnotherFragment(R.id.action_homeFragment2_to_postFragment)
            homeActivity.visibility(View.GONE)
        }
        true
    }

    private fun passToAnotherFragment(action: Int) {
        findNavController().navigate(
            action,
            null,
            navOptions { // Use the Kotlin DSL for building NavOptions
                anim {
                    enter = android.R.animator.fade_in
                    exit = android.R.animator.fade_out
                }
            }
        )
    }


    override fun getAllPostCallBack(posts: ArrayList<Post>) {
        Collections.sort(posts , postComparator())
        this.allPost.addAll(posts)
        binding.homeREVPosts.adapter?.notifyItemRangeInserted(0, posts.size)
        visibilityProgressBarAllPost(View.GONE)
    }




}