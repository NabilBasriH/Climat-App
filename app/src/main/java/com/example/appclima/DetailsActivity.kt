package com.example.appclima

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appclima.api.OpenWeather
import com.example.appclima.api.RetrofitClient
import com.example.appclima.databinding.ActivityDetailsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0)
            insets
        }

        binding.toolbarDetails.setTitle(R.string.app_name)
        setSupportActionBar(binding.toolbarDetails)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)


        val lat = intent.getDoubleExtra("LAT", 0.0)
        val lon = intent.getDoubleExtra("LON", 0.0)
        val cityName = intent.getStringExtra("NAME") ?: ""

        cityWeather(lat, lon, cityName)
    }

    private fun cityWeather(lat: Double, lon: Double, cityName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.cityWeather
                    .getCityWeather(lat, lon, OpenWeather.API_KEY)

                withContext(Dispatchers.Main) {
                    placeWeather(response, cityName)
                }
            }catch (e: Exception) {
                Log.e("DETAILS_ACTIVITY", "Error al obtener el tiempo: ${e.message}")
            }
        }
    }

    private fun placeWeather(weather: WeatherResponse, cityName: String) {
        binding.tvTemperatura.text = getString(R.string.temp_format, weather.main.temp)
        binding.tvCiudad.text = cityName
        binding.tvPrecipitacion.text = weather.weather[0].description
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