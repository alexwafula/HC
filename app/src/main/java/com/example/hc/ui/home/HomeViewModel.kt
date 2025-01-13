package com.example.hc.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hc.models.SongRecommendation
import com.example.hc.models.UserRecommendations
import com.example.hc.utils.DailyMessages
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {


    private val firestore = FirebaseFirestore.getInstance()

    private val _avatar = MutableStateFlow<String?>(null)
    val avatar: StateFlow<String?> get() = _avatar

    private val _message = MutableStateFlow("")

    val message: StateFlow<String> get() = _message

    private val _userRecommendations = MutableStateFlow<List<UserRecommendations>>(emptyList())
    val userRecommendations: StateFlow<List<UserRecommendations>> get() = _userRecommendations

    init {
        loadDailyMessage()
        loadAllUserRecommendations()
    }

    private fun loadDailyMessage() {
        _message.value = DailyMessages.getMessageForToday()
    }

    private fun loadAllUserRecommendations() {
        viewModelScope.launch {
            firestore.collection("users")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val allRecommendations = querySnapshot.documents.mapNotNull { document ->
                        val userId = document.id
                        val recommendations = document.get("recommended_songs") as? List<Map<String, String>>
                        if (recommendations != null) {
                            // Map the recommendations to SongRecommendation objects
                            val songList = recommendations.map {
                                SongRecommendation(
                                    title = it["title"] ?: "",
                                    artist = it["artist"] ?: "",
                                    preview_url = it["preview_url"] ?: ""  // Ensure this field is populated
                                )
                            }

                            // Limit the recommendations to the first 5
                            val top5Recommendations = songList.take(5)

                            // Create and return a UserRecommendations object
                            UserRecommendations(userId = userId, recommendations = top5Recommendations)
                        } else null
                    }

                    // Update the state with the filtered recommendations
                    _userRecommendations.value = allRecommendations
                }
                .addOnFailureListener { exception ->
                    // Log the error for debugging purposes
                    Log.e("HomeViewModel", "Failed to load user recommendations", exception)

                    // Notify the UI layer or user about the error (optional)
                    _userRecommendations.value = emptyList() // Clear recommendations in case of failure
                }
        }
    }

}


