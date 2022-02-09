package com.example.parking.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.parking.InterFaces.GetMyPostsCallBack
import com.example.parking.R
import com.example.parking.comparables.postComparator
import com.example.parking.databinding.FragmentMyPostsBinding
import com.example.parking.objects.DBManager
import com.example.parking.objects.Post
import com.example.parking.recyclers.RecyclerPosts
import com.example.parking.utils.MyLocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyPostsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyPostsFragment : Fragment() , GetMyPostsCallBack{


    private lateinit var binding: FragmentMyPostsBinding
    private lateinit var  myPosts:ArrayList<Post>
    private lateinit var  dbManager: DBManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyPostsBinding.inflate(inflater , container , false)

        initValues()
        initRecycler()
        initCallBacks()
        getMyPostsFromDB()
        visibilityProgressBarMyPost(View.VISIBLE)
        return binding.root
    }




    private fun getMyPostsFromDB() {
        dbManager.getMyPostsFromDB()
    }

    private fun initCallBacks() {
        dbManager.initGetMyPostCallBack(this)
    }

    private fun initRecycler() {
        val mAdapter = RecyclerPosts(myPosts, object : RecyclerPosts.NavigateBtnCallback {
            override fun onNavigateBtnClick(latLng: LatLng) {
                MyLocationServices.getInstance(requireContext()).navigate(requireActivity(),latLng)
            }
        })
        binding.myPostREVPosts.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.myPostREVPosts.adapter = mAdapter

    }

    private fun initValues() {
        myPosts = ArrayList()
        dbManager = DBManager()
    }

    override fun getMyPostsCallBack(posts: ArrayList<Post>) {
        //Collections.sort(posts , postComparator())
        myPosts.addAll(posts)
        binding.myPostREVPosts.adapter?.notifyItemRangeInserted(0, posts.size)
        visibilityProgressBarMyPost(View.GONE)

    }
    private fun visibilityProgressBarMyPost(visible: Int) {
        binding.myPostPRBAllPosts.visibility = visible
    }

}