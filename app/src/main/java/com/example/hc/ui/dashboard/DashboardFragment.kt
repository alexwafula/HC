package com.example.hc.ui.dashboard

import android.content.Intent
import android.graphics.Typeface
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar


import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hc.PlaylistDetailsActivity
import com.example.hc.R
import com.example.hc.adapters.PlaylistAdapter
import com.example.hc.databinding.FragmentDashboardBinding
import com.example.hc.models.SongRecommendation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PlaylistAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        progressBar = binding.progressBar // Bind the progress bar

        dashboardViewModel.autoLoadPlaylists(requireContext()) // Auto-load user playlists
        setupRecyclerView()
        setupFab()
        observePlaylists()
        handleIncomingLink() // Handle any incoming deep links

        return root
    }

    private fun setupRecyclerView() {
        adapter = PlaylistAdapter(emptyList()) { playlistId ->
            Log.d("DashboardFragment", "Playlist clicked with ID: $playlistId")  // Log the playlist ID when clicked
            navigateToPlaylistDetailsActivity(playlistId)
        }
        binding.recyclerViewPlaylists.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewPlaylists.adapter = adapter
    }

    private fun setupFab() {
        binding.fabCreatePlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    private fun showCreatePlaylistDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_playlist, null)
        dialogBuilder.setView(dialogView)

        val playlistNameInput = dialogView.findViewById<EditText>(R.id.playlistNameInput)
        dialogBuilder.setTitle("Create Playlist")
            .setPositiveButton("Create") { dialog, _ ->
                val playlistName = playlistNameInput.text.toString().trim()
                if (playlistName.isNotEmpty()) {
                    dashboardViewModel.addPlaylist(requireContext(), playlistName)
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Please enter a playlist name.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        dialogBuilder.create().show()
    }

    private fun observePlaylists() {
        // Observe the playlists from the ViewModel
        dashboardViewModel.playlists.observe(viewLifecycleOwner) { playlists ->

            // Hide the progress bar once data is loaded
            hideLoading()

            // Update the adapter with the new playlists
            adapter = PlaylistAdapter(playlists) { playlistId ->
                Log.d("DashboardFragment", "Playlist clicked with ID: $playlistId")  // Log the playlist ID when clicked
                navigateToPlaylistDetailsActivity(playlistId)
            }
            binding.recyclerViewPlaylists.adapter = adapter
        }
    }

    private fun handleIncomingLink() {
        val intent = activity?.intent
        if (intent?.action == Intent.ACTION_VIEW) {
            val data: Uri? = intent.data
            if (data != null && data.path == "/share") {
                val playlistId = data.getQueryParameter("playlistId")
                if (playlistId != null) {
                    Log.d("DashboardFragment", "Shared playlist ID: $playlistId")
                    loadPlaylistFromFirestore(playlistId)
                }
            }
        }
    }

    private fun loadPlaylistFromFirestore(playlistId: String) {
        showLoading() // Show progress bar while loading
        val firestore = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        firestore.collection("playlists").document(playlistId).get()
            .addOnSuccessListener { document ->
                hideLoading() // Hide progress bar
                if (document.exists()) {
                    val collaborators = document.get("collaborators") as? List<String> ?: emptyList()

                    if (!collaborators.contains(currentUserId)) {
                        // Add the user to collaborators after checking or creating the field
                        val updatedCollaborators = collaborators + currentUserId
                        document.reference.update("collaborators", updatedCollaborators)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "You are now collaborating on this playlist!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateToPlaylistDetailsActivity(playlistId)
                            }
                            .addOnFailureListener { e ->
                                Log.e("DashboardFragment", "Failed to update collaborators: ${e.message}")
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to join playlist. Please try again later.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        // User already a collaborator
                        navigateToPlaylistDetailsActivity(playlistId)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Playlist not found or unavailable.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { exception ->
                hideLoading() // Hide progress bar
                Log.e("DashboardFragment", "Error fetching playlist: ${exception.message}")
                Toast.makeText(
                    requireContext(),
                    "Failed to load playlist. Please try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    fun downloadRecommendationsAsPdf(recommendations: List<SongRecommendation>) {
        if (recommendations.isNotEmpty()) {
            val fileName = "playlist.pdf"
            val pdfFile = File(requireContext().cacheDir, fileName)

            try {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(700, 1000, 1).create()
                val page = pdfDocument.startPage(pageInfo)

                val canvas = page.canvas
                val paint = Paint().apply { isAntiAlias = true; color = Color.BLACK }

                // Header
                paint.textSize = 24f
                paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                canvas.drawText("Playlist Details", 150f, 50f, paint)

                // Subtitle
                paint.textSize = 16f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("Here is the playlist content:", 40f, 50f, paint)

                // Draw table headers
                paint.textSize = 18f // Larger table headers
                canvas.drawText("Title", 50f, 140f, paint)
                canvas.drawText("Artist", 300f, 140f, paint)
                canvas.drawLine(50f, 150f, 650f, 150f, paint) // Line below headers


                // Draw table content
                paint.textSize = 14f // Content text size
                var yPosition = 180f
                recommendations.forEach {
                    canvas.drawText(it.title, 50f, yPosition, paint)
                    canvas.drawText(it.artist, 300f, yPosition, paint)
                    yPosition += 40f
                }

                // Footer
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                canvas.drawText("Generated by Harmony Collective for ${FirebaseAuth.getInstance().currentUser?.email} at  ${System.currentTimeMillis()}", 50f, 960f, paint)

                pdfDocument.finishPage(page)

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
                    putExtra(Intent.EXTRA_SUBJECT, "Your Playlist Details")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Playlist"))

            } catch (e: Exception) {
                Log.e("DashboardFragment", "Error creating PDF file: ${e.message}", e)
                Toast.makeText(requireContext(), "Failed to create PDF", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(requireContext(), "No songs to save.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun navigateToPlaylistDetailsActivity(playlistId: String) {
        val intent = Intent(requireContext(), PlaylistDetailsActivity::class.java).apply {
            putExtra("playlistId", playlistId)
        }
        startActivity(intent)
    }

    // Show the progress bar while loading
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    // Hide the progress bar when done loading
    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
