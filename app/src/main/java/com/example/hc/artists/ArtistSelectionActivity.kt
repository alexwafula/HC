package com.example.hc.artists

import android.util.Log
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
import com.example.hc.models.Artist

class ArtistSelectionActivity : AppCompatActivity() {

    private lateinit var artistRecyclerView: RecyclerView
    private lateinit var artistAdapter: ArtistAdapter
    private var artistList: MutableList<Artist> = mutableListOf()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

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

        // Set the adapter
        artistAdapter = ArtistAdapter(artistList) { artist ->
            // Handle the artist selection logic here
            addArtistToUserFavorites(artist)
        }
        artistRecyclerView.adapter = artistAdapter

        // Initialize the ProgressBar
        progressBar = findViewById(R.id.progressBar)

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
    }

    // Fetch artist list from Firestore
    private fun fetchArtistsFromFirestore() {
        // Show the ProgressBar when starting the fetch
        progressBar.visibility = View.VISIBLE

        firestore.collection("artists").get()
            .addOnCompleteListener { task ->
                // Hide the ProgressBar when the fetch is complete
                progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    artistList.clear()
                    for (document in task.result) {
                        val artist = document.toObject(Artist::class.java)
                        artistList.add(artist)
                    }
                    artistAdapter.notifyDataSetChanged() // Notify adapter of data change
                }
            }
    }

    // Filter artist list based on search query
    private fun filterArtists(query: String?) {
        val filteredList = artistList.filter { artist ->
            artist.name.toLowerCase().contains(query?.toLowerCase() ?: "")
        }
        artistAdapter = ArtistAdapter(filteredList.toMutableList()) { artist ->
            addArtistToUserFavorites(artist)
        }
        artistRecyclerView.adapter = artistAdapter
    }

    // Add selected artist to user's favorites in Firestore
    private fun addArtistToUserFavorites(artist: Artist) {
        val userId = auth.currentUser?.uid // Get current user ID
        if (userId != null) {
            val userFavoritesRef = firestore.collection("users").document(userId)

            // First, check if 'favorite_artists' field exists
            userFavoritesRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Log that the document exists
                        Log.d("ArtistSelectionActivity", "User document exists. Adding artist to favorites.")

                        // Field exists, so update it
                        userFavoritesRef.update("favorite_artists", FieldValue.arrayUnion(artist))
                            .addOnSuccessListener {
                                // Artist added successfully
                                Log.d("ArtistSelectionActivity", "Artist added to user's favorites successfully.")
                                Toast.makeText(this, "${artist.name} added to favorites!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                // Handle error when adding artist
                                Log.e("ArtistSelectionActivity", "Error adding artist to favorites", e)
                                Toast.makeText(this, "Failed to add artist. Try again.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // If document doesn't exist, create the field and add the artist
                        Log.d("ArtistSelectionActivity", "User document does not exist. Creating document and adding artist.")
                        val favoriteArtists = hashMapOf(
                            "favorite_artists" to listOf(artist)
                        )
                        userFavoritesRef.set(favoriteArtists)
                            .addOnSuccessListener {
                                // Artist added successfully in a new field
                                Log.d("ArtistSelectionActivity", "New document created and artist added to favorites successfully.")
                                Toast.makeText(this, "${artist.name} added to favorites!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                // Handle error when creating document
                                Log.e("ArtistSelectionActivity", "Error creating user document", e)
                                Toast.makeText(this, "Failed to add artist. Try again.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle error when fetching user document
                    Log.e("ArtistSelectionActivity", "Error fetching user document", e)
                    Toast.makeText(this, "Failed to retrieve user data. Try again.", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Handle the case where the user is not logged in
            Log.e("ArtistSelectionActivity", "User not logged in.")
            Toast.makeText(this, "Please log in to add favorites.", Toast.LENGTH_SHORT).show()
        }
    }
}


