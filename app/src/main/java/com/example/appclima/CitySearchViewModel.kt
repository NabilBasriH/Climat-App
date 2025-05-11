package com.example.appclima

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appclima.api.OpenWeather
import com.example.appclima.api.RetrofitClient
import com.example.appclima.model.CityResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CitySearchViewModel: ViewModel() {
    private val _suggestions = MutableLiveData<List<CityResponse>>()
    val suggestions: LiveData<List<CityResponse>> = _suggestions

    private var searchJob: Job? = null

    fun searchCity(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) //debounce
            if (query.length > 2) {
                try {
                    val result = RetrofitClient.cityNamesRetrofit
                        .getCityNames(query, 5, OpenWeather.API_KEY)
                    _suggestions.value = result
                }catch (e: Exception) {
                    Log.e("CitySearchViewModel", "Error buscando ciudad: ${e.message}")
                }
            }else {
                _suggestions.value = emptyList()
            }
        }

    }
}