package com.example.appclima.api

import com.example.appclima.model.CityUv
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface OpenUVService {
    @GET("api/v1/uv")
    suspend fun getCityUV(
        @Header("x-access-token") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): CityUv
}