package com.example.appclima.data.local.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appclima.data.local.model.FavouriteEntity

@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(favourite: FavouriteEntity)

    @Query("SELECT * FROM favourites")
    fun getFavourites(): LiveData<List<FavouriteEntity>>

    @Query("DELETE FROM favourites WHERE id = :cityId")
    suspend fun deleteFavouriteById(cityId: Int)
}