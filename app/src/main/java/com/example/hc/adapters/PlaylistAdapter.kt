package com.example.hc.adapters

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.hc.R
import com.example.hc.auth.LoginActivity
import com.example.hc.models.Playlist
import com.example.hc.models.SongRecommendation
import com.example.hc.ui.dashboard.DashboardFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlaylistAdapter(
    private val playlists: List<Playlist>,
    private val onPlaylistClick: (String) -> Unit // Callback for handling clicks
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.playlistName.text = playlist.name

        // Set up click listener for each playlist
        holder.itemView.setOnClickListener {
            onPlaylistClick(playlist.playlistId) // Pass the playlist ID to the callback
        }

        // Set up click listener for the vertical three dots (TextView)
        holder.itemView.findViewById<TextView>(R.id.textMoreOptions).setOnClickListener { view ->
            // Create a PopupMenu for the options
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.playlist_options_menu) // Assuming you have a menu resource

            // Handle menu item clicks
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.share_playlist -> {
                        // Share Playlist logic
                        sharePlaylist(playlist, view)
                        true
                    }
                    R.id.download_playlist -> {
                        downloadPlaylistAsPdf(playlist, view)
                        true
                    }
                    R.id.delete_playlist -> {
                        deletePlaylist(playlist, view)
                        true
                    }
                    else -> false
                }
            }

            // Show the menu
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int = playlists.size

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playlistName: TextView = itemView.findViewById(R.id.textPlaylistName)
    }

    private fun sharePlaylist(playlist: Playlist, view: View) {
        Log.d("SharePlaylist", "Share playlist function called for ${playlist.name}")

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(view.context, "You must be logged in to share a playlist", Toast.LENGTH_SHORT).show()
            val intent = Intent(view.context, LoginActivity::class.java)
            view.context.startActivity(intent)
            return
        }

        // Generate a shareable link
        val shareableLink = "https://harmonycollective.com/share?playlistId=${playlist.playlistId}"

        // Use Android's Share Intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out my playlist on Harmony Collective!")
            putExtra(Intent.EXTRA_TEXT, "Hey! Check out this playlist: $shareableLink")
        }

        // Launch the sharing dialog
        view.context.startActivity(Intent.createChooser(shareIntent, "Share Playlist"))
    }

    private fun downloadPlaylistAsPdf(playlist: Playlist, view: View) {
        Log.d("DownloadPlaylist", "Attempting to download playlist: ${playlist.name} (ID: ${playlist.playlistId})")

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("playlists").document(playlist.playlistId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("DownloadPlaylist", "Playlist document found: ${document.id}")
                    val songs = document.get("songs") as? List<Map<String, String>> ?: emptyList()
                    val recommendations = songs.map {
                        SongRecommendation(it["title"] ?: "Unknown Title", it["artist"] ?: "Unknown Artist")
                    }
                    Log.d("DownloadPlaylist", "Starting PDF download for playlist: ${playlist.name}")
                    (view.context as? DashboardFragment)?.downloadRecommendationsAsPdf(recommendations)
                } else {
                    Log.w("DownloadPlaylist", "Playlist document does not exist for ID: ${playlist.playlistId}")
                    Toast.makeText(view.context, "Playlist not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DownloadPlaylist", "Error fetching playlist: ${exception.message}")
                Toast.makeText(view.context, "Failed to download playlist.", Toast.LENGTH_SHORT).show()
            }

        // Add a log to indicate if download has not started
        Log.d("DownloadPlaylist", "Download process initiated, waiting for success or failure.")
    }

    private fun deletePlaylist(playlist: Playlist, view: View) {
        // Show a confirmation dialog before deletion
        AlertDialog.Builder(view.context)
            .setTitle("Delete Playlist")
            .setMessage("Are you sure you want to delete the playlist '${playlist.name}'?")
            .setPositiveButton("Yes") { _, _ ->
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("playlists").document(playlist.playlistId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(view.context, "Playlist '${playlist.name}' deleted successfully.", Toast.LENGTH_SHORT).show()

                        // Update the UI (remove the deleted playlist from the list)
                        val updatedPlaylists = playlists.toMutableList()
                        updatedPlaylists.remove(playlist)
                        (playlists as MutableList).clear()
                        playlists.addAll(updatedPlaylists)
                        notifyDataSetChanged()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(view.context, "Failed to delete playlist: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

}

