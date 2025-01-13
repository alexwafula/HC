package com.example.hc.network

import com.example.hc.api.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // HarmonyCollective Base URL
    private const val BASE_URL_HC = "https://harmonycollective-d613d.ew.r.appspot.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Set log level to BODY
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging) // Add the logging interceptor
        .build()

    // Retrofit instance for HarmonyCollective API
    private val retrofitHC by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_HC)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Create the HarmonyCollective API service instance
    val api: ApiService by lazy {
        retrofitHC.create(ApiService::class.java)
    }
    // Getter for HarmonyCollective Retrofit instance
    fun getHarmonyCollectiveRetrofit(): Retrofit {
        return retrofitHC
    }

}
