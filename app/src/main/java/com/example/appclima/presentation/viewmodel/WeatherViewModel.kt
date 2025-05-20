package com.example.appclima.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.appclima.data.local.database.AppDatabase
import com.example.appclima.data.local.model.FavouriteEntity
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val favouriteDao = AppDatabase.getDatabase(application).favouriteDao()

    fun insertFavourite(city: FavouriteEntity) {
        viewModelScope.launch {
            try {
                favouriteDao.insertFavourite(city)
            }catch (e: Exception) {
                Log.e("WeatherViewModel", "Error al guardar: ", e)
            }
        }
    }

    fun removeFavourite(cityId: Int) {
        viewModelScope.launch {
            favouriteDao.deleteFavouriteById(cityId)
        }
    }

    val favourites: LiveData<List<FavouriteEntity>> = favouriteDao.getFavourites()
}