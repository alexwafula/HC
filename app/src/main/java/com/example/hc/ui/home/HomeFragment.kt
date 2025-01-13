package com.example.hc.ui.home

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hc.R
import com.example.hc.adapters.RecommendationAdapter
import com.example.hc.models.SongRecommendation
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var avatarImageView: ImageView
    private lateinit var dailyMessageTextView: TextView
    private lateinit var recommendationsRecyclerView: RecyclerView
    private lateinit var recommendationAdapter: RecommendationAdapter
    private lateinit var downloadButton: Button
    private lateinit var progressBar: ProgressBar

    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        avatarImageView = view.findViewById(R.id.avatarImageView)
        dailyMessageTextView = view.findViewById(R.id.dailyMessageTextView)
        recommendationsRecyclerView = view.findViewById(R.id.recommendationsRecyclerView)
        downloadButton = view.findViewById(R.id.downloadButton)
        progressBar = view.findViewById(R.id.progressBar)

        setupRecyclerView()
        // Show ProgressBar when fetching data
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launchWhenStarted {
            viewModel.avatar.collect { url ->
                if (url != null) {
                    Glide.with(requireContext())
                        .load(url)
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_icon)
                        .into(avatarImageView)
                    Log.d("HomeFragment", "Avatar updated: $url")
                } else {
                    avatarImageView.setImageResource(R.drawable.ic_profile_icon)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.message.collect { message ->
                dailyMessageTextView.text = message
                Log.d("HomeFragment", "Daily message updated: $message")
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.userRecommendations.collect { userRecommendations ->
                progressBar.visibility = View.GONE // Hide ProgressBar after data is fetched
                val currentUserRecommendations =
                    userRecommendations.find { it.userId == currentUserId }
                val top5Recommendations = currentUserRecommendations?.recommendations?.take(5) ?: emptyList()
                recommendationAdapter.updateRecommendationList(top5Recommendations)
                Log.d("HomeFragment", "Top 5 recommendations updated: $top5Recommendations")
            }
        }

        downloadButton.setOnClickListener {
            if (isUserLoggedIn()) {
                val recommendations = viewModel.userRecommendations.value
                    ?.find { it.userId == currentUserId }
                    ?.recommendations?.take(5)

                if (recommendations != null) {
                    Log.d("HomeFragment", "Download button clicked with recommendations: $recommendations")
                    downloadRecommendationsAsPdf(recommendations)
                } else {
                    Log.d("HomeFragment", "No recommendations available for download.")
                }
            } else {
                Log.d("HomeFragment", "Download button clicked but user is not logged in.")
            }
        }
    }

    private fun setupRecyclerView() {
        recommendationAdapter = RecommendationAdapter(emptyList()) { recommendation ->
            Log.d("HomeFragment", "Item clicked: ${recommendation.title} by ${recommendation.artist}")
            Log.d("HomeFragment", "Opening Spotify link: ${recommendation.preview_url}")
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(recommendation.preview_url))
                startActivity(intent)
                Log.d("HomeFragment", "Spotify link opened successfully.")
            } catch (e: Exception) {
                Log.e("HomeFragment", "Failed to open Spotify link: ${recommendation.preview_url}", e)
                Toast.makeText(requireContext(), "Unable to open the link. Please ensure Spotify or a browser is available.", Toast.LENGTH_LONG).show()
            }
        }
        recommendationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recommendationAdapter
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val loggedIn = FirebaseAuth.getInstance().currentUser != null
        Log.d("HomeFragment", "User logged in: $loggedIn")
        return loggedIn
    }

    private fun downloadRecommendationsAsPdf(recommendations: List<SongRecommendation>) {
        if (recommendations.isNotEmpty()) {
            val fileName = "recommendations.pdf"
            val pdfFile = File(requireContext().cacheDir, fileName)

            try {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(500, 400, 1).create()
                val page = pdfDocument.startPage(pageInfo)

                val canvas = page.canvas
                val paint = Paint().apply { isAntiAlias = true; color = Color.BLACK }

                // Header
                paint.textSize = 16f
                paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                canvas.drawText("User Recommendations", 80f, 30f, paint)

                // Subtitle
                paint.textSize = 12f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("Here are your personalized music recommendations:", 40f, 50f, paint)

                // Draw table headers
                paint.textSize = 14f
                canvas.drawText("Title", 30f, 80f, paint)
                canvas.drawText("Artist", 180f, 80f, paint)
                canvas.drawLine(30f, 85f, 270f, 85f, paint) // Line below headers

                // Draw table content
                paint.textSize = 12f
                var yPosition = 100f
                recommendations.forEach {
                    canvas.drawText(it.title, 30f, yPosition, paint)
                    canvas.drawText(it.artist, 180f, yPosition, paint)
                    yPosition += 20f
                }

                // Footer
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                canvas.drawText("These are recommendations for ${FirebaseAuth.getInstance().currentUser?.email} at ${System.currentTimeMillis()}", 30f, 370f, paint)

                pdfDocument.finishPage(page)

                // Write PDF to file
                FileOutputStream(pdfFile).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                pdfDocument.close()

                // Share PDF
                val fileUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    pdfFile
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    putExtra(Intent.EXTRA_SUBJECT, "Your Music Recommendations")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Recommendations"))

            } catch (e: Exception) {
                Log.e("HomeFragment", "Error creating PDF file: ${e.message}", e)
                Toast.makeText(requireContext(), "Failed to create PDF", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d("HomeFragment", "Recommendations list is empty. Nothing to download.")
            Toast.makeText(requireContext(), "No recommendations to save.", Toast.LENGTH_SHORT).show()
        }
    }


}


