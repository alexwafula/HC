package com.example.hc.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hc.repository.DiceBearRepository
import com.example.hc.utils.DailyMessages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = DiceBearRepository()

    private val _avatar = MutableStateFlow<String?>(null)
    val avatar: StateFlow<String?> get() = _avatar

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> get() = _message

    init {
        loadDailyMessage()
        loadRandomAvatar()
    }

    private fun loadDailyMessage() {
        _message.value = DailyMessages.getMessageForToday()
    }

    fun loadRandomAvatar() {
        viewModelScope.launch {
            val seed = generateRandomSeed()
            val avatarUrl = repository.fetchAvatar(seed)
            _avatar.value = avatarUrl
        }
    }

    private fun generateRandomSeed(): String {
        return (1..100000).random().toString()
    }
}

