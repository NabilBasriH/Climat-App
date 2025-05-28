package com.example.appclima.data.remote

import com.example.appclima.data.remote.api.openuv.OpenUVService
import com.example.appclima.data.remote.api.openweathermap.ApiService
import com.example.appclima.data.remote.api.openweathermap.HeaderInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val client = OkHttpClient.Builder()
        .addInterceptor(HeaderInterceptor())
        .build()

    private val retrofitOpenWeather = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiServiceOpenWeather = retrofitOpenWeather.create(ApiService::class.java)

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