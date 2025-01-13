package com.example.hc.models

data class UserRecommendations(
    val userId: String,
    val recommendations: List<SongRecommendation>
)
