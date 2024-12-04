package com.example.hc.repository

import android.util.Log
import com.example.hc.network.RetrofitInstance

class DiceBearRepository {

    suspend fun fetchAvatar(seed: String): String? {
        return try {
            val response = RetrofitInstance.diceBearApi.getAvatar(seed)
            if (response.isSuccessful) {
                val svgUrl = response.raw().request.url.toString()
                svgUrl
            } else {
                Log.e("DiceBearRepository", "Failed to fetch avatar: ${response.errorBody()}")
                null
            }
        } catch (e: Exception) {
            Log.e("DiceBearRepository", "Exception: ${e.message}")
            null
        }
    }
}
