package com.example.hc.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hc.R
import com.example.hc.models.Song

class SongAdapter(
    private var songs: List<Song>,
    private val currentPlaylistSongs: Set<Song>, // Tracks current playlist songs
    private val mode: Mode, // Determines the adapter mode
    private val onSongAction: ((Song, Boolean) -> Unit)? = null, // Optional listener for add/remove actions
    private val onAddSongClick: ((Song, Boolean) -> Unit)? = null // Optional listener for add/remove actions in ADD mode
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    enum class Mode { VIEW, ADD } // Modes for the adapter: VIEW or ADD

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        val isInPlaylist = currentPlaylistSongs.contains(song) // Check if the song is already in the playlist
        holder.bind(song, mode, isInPlaylist, onSongAction, onAddSongClick)
    }

    override fun getItemCount(): Int = songs.size

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songTitle: TextView = itemView.findViewById(R.id.songTitle)
        private val songArtist: TextView = itemView.findViewById(R.id.songArtist)
        private val addSongButton: Button = itemView.findViewById(R.id.addSongButton)
        private val removeSongButton: Button = itemView.findViewById(R.id.removeSongButton)

        fun bind(
            song: Song,
            mode: Mode,
            isInPlaylist: Boolean,
            onSongAction: ((Song, Boolean) -> Unit)?,
            onAddSongClick: ((Song, Boolean) -> Unit)?
        ) {
            songTitle.text = song.title
            songArtist.text = song.artist

            if (mode == Mode.VIEW) {
                // In VIEW mode, hide both Add and Remove buttons
                addSongButton.visibility = View.GONE
                removeSongButton.visibility = View.GONE
            } else if (mode == Mode.ADD) {
                // In ADD mode, show only the Add button and handle logic for adding songs
                addSongButton.visibility = View.VISIBLE
                removeSongButton.visibility = View.GONE // Remove button is always hidden in ADD mode

                var isChecked = isInPlaylist // Use the initial state based on the playlist
                addSongButton.text = if (isChecked) "Added" else "+"

                addSongButton.setOnClickListener {
                    isChecked = !isChecked
                    onAddSongClick?.invoke(song, isChecked)

                    // Update button text based on selection state
                    addSongButton.text = if (isChecked) "Added" else "+"
                }
            }
        }
    }

    // Update the list of songs dynamically
    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}





