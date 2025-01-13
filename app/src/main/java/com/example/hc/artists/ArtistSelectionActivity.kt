package com.example.hc.artists

import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.Toast
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.example.hc.R
import com.example.hc.adapters.ArtistAdapter
import com.example.hc.auth.LogininActivity
import com.example.hc.models.Artist

class ArtistSelectionActivity : AppCompatActivity() {

    private lateinit var artistRecyclerView: RecyclerView
    private lateinit var artistAdapter: ArtistAdapter
    private var artistList: MutableList<Artist> = mutableListOf()
    private var selectedArtists: MutableList<Artist> = mutableListOf() // Track selected artists
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var nextButton: Button // "Next" button reference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist_selection)

        // Initialize Firebase services
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize RecyclerView and Adapter
        artistRecyclerView = findViewById(R.id.artistRecyclerView)
        artistRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the ProgressBar and "Next" Button
        progressBar = findViewById(R.id.progressBar)
        nextButton = findViewById(R.id.nextButton) // Button should be in the XML layout
        nextButton.visibility = View.GONE // Hide initially

        // Set the adapter
        artistAdapter = ArtistAdapter(artistList) { artist ->
            toggleArtistSelection(artist) // New method to handle selection
        }
        artistRecyclerView.adapter = artistAdapter

        // Fetch artists from Firestore
        fetchArtistsFromFirestore()

        // Search functionality
        val searchView: SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterArtists(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterArtists(newText)
                return false
            }
        })

        // Set click listener for "Next" button to navigate
        nextButton.setOnClickListener {
            if (selectedArtists.size >= 3) {
                sendSelectionsToDb() // Send data to the database
                navigateToLogininActivity() // Function to navigate
            }
        }
    }

    // Toggle artist selection and update the UI accordingly
    private fun toggleArtistSelection(artist: Artist) {
        if (selectedArtists.contains(artist)) {
            selectedArtists.remove(artist)
        } else {
            selectedArtists.add(artist)
        }

        // Show or hide the "Next" button based on the selection count
        nextButton.visibility = if (selectedArtists.size >= 3) View.VISIBLE else View.GONE
    }

    // Send selected artists to Firestore only if 3 or more are chosen
    private fun sendSelectionsToDb() {
        val userId = auth.currentUser?.uid
        if (userId != null && selectedArtists.size >= 3) {
            val userFavoritesRef = firestore.collection("users").document(userId)
            userFavoritesRef.update("favorite_artists", FieldValue.arrayUnion(*selectedArtists.toTypedArray()))
                .addOnSuccessListener {
                    Toast.makeText(this, "Favorites updated!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("ArtistSelectionActivity", "Error updating favorites", e)
                    Toast.makeText(this, "Failed to update favorites. Try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Fetch artist list from Firestore
    private fun fetchArtistsFromFirestore() {
        progressBar.visibility = View.VISIBLE
        firestore.collection("artists").get()
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    artistList.clear()
                    for (document in task.result) {
                        val artist = document.toObject(Artist::class.java)
                        artistList.add(artist)
                    }
                    artistAdapter.notifyDataSetChanged()
                }
            }
    }

    // Filter artist list based on search query
    private fun filterArtists(query: String?) {
        val filteredList = artistList.filter { artist ->
            artist.name.toLowerCase().contains(query?.toLowerCase() ?: "")
        }
        artistAdapter = ArtistAdapter(filteredList.toMutableList()) { artist ->
            toggleArtistSelection(artist)
        }
        artistRecyclerView.adapter = artistAdapter
    }

    // Navigate to the Main Activity
    private fun navigateToLogininActivity() {
        // Intent logic to navigate to LogininActivity
        val intent = Intent(this, LogininActivity::class.java)
        startActivity(intent)
        finish()
    }
}



