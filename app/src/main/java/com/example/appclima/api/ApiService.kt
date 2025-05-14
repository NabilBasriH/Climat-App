package com.example.appclima.api

import com.example.appclima.model.CityResponse
import com.example.appclima.model.CityUv
import com.example.appclima.model.NearbyCityResponse
import com.example.appclima.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {

    @GET("data/2.5/weather")
    suspend fun getCityWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "es"
    ): WeatherResponse

    @GET("geo/1.0/direct")
    suspend fun getCityNames(
        @Query("q") cityName: String,
        @Query("limit") limit: Int,
        @Query("appid") apiKey: String
    ): List<CityResponse>

    @GET("geo/1.0/reverse")
    suspend fun getCityNameReverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): List<CityResponse>

    @GET("data/2.5/find")
    suspend fun getNearbyCitiesWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("cnt") count: Int = 10,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "es"
    ): NearbyCityResponse
}