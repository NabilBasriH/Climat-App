package com.example.appclima.presentation.favourites

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appclima.R
import com.example.appclima.core.utils.Network
import com.example.appclima.presentation.viewmodel.WeatherViewModel
import com.example.appclima.databinding.ActivityFavouritesBinding
import com.example.appclima.presentation.adapter.FavouriteCityAdapter
import com.example.appclima.presentation.details.DetailsActivity

class FavouritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavouritesBinding
    private lateinit var viewModel: WeatherViewModel
    private lateinit var adapter: FavouriteCityAdapter

    private var lastTextSearch: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFavouritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarFavourites)
        supportActionBar?.title = getString(R.string.app_name)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initRecycler()
    }

    private fun initRecycler() {
        adapter = FavouriteCityAdapter(emptyList()) { city ->
            onItemSelected(city.name, city.lat, city.lon)
        }
        binding.rvCities.layoutManager = GridLayoutManager(this, 2)
        binding.rvCities.adapter = adapter

        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
        viewModel.favourites.observe(this) { cities ->
            adapter.updateData(cities)
        }
    }

    private fun onItemSelected(name: String, lat: Double, lon: Double) {
        if (!Network.checkNetwork(this)) return
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("NAME", name)
        intent.putExtra("LATITUDE", lat)
        intent.putExtra("LONGITUDE", lon)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_favoritos, menu)

        val searchMenuItem = menu?.findItem(R.id.item_buscarFav)
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
                adapter.filterCities(lastTextSearch)
                return true
            }
        })

        searchView.setOnCloseListener {
            searchView.setQuery("", false)
            true
        }

        binding.main.setOnClickListener {
            if (!searchView.isIconified) {
                hideKeyboard()
                searchView.setQuery("", false)
                searchMenuItem.collapseActionView()
            }
        }

        return super.onCreateOptionsMenu(menu)
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

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        } ?: run {
            imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        }
    }
}