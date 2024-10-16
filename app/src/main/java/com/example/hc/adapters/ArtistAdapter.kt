package com.example.hc.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hc.R
import com.example.hc.models.Artist

class ArtistAdapter(
    private var artistList: List<Artist>,
    private val onArtistAddListener: (Artist) -> Unit // Change listener type to (Artist) -> Unit
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_artists, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artistList[position]
        holder.artistName.text = artist.name // Using Kotlin's direct property access

        // Handle the "Add" button click
        holder.addArtistButton.setOnClickListener {
            onArtistAddListener(artist) // Call the listener directly with the artist
        }
    }

    override fun getItemCount(): Int {
        return artistList.size
    }

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artistName: TextView = itemView.findViewById(R.id.artistName)
        val addArtistButton: Button = itemView.findViewById(R.id.addArtistButton)
    }

    // Method to update the artist list (used when filtering)
    fun updateArtistList(newArtistList: List<Artist>) {
        artistList = newArtistList
        notifyDataSetChanged() // Notify the adapter of the changes
    }
}

