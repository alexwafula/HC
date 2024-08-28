package com.example.hc.ui.recs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.hc.databinding.FragmentRecsBinding



class RecsFragment : Fragment() {

    private var _binding: FragmentRecsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val recsViewModel =
            ViewModelProvider(this).get(RecsViewModel::class.java)

        _binding = FragmentRecsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textRecs
        recsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}