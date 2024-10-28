package com.example.hc.ui.notifications

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.hc.R
import com.example.hc.auth.LogininActivity
import com.google.firebase.auth.FirebaseAuth

class NotificationsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_notifications, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize ProgressBar
        progressBar = rootView.findViewById(R.id.progressBar)

        // Handle the Privacy Policy link click
        val privacyPolicyLink: TextView = rootView.findViewById(R.id.privacy_policy)
        privacyPolicyLink.setOnClickListener {
            openPrivacyPolicyInBrowser()
        }

        // Find the logout button in the layout
        val logoutButton: Button = rootView.findViewById(R.id.btn_logout)

        // Set a click listener on the logout button
        logoutButton.setOnClickListener {
            // Show the ProgressBar
            progressBar.visibility = View.VISIBLE

            // Log out the user from Firebase
            logoutUser()
        }

        return rootView
    }

    private fun logoutUser() {
        auth.signOut()  // Log out the user from Firebase

        // Hide the ProgressBar
        progressBar.visibility = View.GONE

        // Navigate to the Login Activity
        val intent = Intent(requireContext(), LogininActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun openPrivacyPolicyInBrowser() {
        val privacyUrl = "http://192.168.0.62:5000/privacy"  // Replace with your actual URL
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl))
        startActivity(intent)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}







