package com.example.appclima.data.remote.api.openuv

import com.example.appclima.domain.model.CityUv
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface OpenUVService {
    @GET("api/v1/uv")
    suspend fun getCityUV(
        @Header("x-access-token") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): CityUv
}