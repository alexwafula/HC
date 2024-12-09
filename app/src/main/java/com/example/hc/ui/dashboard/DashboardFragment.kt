package com.example.hc.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hc.R
import com.example.hc.adapters.PlaylistAdapter
import com.example.hc.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PlaylistAdapter
    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        dashboardViewModel.autoLoadPlaylists(requireContext()) // Auto-load user playlists
        setupRecyclerView()
        setupFab()
        observePlaylists()

        return root
    }

    private fun setupRecyclerView() {
        adapter = PlaylistAdapter(emptyList()) // Initially empty list
        binding.recyclerViewPlaylists.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewPlaylists.adapter = adapter
    }

    private fun setupFab() {
        binding.fabCreatePlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    private fun showCreatePlaylistDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_playlist, null)
        dialogBuilder.setView(dialogView)

        val playlistNameInput = dialogView.findViewById<EditText>(R.id.playlistNameInput)
        dialogBuilder.setTitle("Create Playlist")
            .setPositiveButton("Create") { dialog, _ ->
                val playlistName = playlistNameInput.text.toString().trim()
                if (playlistName.isNotEmpty()) {
                    dashboardViewModel.addPlaylist(requireContext(), playlistName)
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Please enter a playlist name.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        dialogBuilder.create().show()
    }

    private fun observePlaylists() {
        dashboardViewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            adapter = PlaylistAdapter(playlists)
            binding.recyclerViewPlaylists.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

