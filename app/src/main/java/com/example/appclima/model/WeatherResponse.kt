package com.example.appclima.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val name: String,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val rain: Rain?,
    val clouds: Clouds
)

data class Weather(val description: String, val icon: String)
data class Main(
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    val humidity: Int
)

data class Wind(
    val speed: Double
)
data class Rain(
    @SerializedName("1h") val mmH: Double
)
data class Clouds(
    val all: Int
)