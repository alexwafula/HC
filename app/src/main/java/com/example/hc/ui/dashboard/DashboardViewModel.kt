package com.example.hc.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hc.models.Playlist

class DashboardViewModel : ViewModel() {

    // LiveData to hold the list of playlists
    private val _playlists = MutableLiveData<List<Playlist>>().apply {
        value = emptyList()  // Initializing with an empty list
    }
    val playlists: LiveData<List<Playlist>> = _playlists

    // Function to add a new playlist
    fun addPlaylist(name: String) {
        val currentPlaylists = _playlists.value?.toMutableList() ?: mutableListOf()
        currentPlaylists.add(Playlist(name))
        _playlists.value = currentPlaylists  // Update the LiveData
    }

}
