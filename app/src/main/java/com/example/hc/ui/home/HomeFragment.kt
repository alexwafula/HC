package com.example.hc.ui.home

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.hc.R
import kotlinx.coroutines.flow.collect

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var avatarImageView: ImageView
    private lateinit var dailyMessageTextView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        avatarImageView = view.findViewById(R.id.avatarImageView)
        dailyMessageTextView = view.findViewById(R.id.dailyMessageTextView)

        // Observe the avatar URL and update ImageView with Glide
        lifecycleScope.launchWhenStarted {
            viewModel.avatar.collect { url ->
                if (url != null) {
                    Glide.with(requireContext())
                        .load(url)
                        .into(avatarImageView)
                }
            }
        }

        // Observe the daily message and update TextView
        lifecycleScope.launchWhenStarted {
            viewModel.message.collect { message ->
                dailyMessageTextView.text = message
            }
        }
    }
}
