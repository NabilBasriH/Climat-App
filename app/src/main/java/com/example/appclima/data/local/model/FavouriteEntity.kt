package com.example.appclima.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val lat: Double,
    val lon: Double,
    val temp: Double,
    val icon: String
)
