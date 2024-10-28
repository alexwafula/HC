package com.example.hc.network

import com.example.hc.api.ApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://harmonycollective-d613d.ew.r.appspot.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Set log level to BODY
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging) // Add the logging interceptor
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Create the API service instance
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    fun getRetrofitInstance(): Retrofit {
        return retrofit
    }
}