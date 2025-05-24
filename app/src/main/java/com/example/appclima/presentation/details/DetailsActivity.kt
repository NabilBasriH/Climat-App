package com.example.appclima.presentation.details

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.appclima.R
import com.example.appclima.data.remote.api.openuv.OpenUv
import com.example.appclima.data.remote.api.openweathermap.OpenWeather
import com.example.appclima.data.remote.RetrofitClient
import com.example.appclima.databinding.ActivityDetailsBinding
import com.example.appclima.domain.model.CityResponse
import com.example.appclima.domain.model.CityUv
import com.example.appclima.domain.model.WeatherResponse
import com.example.appclima.core.utils.AppLocation
import com.example.appclima.core.utils.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        val cityName = intent.getStringExtra("NAME") ?: ""
        val lat = intent.getDoubleExtra("LATITUDE", 0.0)
        val lon = intent.getDoubleExtra("LONGITUDE", 0.0)

        setSupportActionBar(binding.toolbarDetails)
        supportActionBar?.title = cityName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getCityWeather(lat, lon)
    }

    private fun getCityWeather(latitude: Double, longitude: Double) {
        if (!Network.checkNetwork(this)) return
        if (ubicacion.isPermissionGrantedOnce()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val responseNameList = RetrofitClient.cityNamesRetrofit
                        .getCityNameReverse(latitude, longitude, apiKey = OpenWeather.API_KEY)
                    val responseName = responseNameList.firstOrNull()!!

                    val responseWeather = RetrofitClient.cityWeather
                        .getCityWeather(latitude, longitude, OpenWeather.API_KEY)
                    Log.d("WEATHER_JSON", "WEATHER Result: $responseWeather")

                    val responseUv = RetrofitClient.cityUv
                        .getCityUV(apiKey = OpenUv.API_KEY_UV, lat = latitude, lng = longitude)

                    withContext(Dispatchers.Main) {
                        placeWeather(responseName, responseWeather, responseUv)
                        binding.progressView.progressContainer.visibility = View.GONE
                        binding.main.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    Log.e("DETAILS_ACTIVITY", "Error al obtener el tiempo: ${e.message}")
                }
            }
        }
    }

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
        binding.tvDComunidad.text = cityName.state ?: "N/A"
        binding.tvDTempMax.text =
            getString(R.string.temp_max_format, cityWeather.main.tempMax.toInt())
        binding.tvDTempMin.text =
            getString(R.string.temp_min_format, cityWeather.main.tempMin.toInt())
        binding.tvSensationTemp.text =
            getString(R.string.temp_format, cityWeather.main.feelsLike.toInt())
        binding.tvCloudTemp.text = getString(R.string.temp_percent_format, cityWeather.clouds.all)
        binding.tvRaindropTemp.text = getString(R.string.temp_percent_format, cityWeather.main.humidity)
        binding.tvRaindropsTemp.text = cityWeather.rain?.mmH?.let {
            getString(R.string.temp_rain_format, it)
        } ?: "N/A"
        val windSpeed = cityWeather.wind.speed * 3.6
        binding.tvWindTemp.text = getString(R.string.temp_speed_format, windSpeed.toInt())
        binding.tvUvTemp.text = getString(R.string.temp_uv_format, cityUv.result.uv)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}