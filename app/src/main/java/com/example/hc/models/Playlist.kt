package com.example.hc.models

data class Playlist(
    var playlistId: String = "", // Unique ID for the playlist (Firestore document ID)
    val name: String = "", // Name of the playlist
    val userId: String = "", // User ID associated with the playlist
    val songs: List<Map<String, String>> = listOf() // List of songs (each song is a map with title and artist)
) {
    // Default constructor for Firestore (required for object deserialization)
    constructor() : this("", "", "", listOf())
}



