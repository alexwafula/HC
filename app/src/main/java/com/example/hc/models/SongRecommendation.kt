package com.example.hc.models

data class SongRecommendation(
    val title: String = "",
    val artist: String = "",
    val preview_url: String = "" // Add this field to store the Spotify link
)

