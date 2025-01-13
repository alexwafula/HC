package com.example.hc

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hc.adapters.SongAdapter
import com.example.hc.databinding.ActivitySongsBinding
import com.example.hc.models.Song
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SongsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySongsBinding
    private lateinit var adapter: SongAdapter
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val selectedSongs = mutableSetOf<Song>()
    private val currentPlaylistSongs = mutableSetOf<Song>()

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
        adapter = SongAdapter(emptyList(), currentPlaylistSongs, SongAdapter.Mode.ADD) { song, isAdd ->
            if (isAdd) selectedSongs.add(song) else selectedSongs.remove(song)
        }
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
                    adapter.notifyDataSetChanged()
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

            addSongsToPlaylist(playlistId)
        }
    }

    private fun addSongsToPlaylist(playlistId: String) {
        showProgressBar()
        val playlistRef = db.collection("playlists").document(playlistId)

        lifecycleScope.launch(Dispatchers.IO) {
            playlistRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val existingSongs = document["songs"] as? List<Map<String, String>> ?: emptyList()
                        val newSongs = selectedSongs.map { song ->
                            mapOf(
                                "title" to song.title.orEmpty(),
                                "artist" to song.artist.orEmpty()
                            )
                        }
                        val updatedSongs = (existingSongs + newSongs).distinctBy { it["title"] to it["artist"] }

                        playlistRef.set(
                            mapOf("songs" to updatedSongs),
                            SetOptions.merge()
                        ).addOnSuccessListener {
                            Toast.makeText(this@SongsActivity, "Songs added successfully!", Toast.LENGTH_SHORT).show()
                            // Navigate back to PlaylistDetailsActivity
                            val intent = Intent(this@SongsActivity, PlaylistDetailsActivity::class.java)
                            intent.putExtra("playlistId", playlistId)
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener { e ->
                            Toast.makeText(this@SongsActivity, "Failed to add songs: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SongsActivity, "Playlist does not exist.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@SongsActivity, "Failed to fetch playlist: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener { hideProgressBar() }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }
}
