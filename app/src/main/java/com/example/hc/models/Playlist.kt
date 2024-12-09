package com.example.hc.models

data class Playlist(
    val name: String,
    val userId: String // Add user ID to associate with a user
) {
    constructor() : this("", "") // Default constructor for Firestore
}

