package com.example.appclima

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.appclima.api.OpenWeather
import com.example.appclima.api.RetrofitClient
import com.example.appclima.databinding.ActivityMainBinding
import com.example.appclima.recycleview.CityAdapter
import com.example.appclima.recycleview.CityInfoAdapter
import com.example.appclima.utilities.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var ubicacion: Location
    private lateinit var viewModel: CitySearchViewModel
    private lateinit var adapterCity: CityAdapter
    private lateinit var adapterCities: CityInfoAdapter
    private var lastTextSearch: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ubicacion = Location(this)
        ubicacion.enableLocation()

        binding.toolbar.setTitle(R.string.app_name)
        setSupportActionBar(binding.toolbar)


        getMainCity()
        initRecyclerViews()
        getCities()

        viewModel = ViewModelProvider(this)[CitySearchViewModel::class.java]
        viewModel.suggestions.observe(this) { cities ->
            adapterCity.updateData(cities)
            binding.rvSuggestions.visibility = if (cities.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun getMainCity() {
        if (ubicacion.isPermissionGrantedOnce()) {
            ubicacion.getActualLocation { latitude, longitude ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val responseName = RetrofitClient.cityNamesRetrofit
                            .getCityNameReverse(latitude, longitude, apiKey = OpenWeather.API_KEY)
                        val responseWeather = RetrofitClient.cityWeather
                            .getCityWeather(latitude, longitude, OpenWeather.API_KEY)

                        withContext(Dispatchers.Main) {
                            placeWeather(responseName, responseWeather)
                        }
                    }catch (e: Exception) {
                        Log.e("MAIN_ACTIVITY", "Error al obtener el tiempo: ${e.message}")
                    }
                }
            }
        }
    }

    private fun placeWeather(cityName: CityResponse, cityWeather: WeatherResponse) {
        binding.tvTemperatura.text = getString(R.string.temp_format, cityWeather.main.temp)
        binding.tvDescripcion.text = cityWeather.weather[0].description
        Glide.with(this)
            .load("https://openweathermap.org/img/wn/${cityWeather.weather[0].icon}@2x.png")
            .into(binding.ivImagenPrecipitacion)
        binding.tvCiudad.text = cityName.name
        binding.tvComunidad.text = cityName.state
        binding.tvTempMax.text = getString(R.string.temp_format, cityWeather.main.tempMax)
        binding.tvTempMin.text = getString(R.string.temp_format, cityWeather.main.tempMin)
    }

    private fun getCities() {
        ubicacion.getActualLocation { latitude, longitude ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.nearbyCitiesWeather.getNearbyCitiesWeather(latitude, longitude, apiKey = OpenWeather.API_KEY)
                    val cities = response.list
                    Log.d("CIUDADES", "Ciudades encontradas: $cities")

                    withContext(Dispatchers.Main) {
                        adapterCities.updateData(cities)
                    }
                } catch (e: Exception) {
                    Log.e("CITY_WEATHER_RECYCLER", "Error: ${e.message}")
                }
            }
        }
    }

    private fun initRecyclerViews() {
        adapterCities = CityInfoAdapter(emptyList()) { weatherCity -> onItemSelected(weatherCity) }
        binding.rvCities.layoutManager = LinearLayoutManager(this)
        binding.rvCities.adapter = adapterCities

        adapterCity = CityAdapter(emptyList()) { city -> onItemSelected(city) }
        binding.rvSuggestions.layoutManager = LinearLayoutManager(this)
        binding.rvSuggestions.adapter = adapterCity
    }

    private fun onItemSelected(city: CityResponse) {
        val intent = Intent(this, DetailsActivity::class.java)
//        intent.putExtra("LAT", city.lat)
//        intent.putExtra("LON", city.lon)
//        intent.putExtra("NAME", city.name)
        startActivity(intent)
        Toast.makeText(this, "Hasta aquí bien", Toast.LENGTH_SHORT).show()
    }

    private fun onItemSelected(weatherCity: WeatherResponse) {
        val intent = Intent(this, DetailsActivity::class.java)
//        intent.putExtra("LAT", city.lat)
//        intent.putExtra("LON", city.lon)
//        intent.putExtra("NAME", city.name)
        startActivity(intent)
        Toast.makeText(this, "Hasta aquí bien", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        val searchMenuItem = menu?.findItem(R.id.item_buscar)
        val searchView = searchMenuItem?.actionView as SearchView
        searchView.queryHint = "Albatera"

        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                searchView.post {
                    searchView.isIconified = false
                    searchView.clearFocus()
                    searchView.requestFocusFromTouch()

                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
                }
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Asegurarse de que la flecha no vuelve a aparecer
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.setHomeButtonEnabled(false)
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                lastTextSearch = newText ?: ""
                viewModel.searchCity(lastTextSearch)
                return true
            }
        })

        searchView.setOnCloseListener {
            searchView.setQuery("", false)
            true
        }

        binding.main.setOnTouchListener { _, _ ->
            if (!searchView.isIconified) {
                searchView.setQuery("", false)
                searchMenuItem.collapseActionView()
            }
            false
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ubicacion.handlePermissionResult(requestCode, grantResults)
    }

    override fun onResume() {
        super.onResume()
        ubicacion.isPermissionGrantedOnce()
    }
}