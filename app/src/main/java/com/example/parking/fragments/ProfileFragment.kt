package com.example.parking.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.parking.R
import com.example.parking.databinding.FragmentMapBinding
import com.example.parking.databinding.FragmentProfileBinding
import com.example.parking.utils.AuthUtils


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding?.root
    }

    private fun initViews() {
        binding.floatingActionButton.setOnClickListener {

        }
        binding.profileBTNLogout.setOnClickListener {

        }
        binding.profileBTNSettings.setOnClickListener {

        }
    }

    private fun getUser() {
        var uid: String? = AuthUtils.getCurrentUser()
        
    }



}