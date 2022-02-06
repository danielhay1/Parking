package com.example.parking.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.translation.ViewTranslationRequest
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.parking.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var binding : FragmentLoginBinding? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater,container,false)
        clickListener()




        return binding?.root
    }

    private fun clickListener() {
        binding?.loginRegister?.setOnClickListener(BtnRegister)
    }

private val BtnRegister = View.OnClickListener {
   // it!!.findNavController().navigate(R.id.viewTransactionsAction)

}
}