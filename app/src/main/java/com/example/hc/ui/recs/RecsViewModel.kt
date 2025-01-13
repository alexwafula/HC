package com.example.hc.ui.recs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hc.models.SongRecommendation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class RecsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _recommendations = MutableLiveData<List<SongRecommendation>>()
    val recommendations: LiveData<List<SongRecommendation>> = _recommendations

    private val _filteredRecommendations = MutableLiveData<List<SongRecommendation>>()
    val filteredRecommendations: LiveData<List<SongRecommendation>> = _filteredRecommendations

    private val _isLoading = MutableLiveData<Boolean>(false) // To track loading state
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        fetchAllRecommendations()
    }

    // Fetch all recommendations from Firestore
    private fun fetchAllRecommendations() {
        _isLoading.value = true // Show progress bar when fetching data

        viewModelScope.launch {
            firestore.collection("users")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val allRecommendations = querySnapshot.documents.flatMap { document ->
                        val recommendations = document.get("recommended_songs") as? List<Map<String, String>>
                        recommendations?.map {
                            SongRecommendation(
                                title = it["title"] ?: "",
                                artist = it["artist"] ?: ""
                            )
                        } ?: emptyList()
                    }

                    _recommendations.value = allRecommendations
                    _filteredRecommendations.value = allRecommendations // Initially, show all recommendations
                    _isLoading.value = false // Hide progress bar when data is fetched
                }
                .addOnFailureListener {
                    _isLoading.value = false // Hide progress bar if an error occurs
                    // Optionally, handle error (e.g., show a message to the user)
                }
        }
    }

    // Filter the recommendations based on query
    fun filterRecommendations(query: String) {
        val filteredList = _recommendations.value?.filter { song ->
            song.title.contains(query, ignoreCase = true) || song.artist.contains(query, ignoreCase = true)
        } ?: emptyList()

        _filteredRecommendations.value = filteredList
    }
}
