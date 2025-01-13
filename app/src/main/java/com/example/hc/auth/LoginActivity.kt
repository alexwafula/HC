package com.example.hc.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hc.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle Sign-up button click to navigate to SigninActivity
        binding.SignupButton.setOnClickListener {
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }

        // Handle Log-in button click to navigate to LogininActivity
        binding.LoginButton.setOnClickListener {
            val intent = Intent(this, LogininActivity::class.java)
            startActivity(intent)
        }

        binding.ForgotPasswordButton.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}
