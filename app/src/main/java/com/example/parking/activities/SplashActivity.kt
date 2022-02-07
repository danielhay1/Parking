package com.example.parking.activities

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import com.example.parking.databinding.ActivitySplashBinding
import com.example.parking.fragments.LoginFragment
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val animationDuration = 3000
    private lateinit var binding: ActivitySplashBinding
    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initValues()
        //  findView();
        showView(binding.splashIMGLogo)
    }

    private fun initValues() {
        mAuth = FirebaseAuth.getInstance()
    }



   private fun showView(view: View) {
        view.alpha = 0.0f
        view.animate()
            .alpha(1.0f)
            .setDuration(animationDuration.toLong())
            .setInterpolator(LinearInterpolator())
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    view.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    startApp()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
    }

    private fun startApp() {
        val mIntent: Intent?

        if(mAuth.currentUser!= null) {
            mIntent = Intent(baseContext, HomeActivity::class.java)
        }else {
            mIntent = Intent(baseContext, MainActivity::class.java)
        }
        startActivity(mIntent)
        finish()
    }


}