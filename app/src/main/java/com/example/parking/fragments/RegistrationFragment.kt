package com.example.parking.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

import com.example.parking.R
import com.example.parking.databinding.FragmentRegistrationBinding
import com.example.parking.objects.DBManager
import com.example.parking.objects.User

import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.SignInMethodQueryResult
import java.lang.Exception
import java.util.*
import android.content.Intent
import com.example.parking.activities.HomeActivity


class RegistrationFragment : Fragment() {

    private lateinit var binding :FragmentRegistrationBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dbManager:DBManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentRegistrationBinding.inflate(inflater,container , false)
        initValues()
        clickListeners()



        return binding.root
    }

    private fun clickListeners() {
        binding.registerBTNRegister.setOnClickListener(btnRegister)
    }


    private val btnRegister = View.OnClickListener { view ->
        val isEmptyName = checkAndHandleEmptyFullName()
        val isEmptyEmail: Boolean = checkAndHandleEmptyEmail()
        val isEmptyOrBelowSixCharacters: Boolean = checkAndHandlePassword()
        // when the user inserted Good information
        // when the user inserted Good information
        if (!isEmptyName && !isEmptyEmail && !isEmptyOrBelowSixCharacters) {
            handleProgressBar(View.VISIBLE)
            hideKeyBoard(view)
            // check if email already exists
            checkEmailExistsOrNot(getEmail())
        }

    }

   private fun checkEmailExistsOrNot(email: String?) {
        mAuth.fetchSignInMethodsForEmail(email!!)
            .addOnCompleteListener { task: Task<SignInMethodQueryResult> ->
                if (task.isSuccessful) {
                    //when there isn't email account with this email
                    if (Objects.requireNonNull(
                            task.result.signInMethods
                        ).size == 0
                    ) {
                        binding.registerEDTEmail.isErrorEnabled = false
                        createAccountAuth(email, getPassword())

                        // when there is already account with this email
                    } else {
                        setAndEnabledErrorMsgTextFiled(
                            R.string.already_account,
                            binding.registerEDTEmail
                        )
                        handleProgressBar(View.INVISIBLE)
                    }
                    //when the email that was inserted not Invalid
                } else {
                    setAndEnabledErrorMsgTextFiled(R.string.invalid_email, binding.registerEDTEmail)
                    handleProgressBar(View.INVISIBLE)
                }
            }.addOnFailureListener { e: Exception? -> handleProgressBar(View.INVISIBLE) }
    }


    private fun createAccountAuth(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                requireActivity()
            ) { task: Task<AuthResult?> ->
                // when Sign in success, update UI with the signed-in user's information
                if (task.isSuccessful) {
                    //save the name , email  , password of the new user;
                    FirebaseAuth.getInstance().currentUser?.let {userId->
                        saveNewUserInFireStore(
                            getFullName(),
                            email,
                            userId.uid
                        )
                    }
                   // saveLogInDetailSharedPref(email, password)
                    val intent = Intent(activity, HomeActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                    // when sign is fails
                } else {
                    //  display a message to the user.
                    Toast.makeText(
                        activity, R.string.authentication_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    handleProgressBar(View.GONE)
                }
            }
    }


    private fun saveNewUserInFireStore(fullName: String, email: String, userId: String) {

        dbManager.saveNewUserToFireStore(User(fullName , email ,userId ))
    }

    private fun hideKeyBoard(view: View) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun handleProgressBar(visibility: Int) {

        binding.registerProgress.visibility = visibility
    }

    private fun checkAndHandleEmptyEmail(): Boolean {
        val email: String = getEmail()
        var isEmptyEmail = true
        if (email == "") {
            setAndEnabledErrorMsgTextFiled(R.string.email_required, binding.registerEDTEmail)
        } else {
            isEmptyEmail = false
            binding.registerEDTEmail.isErrorEnabled = false
        }
        return isEmptyEmail
    }

    private fun checkAndHandleEmptyFullName(): Boolean {
        val name: String = getFullName()
        var isEmptyName = true
        if (name == "") {
            setAndEnabledErrorMsgTextFiled(R.string.name_required, binding.registerEDTName)
        } else {
            isEmptyName = false
            binding.registerEDTName.isErrorEnabled = false

        }
        return isEmptyName
    }


   private fun checkAndHandlePassword(): Boolean {
        val password: String = getPassword()
        var isEmptyOrBelowSixCharacters = true
        if (password == "" || password.length < 6) {
            setAndEnabledErrorMsgTextFiled(R.string.password_required, binding.registerEDTPassword)
        } else {
            isEmptyOrBelowSixCharacters = false
            binding.registerEDTPassword.isErrorEnabled = false
        }
        return isEmptyOrBelowSixCharacters
    }

    private fun initValues() {
        mAuth = FirebaseAuth.getInstance()
        dbManager = DBManager()
    }

    private fun getPassword(): String {

        return binding.registerEDTPassword.editText?.text.toString()
    }

    private fun getFullName(): String {
        return binding.registerEDTName.editText?.text.toString()
    }

    private fun setAndEnabledErrorMsgTextFiled(msg: Int, textField: TextInputLayout) {
        textField.error = getString(msg)
        textField.isErrorEnabled = true
    }

    private fun getEmail(): String {
        return binding.registerEDTEmail.editText?.text.toString()
    }

}