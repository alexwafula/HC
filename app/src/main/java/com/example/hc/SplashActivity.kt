package com.example.hc

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.hc.auth.LoginActivity
import com.example.hc.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Delay the transition for 2 seconds (you can adjust the time)
        Handler(Looper.getMainLooper()).postDelayed({
            // Start the MainActivity with a side-slide animation
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left) // Slide animation
            finish() // Close SplashActivity
        }, 3000) // Delay time in milliseconds
    }
}