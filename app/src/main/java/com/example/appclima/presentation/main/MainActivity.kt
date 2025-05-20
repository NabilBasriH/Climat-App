package com.example.appclima.presentation.main

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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.appclima.presentation.viewmodel.CitySearchViewModel
import com.example.appclima.R
import com.example.appclima.presentation.viewmodel.WeatherViewModel
import com.example.appclima.data.remote.api.openweathermap.OpenWeather
import com.example.appclima.data.remote.RetrofitClient
import com.example.appclima.databinding.ActivityMainBinding
import com.example.appclima.domain.model.CityResponse
import com.example.appclima.data.local.model.FavouriteEntity
import com.example.appclima.domain.model.WeatherResponse
import com.example.appclima.presentation.adapter.SearchCityAdapter
import com.example.appclima.presentation.adapter.CityListAdapter
import com.example.appclima.core.utils.AppLocation
import com.example.appclima.core.utils.Geo
import com.example.appclima.core.utils.Network
import com.example.appclima.presentation.details.DetailsActivity
import com.example.appclima.presentation.favourites.FavouritesActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var ubicacion: AppLocation
    private lateinit var viewModel: CitySearchViewModel
    private lateinit var adapterSearchCity: SearchCityAdapter
    private lateinit var adapterCityList: CityListAdapter
    private lateinit var weatherViewModel: WeatherViewModel

    private var lastTextSearch: String = ""
    private var cityName: String = ""
    private var currentLat: Double = 0.0
    private var currentLon: Double = 0.0
    private val favouriteCityIds = mutableSetOf<Int>()
    private var isInBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.swipeRefresh.setOnRefreshListener {
            if (checkNetwork()) {
                fetchLocationAndData()
            }
            binding.swipeRefresh.isRefreshing = false
        }

        ubicacion = AppLocation(this)
        ubicacion.onPermissionGranted = {
            runOnUiThread {
                fetchLocationAndData()
            }
        }

        if (ubicacion.isPermissionGrantedOnce()) {
            fetchLocationAndData()
        } else {
            ubicacion.enableLocation()
        }

        setupAdapters()

        binding.toolbar.setTitle(R.string.app_name)
        setSupportActionBar(binding.toolbar)

        if (!checkNetwork()) return

        viewModel = ViewModelProvider(this)[CitySearchViewModel::class.java]
        viewModel.suggestions.observe(this) { cities ->
            adapterSearchCity.updateData(cities)
            binding.rvSuggestions.visibility = if (cities.isNotEmpty()) View.VISIBLE else View.GONE
        }
        weatherViewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
        weatherViewModel.favourites.observe(this) { favourites ->
            favouriteCityIds.clear()
            favouriteCityIds.addAll(favourites.map { it.id })
            adapterSearchCity.notifyDataSetChanged()
        }

        binding.mcvPrincipal.setOnClickListener {
            onItemSelected(cityName, currentLat, currentLon)
        }
    }

    private fun fetchLocationAndData() {
        if (!ubicacion.isGpsEnabled()) {
            ubicacion.showGpsDisabledDialog()
            return
        }
        ubicacion.getActualLocation { lat, lon ->
            currentLat = lat
            currentLon = lon
            getMainCity(lat, lon)
            getCities(lat, lon)
        }
    }

    private fun setupAdapters() {
        adapterCityList = CityListAdapter(emptyList()) { city ->
            onItemSelected(city.name, city.coord.lat, city.coord.lon)
        }
        binding.rvCities.layoutManager = LinearLayoutManager(this)
        binding.rvCities.adapter = adapterCityList

        adapterSearchCity = SearchCityAdapter(
            emptyList(),
            onClickListener = { city ->
                onItemSelected(city.name, city.lat, city.lon)
            },
            onBookmarkClick = { city ->
                saveCityToFavourites(city)
            },
            favaouriteIds = { favouriteCityIds }
        )
        binding.rvSuggestions.layoutManager = LinearLayoutManager(this)
        binding.rvSuggestions.adapter = adapterSearchCity
    }

    private fun getMainCity(latitude: Double, longitude: Double) {
        if (ubicacion.isPermissionGrantedOnce()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val responseNameList = RetrofitClient.cityNamesRetrofit
                        .getCityNameReverse(latitude, longitude, apiKey = OpenWeather.API_KEY)
                    val responseName = responseNameList.firstOrNull()!!
                    val responseWeather = RetrofitClient.cityWeather
                        .getCityWeather(latitude, longitude, OpenWeather.API_KEY)

                    withContext(Dispatchers.Main) {
                        placeWeather(responseName, responseWeather)
                    }
                } catch (e: Exception) {
                    Log.e("MAIN_ACTIVITY", "Error al obtener el tiempo: ${e.message}")
                }
            }
        }
    }

    private fun placeWeather(cityResponse: CityResponse, cityWeather: WeatherResponse) {
        binding.tvTemperatura.text = getString(R.string.temp_format, cityWeather.main.temp.toInt())
        binding.tvDescripcion.text = cityWeather.weather[0].description.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }
        Glide.with(this)
            .load("https://openweathermap.org/img/wn/${cityWeather.weather[0].icon}@2x.png")
            .into(binding.ivImagenPrecipitacion)
        binding.tvCiudad.text = cityResponse.name
        binding.tvComunidad.text = cityResponse.state ?: ""
        binding.tvTempMax.text =
            getString(R.string.temp_max_format, cityWeather.main.tempMax.toInt())
        binding.tvTempMin.text =
            getString(R.string.temp_min_format, cityWeather.main.tempMin.toInt())
        cityName = cityResponse.name
    }

    private fun getCities(latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.nearbyCitiesWeather.getNearbyCitiesWeather(
                    latitude,
                    longitude,
                    apiKey = OpenWeather.API_KEY
                )
                val cities = response.list
                val groupedByName = cities
                    .filter { city ->
                        val distance =
                            Geo.distanceInKm(latitude, longitude, city.coord.lat, city.coord.lon)
                        distance > 0.5
                    }
                    .groupBy { it.name }
                val filteredCities = groupedByName.map { (_, duplicates) ->
                    duplicates.minByOrNull { city ->
                        Geo.distanceInKm(latitude, longitude, city.coord.lat, city.coord.lon)
                    }!!
                }

                withContext(Dispatchers.Main) {
                    adapterCityList.updateData(filteredCities)
                }
            } catch (e: Exception) {
                Log.e("CITY_WEATHER_RECYCLER", "Error: ${e.message}")
            }
        }
    }

    private fun onItemSelected(name: String, lat: Double, lon: Double) {
        if (!checkNetwork()) return
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("NAME", name)
        intent.putExtra("LATITUDE", lat)
        intent.putExtra("LONGITUDE", lon)
        startActivity(intent)
    }

    private fun saveCityToFavourites(city: CityResponse) {
        val cityId = city.lat.hashCode() + city.lon.hashCode()
        val isFavourite = favouriteCityIds.contains(cityId)

        if (isFavourite) {
            weatherViewModel.removeFavourite(cityId)
        } else {
            lifecycleScope.launch {
                try {
                    val weather = RetrofitClient.cityWeather.getCityWeather(
                        city.lat,
                        city.lon,
                        OpenWeather.API_KEY
                    )

                    val entity = FavouriteEntity(
                        id = city.lat.hashCode() + city.lon.hashCode(),
                        name = city.name,
                        lat = weather.coord.lat,
                        lon = weather.coord.lon,
                        temp = weather.main.temp,
                        icon = weather.weather[0].icon
                    )

                    weatherViewModel.insertFavourite(entity)
                } catch (e: Exception) {
                    Log.e("MAIN_ACTIVITY", "Error al guardar ciudad: ${e.message}")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        val searchMenuItem = menu?.findItem(R.id.item_buscar)
        val searchView = searchMenuItem?.actionView as SearchView
        searchView.queryHint = getString(R.string.min_three_letter)

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
                binding.rvSuggestions.visibility = View.GONE
                searchView.setQuery("", false)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_favorito -> {
                val intent = Intent(this, FavouritesActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ubicacion.handlePermissionResult(requestCode, grantResults)
    }

    private fun checkNetwork(): Boolean {
        if (!Network.hasNetwork(this)) {
            Toast.makeText(this, R.string.no_network, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        if (isInBackground) {
            if (!checkNetwork()) return
        }
        if (ubicacion.isPermissionGrantedOnce()) {
            fetchLocationAndData()
        }
    }

    override fun onPause() {
        super.onPause()
        isInBackground = true
    }
}