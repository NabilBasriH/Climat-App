package com.example.appclima.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // OkHttpClient creation with the interceptor
    private val client = OkHttpClient.Builder()
        .addInterceptor(HeaderInterceptor())
        .build()

    // Retrofit configuration
    private val retrofitOpenWeather = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // ApiService instance
    private val apiServiceOpenWeather = retrofitOpenWeather.create(ApiService::class.java)

    // Functions to access endpoints
    val cityNamesRetrofit: ApiService = apiServiceOpenWeather
    val cityWeather: ApiService = apiServiceOpenWeather
    val nearbyCitiesWeather: ApiService = apiServiceOpenWeather

    private val retrofitOpenUV = Retrofit.Builder()
        .baseUrl("https://api.openuv.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private val apiServiceOpenUV = retrofitOpenUV.create(OpenUVService::class.java)

    val cityUv: OpenUVService = apiServiceOpenUV
}