package com.example.hc.network

import com.example.hc.api.ApiService
import com.example.hc.api.DiceBearApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // HarmonyCollective Base URL
    private const val BASE_URL_HC = "https://harmonycollective-d613d.ew.r.appspot.com/"

    // DiceBear Base URL
    private const val BASE_URL_DICEBEAR = "https://avatars.dicebear.com/"

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

    // Retrofit instance for DiceBear API
    private val retrofitDiceBear by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_DICEBEAR)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Create the HarmonyCollective API service instance
    val api: ApiService by lazy {
        retrofitHC.create(ApiService::class.java)
    }

    // Create the DiceBear API service instance
    val diceBearApi: DiceBearApi by lazy {
        retrofitDiceBear.create(DiceBearApi::class.java)
    }

    // Getter for HarmonyCollective Retrofit instance
    fun getHarmonyCollectiveRetrofit(): Retrofit {
        return retrofitHC
    }

    // Getter for DiceBear Retrofit instance
    fun getDiceBearRetrofit(): Retrofit {
        return retrofitDiceBear
    }
}
