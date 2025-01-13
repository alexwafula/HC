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
    private val currentPlaylistSongs: Set<Song>,
    private val mode: Mode,
    private val onSongAction: ((Song, Action) -> Unit)? = null
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    enum class Mode { VIEW, ADD }
    enum class Action { ADD, REMOVE }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        val isInPlaylist = currentPlaylistSongs.contains(song)
        holder.bind(song, mode, isInPlaylist, onSongAction)
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
            onSongAction: ((Song, Action) -> Unit)?
        ) {
            songTitle.text = song.title
            songArtist.text = song.artist

            when (mode) {
                Mode.VIEW -> {
                    addSongButton.visibility = View.GONE
                    removeSongButton.visibility = View.GONE
                }
                Mode.ADD -> {
                    addSongButton.visibility = if (!isInPlaylist) View.VISIBLE else View.GONE
                    removeSongButton.visibility = if (isInPlaylist) View.VISIBLE else View.GONE

                    addSongButton.setOnClickListener {
                        onSongAction?.invoke(song, Action.ADD)
                    }

                    removeSongButton.setOnClickListener {
                        onSongAction?.invoke(song, Action.REMOVE)
                    }
                }
            }
        }
    }

    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}
