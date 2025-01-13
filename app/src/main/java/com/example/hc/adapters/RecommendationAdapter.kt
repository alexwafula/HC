package com.example.hc.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hc.R
import com.example.hc.models.SongRecommendation
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore

class RecommendationAdapter(
    private var recommendationList: List<SongRecommendation>,
    private val onRecommendationClickListener: (SongRecommendation) -> Unit // Listener for item clicks
) : RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val recommendation = recommendationList[position]
        holder.songNameTextView.text = recommendation.title
        holder.artistTextView.text = recommendation.artist

        // Handle item click
        holder.itemView.setOnClickListener {
            // Get the song's preview URL from Firestore
            firestore.collection("users")
                .whereEqualTo("recommended_songs.title", recommendation.title)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.documents.isNotEmpty()) {
                        val document = querySnapshot.documents.first()
                        val previewUrl = document.getString("preview_url") // Assuming the field in Firestore

                        // If we have a valid preview URL, open it in Spotify
                        if (!previewUrl.isNullOrEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(previewUrl))
                            intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://com.spotify.android.music"))
                            holder.itemView.context.startActivity(intent)
                        }
                    }
                }
        }
    }

    override fun getItemCount(): Int {
        return recommendationList.size
    }

    inner class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songNameTextView: TextView = itemView.findViewById(R.id.songNameTextView)
        val artistTextView: TextView = itemView.findViewById(R.id.artistTextView)
    }

    // Method to update the recommendation list
    fun updateRecommendationList(newRecommendationList: List<SongRecommendation>) {
        recommendationList = newRecommendationList
        notifyDataSetChanged() // Notify the adapter of the changes
    }
}
