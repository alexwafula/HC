package com.example.hc.ui.recs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecsViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is recommendations Fragment"
    }
    val text: LiveData<String> = _text
}