package com.rcappstudio.indoorfarming

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.rcappstudio.indoorfarming.databinding.ActivitySplashBinding
import com.rcappstudio.indoorfarming.views.activities.MainActivity
import com.rcappstudio.indoorfarming.views.activities.SignInActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivitySplashBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val colorFrom = ResourcesCompat.getColor(resources, R.color.white, null)
        val colorTo = ResourcesCompat.getColor(resources, R.color.white, null)

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 2000 // milliseconds

        colorAnimation.addUpdateListener { animator -> binding.rlRoot.setBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.addUpdateListener { animator -> binding.rlRoot.setBackgroundColor(animator.animatedValue as Int) }

        Handler().postDelayed({
            colorAnimation.start()
        }, 500)

        Handler().postDelayed({
            TransitionManager.beginDelayedTransition(binding.root, AutoTransition())
            binding.lottieAnimationView2.elevation = 5F
            binding.appName.visibility = View.GONE
            binding.knotIdea.visibility = View.VISIBLE

            Handler().postDelayed({
                if (FirebaseAuth.getInstance().currentUser != null) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this, SignInActivity::class.java))
                    finish()
                }
            }, 800)

        }, 3000)
    }
}