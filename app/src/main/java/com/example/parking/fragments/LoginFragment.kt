package com.example.parking.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.parking.databinding.FragmentLoginBinding
import com.example.parking.databinding.FragmentMainBinding
import com.google.android.material.button.MaterialButton

class LoginFragment : Fragment() {
    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!
    // UI components:
    private var loginBtn : MaterialButton? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun initViews() {
        //var callback: (([Params]) -> ReturnType)? = null
        binding.loginBTNLogin.setOnClickListener(view -> {
            // do something here
        });
    }
    fun login() {
    }

}