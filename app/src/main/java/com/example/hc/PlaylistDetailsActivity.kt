package com.example.hc

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.SearchView
import com.example.hc.adapters.SongAdapter
import com.example.hc.databinding.ActivityPlaylistDetailsBinding
import com.example.hc.models.Song
import com.google.firebase.firestore.FirebaseFirestore

class PlaylistDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistDetailsBinding
    private val db = FirebaseFirestore.getInstance()
    private var playlistId: String? = null
    private var isCollaborator: Boolean = false
    private lateinit var adapter: SongAdapter
    private var allSongs: List<Song> = emptyList() // Original song list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playlistId = intent.getStringExtra("playlistId")
        isCollaborator = intent.getBooleanExtra("isCollaborator", false)

        if (playlistId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid Playlist ID!", Toast.LENGTH_SHORT).show()
            Log.e("PlaylistDetailsActivity", "Invalid Playlist ID!")
            finish()
            return
        }

        Log.d("PlaylistDetailsActivity", "Playlist ID: $playlistId")

        setupRecyclerView()
        setupSearchView()
        loadPlaylistSongs()
        setupAddSongsButton()
    }
    private fun setupRecyclerView() {
        // Use Mode.VIEW to ensure buttons are hidden and no action is needed
        adapter = SongAdapter(
            emptyList(),
            emptySet(),
            SongAdapter.Mode.VIEW,
            onSongAction = { _, _ ->
                // No action needed, as buttons will always be hidden in VIEW mode
            }
        )

        binding.recyclerViewSongs.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSongs.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterSongs(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterSongs(newText)
                return false
            }
        })
    }

    private fun filterSongs(query: String?) {
        val filteredSongs = if (query.isNullOrEmpty()) {
            allSongs // Show all songs if query is empty
        } else {
            allSongs.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
            }
        }
        adapter.updateSongs(filteredSongs)
    }

    private fun loadPlaylistSongs() {
        showProgressBar()
        playlistId?.let { id ->
            db.collection("playlists").document(id)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val songs = document["songs"] as? List<Map<String, String>>
                        allSongs = songs?.map { map ->
                            Song(
                                title = map["title"] ?: "Unknown Title",
                                artist = map["artist"] ?: "Unknown Artist"
                            )
                        } ?: emptyList()
                        adapter.updateSongs(allSongs)

                        binding.textEmptyState.text = if (allSongs.isEmpty()) {
                            "No songs added to this playlist yet."
                        } else {
                            ""
                        }
                    } else {
                        Log.e("PlaylistDetailsActivity", "Playlist does not exist.")
                        Toast.makeText(this, "Playlist does not exist.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("PlaylistDetailsActivity", "Failed to load playlist songs: ${e.message}")
                    Toast.makeText(this, "Failed to load playlist songs: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun setupAddSongsButton() {
        binding.buttonAddSongs.setOnClickListener {
            playlistId?.let { id ->
                val intent = Intent(this, SongsActivity::class.java).apply {
                    putExtra("playlistId", id)
                }
                startActivity(intent)
            } ?: run {
                Log.e("PlaylistDetailsActivity", "Playlist ID is not available.")
                Toast.makeText(this, "Playlist ID is not available.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
