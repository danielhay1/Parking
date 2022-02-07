package com.example.parking.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.parking.R
import com.example.parking.activities.HomeActivity
import com.example.parking.databinding.FragmentLoginBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var mAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        initValues()
        clickListener()


        return binding.root
    }

    private fun initValues() {
        mAuth = FirebaseAuth.getInstance()

    }


    private fun clickListener() {
        binding.loginRegister.setOnClickListener(btnRegister)
        binding.loginBTNLogin.setOnClickListener(btnLogin)
    }

    private val btnRegister = View.OnClickListener {
        passToAnotherFragment(R.id.action_loginFragment_to_registrationFragment)

    }



    private val btnLogin = View.OnClickListener { view ->

        val isPasswordProper = checkAndHandleEmptyPassword()
        val isEmptyEmail = checkAndHandleEmptyEmail(R.string.email_required)

        if (!isPasswordProper && !isEmptyEmail) {
            signInWithEmailAndPassword(getEmail(), getPassword())

            handleShowProgressBar(View.VISIBLE)

            hideKeyBoard(view)
        }

    }

    private fun hideKeyBoard(view: View) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
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

    private fun checkAndHandleEmptyPassword(): Boolean {
        val password = getPassword()
        var isEmptyOrBelowSixCharacters = true
        if (password == "" || password.length < 6) {
            binding.loginEDTPassword.isErrorEnabled = true
            setAndEnabledErrorMsgTextFiled(R.string.password_required, binding.loginEDTPassword)
        } else {
            binding.loginEDTPassword.isErrorEnabled = false
            isEmptyOrBelowSixCharacters = false
        }
        return isEmptyOrBelowSixCharacters
    }


    private fun checkAndHandleEmptyEmail(msg: Int): Boolean {
        val email = getEmail()
        var isEmptyEmail = true
        if (email == "") {
            binding.loginEDTEmail.isErrorEnabled = true
            setAndEnabledErrorMsgTextFiled(msg, binding.loginEDTEmail)
        } else {
            binding.loginEDTEmail.isErrorEnabled = false
            isEmptyEmail = false
        }
        return isEmptyEmail
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        activity?.let {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    it,
                    OnCompleteListener { task: Task<AuthResult?> ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val intent = Intent(activity, HomeActivity::class.java)
                            startActivity(intent)

                        } else {
                            //show finger print
                            handleShowProgressBar(View.INVISIBLE)
                            // If sign in fails, display a message to the user.
                            checkAndHandleEmptyEmail(R.string.invalid_email)
                        }
                    })
        }
    }

    private fun handleShowProgressBar(visibility: Int) {
        binding.loginProgress.visibility = visibility
    }

    private fun setAndEnabledErrorMsgTextFiled(msg: Int, textField: TextInputLayout?) {
        textField?.error = getString(msg)
        textField?.isErrorEnabled = true
    }


    private fun getEmail(): String {
        return binding.loginEDTEmail.editText?.text.toString()
    }

    private fun getPassword(): String {
        return binding.loginEDTPassword.editText?.text.toString()
    }


}