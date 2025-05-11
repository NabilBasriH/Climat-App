package com.example.appclima

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.appclima.api.OpenUv
import com.example.appclima.api.OpenWeather
import com.example.appclima.api.RetrofitClient
import com.example.appclima.databinding.ActivityDetailsBinding
import com.example.appclima.model.CityResponse
import com.example.appclima.model.CityUv
import com.example.appclima.model.WeatherResponse
import com.example.appclima.utilities.AppLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.Locale

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var ubicacion: AppLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ubicacion = AppLocation(this)

        binding.toolbarDetails.setTitle(R.string.app_name)
        setSupportActionBar(binding.toolbarDetails)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        ubicacion.getActualLocation { lat, lon ->
            getCityWeather(lat, lon)
        }
    }

    private fun getCityWeather(latitude: Double, longitude: Double) {
        if (ubicacion.isPermissionGrantedOnce()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val responseNameList = RetrofitClient.cityNamesRetrofit
                        .getCityNameReverse(latitude, longitude, apiKey = OpenWeather.API_KEY)
                    val responseName = responseNameList.firstOrNull()!!
                    val responseWeather = RetrofitClient.cityWeather
                        .getCityWeather(latitude, longitude, OpenWeather.API_KEY)
                    try {
                        val responseUv = RetrofitClient.cityUv
                            .getCityUV(apiKey = OpenUv.API_KEY, lat = latitude, lon = longitude)
                    }catch (e: Exception) {
                        Log.e("UV", "Error al uv: ${e.message}")
                    }

//                    withContext(Dispatchers.Main) {
//                        placeWeather(responseName, responseWeather, responseUv)
//                    }
                } catch (e: Exception) {
                    Log.e("DETAILS_ACTIVITY", "Error al obtener el tiempo: ${e.message}")
                }
            }
        }
    }

//    private fun cityWeather(lat: Double, lon: Double, cityName: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = RetrofitClient.cityWeather
//                    .getCityWeather(lat, lon, OpenWeather.API_KEY)
//
//                withContext(Dispatchers.Main) {
//                    placeWeather(response, cityName)
//                }
//            }catch (e: Exception) {
//                Log.e("DETAILS_ACTIVITY", "Error al obtener el tiempo: ${e.message}")
//            }
//        }
//    }

    private fun placeWeather(cityName: CityResponse, cityWeather: WeatherResponse, cityUv: CityUv) {
        binding.tvDTemperatura.text = getString(R.string.temp_format, cityWeather.main.temp.toInt())
        binding.tvDDescripcion.text = cityWeather.weather[0].description.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }
        Glide.with(this)
            .load("https://openweathermap.org/img/wn/${cityWeather.weather[0].icon}@2x.png")
            .into(binding.ivDImagenPrecipitacion)
        binding.tvDCiudad.text = cityName.name
        binding.tvDComunidad.text = cityName.state
        binding.tvDTempMax.text =
            getString(R.string.temp_max_format, cityWeather.main.tempMax.toInt())
        binding.tvDTempMin.text =
            getString(R.string.temp_min_format, cityWeather.main.tempMin.toInt())
        binding.tvSensationTemp.text = getString(R.string.temp_format, cityWeather.main.feelsLike)
        binding.tvCloudTemp.text = cityWeather.clouds.all.toString()
        binding.tvRaindropTemp.text = cityWeather.main.humidity.toString()
        binding.tvRaindropsTemp.text = cityWeather.rain?.mmH.toString()
        binding.tvWindTemp.text = cityWeather.wind.speed.toString()
        binding.tvUvTemp.text = cityUv.uv.toString()
    }
}