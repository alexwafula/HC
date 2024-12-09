package com.example.hc.ui.dashboard

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hc.models.Playlist
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // LiveData to hold the list of playlists
    private val _playlists = MutableLiveData<List<Playlist>>().apply {
        value = emptyList() // Initializing with an empty list
    }
    val playlists: LiveData<List<Playlist>> = _playlists

    // Function to add a new playlist
    fun addPlaylist(context: Context, name: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val playlist = Playlist(name = name, userId = userId)

        // Add the playlist to the Firestore collection
        db.collection("playlists")
            .add(playlist)
            .addOnSuccessListener {
                // Update the local LiveData
                val currentPlaylists = _playlists.value?.toMutableList() ?: mutableListOf()
                currentPlaylists.add(playlist)
                _playlists.value = currentPlaylists

                // Toast for success
                Toast.makeText(context, "Playlist '$name' created successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Toast for failure
                Toast.makeText(context, "Failed to create playlist: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to fetch playlists for the current user and load them
    fun fetchUserPlaylists(context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("playlists")
            .whereEqualTo("userId", userId) // Query playlists for the logged-in user
            .get()
            .addOnSuccessListener { documents ->
                val userPlaylists = mutableListOf<Playlist>()
                for (document in documents) {
                    val playlist = document.toObject(Playlist::class.java)
                    userPlaylists.add(playlist)
                }

                // Update the LiveData with the fetched playlists
                _playlists.value = userPlaylists

                Toast.makeText(context, "Playlists loaded successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load playlists: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to auto-load playlists on login
    fun autoLoadPlaylists(context: Context) {
        fetchUserPlaylists(context)
    }
}



