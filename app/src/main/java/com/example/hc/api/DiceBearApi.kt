package com.example.hc.api

import retrofit2.http.GET
import retrofit2.http.Path

interface DiceBearApi {
    @GET("api/adventurer/{seed}.svg")
    suspend fun getAvatar(@Path("seed") seed: String): retrofit2.Response<String>
}