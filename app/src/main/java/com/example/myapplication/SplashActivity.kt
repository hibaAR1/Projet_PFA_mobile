package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySplashscreenBinding // Update import

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 1000 // Splash screen delay in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use your actual layout file name here
        val binding = ActivitySplashscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use Handler to delay redirection
        Handler().postDelayed({
            // Start SignInActivity after the delay
            val intent = Intent(this@SplashActivity, SignInActivity::class.java)
            startActivity(intent)
            finish() // Finish Splashscreen activity to prevent user from coming back to it by pressing back button
        }, SPLASH_DELAY)
    }
}
