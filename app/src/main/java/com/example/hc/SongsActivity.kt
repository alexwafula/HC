package com.example.hc

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hc.adapters.SongAdapter
import com.example.hc.databinding.ActivitySongsBinding
import com.example.hc.models.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SongsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySongsBinding
    private lateinit var adapter: SongAdapter
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val selectedSongs = mutableSetOf<Song>() // Songs selected for addition or removal
    private val currentPlaylistSongs = mutableSetOf<Song>() // Existing songs in the playlist

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchCurrentPlaylistSongs()
        fetchRecommendedSongs()
        setupSaveButton()
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter(
            emptyList(),
            currentPlaylistSongs,
            SongAdapter.Mode.ADD,
            onSongAction = { song, action ->
                when (action) {
                    SongAdapter.Action.ADD -> {
                        selectedSongs.add(song)
                        currentPlaylistSongs.add(song)
                    }
                    SongAdapter.Action.REMOVE -> {
                        selectedSongs.remove(song)
                        currentPlaylistSongs.remove(song)
                    }
                }
            }
        )
        binding.recyclerViewSongs.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSongs.adapter = adapter
    }

    private fun fetchCurrentPlaylistSongs() {
        showProgressBar()
        val playlistId = intent.getStringExtra("playlistId") ?: return
        db.collection("playlists").document(playlistId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val songs = document["songs"] as? List<Map<String, String>> ?: emptyList()
                    currentPlaylistSongs.clear()
                    currentPlaylistSongs.addAll(songs.map { Song(it["title"]!!, it["artist"]!!) })
                    adapter.updateSongs(currentPlaylistSongs.toList())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load playlist songs.", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener { hideProgressBar() }
    }

    private fun fetchRecommendedSongs() {
        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }
        showProgressBar()
        db.collection("users").document(userId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val recommendedSongs = document["recommended_songs"] as? List<Map<String, String>>
                    val songs = recommendedSongs?.map { songMap ->
                        Song(
                            title = songMap["title"] ?: "Unknown Title",
                            artist = songMap["artist"] ?: "Unknown Artist"
                        )
                    } ?: emptyList()
                    adapter.updateSongs(songs)
                } else {
                    Toast.makeText(this, "No recommended songs found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch songs: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener { hideProgressBar() }
    }

    private fun setupSaveButton() {
        binding.buttonAddSongs.setOnClickListener {
            val playlistId = intent.getStringExtra("playlistId")
            if (playlistId.isNullOrEmpty()) {
                Toast.makeText(this, "Invalid Playlist ID!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            savePlaylistChanges(playlistId)
        }
    }

    private fun savePlaylistChanges(playlistId: String) {
        showProgressBar()
        val playlistRef = db.collection("playlists").document(playlistId)

        // Convert the updated playlist to Firestore-compatible format
        val updatedSongsMap = currentPlaylistSongs.map {
            mapOf("title" to it.title, "artist" to it.artist)
        }

        playlistRef.set(mapOf("songs" to updatedSongsMap), SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Playlist updated successfully!", Toast.LENGTH_SHORT).show()
                navigateBackToDetails(playlistId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update playlist: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener { hideProgressBar() }
    }

    private fun navigateBackToDetails(playlistId: String) {
        val intent = Intent(this, PlaylistDetailsActivity::class.java)
        intent.putExtra("playlistId", playlistId)
        startActivity(intent)
        finish()
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }
}
