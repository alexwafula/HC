package com.example.hc.api

import com.example.hc.models.RecommendationsResponse
import com.example.hc.models.UserProfile
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {
    @POST("/user")
    fun createUserProfile(@Body userProfile: UserProfile): Call<ResponseBody>

    @GET("/recommendations/{genre}")
    fun getRecommendations(@Path("genre") genre: String): Call<RecommendationsResponse>
}