package com.example.appclima.recycleview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appclima.R
import com.example.appclima.model.WeatherResponse
import com.example.appclima.databinding.ItemListCityOtherBinding

class CityInfoAdapter(private var weatherCities: List<WeatherResponse>, private val onClickListener: (WeatherResponse) -> Unit): RecyclerView.Adapter<CityInfoAdapter.CityInfoHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityInfoHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return CityInfoHolder(layoutInflater.inflate(R.layout.item_list_city_other, parent, false))
    }

    override fun onBindViewHolder(holder: CityInfoHolder, position: Int) {
        holder.render(weatherCities[position], onClickListener)
    }

    override fun getItemCount(): Int = weatherCities.size

    class CityInfoHolder(view: View): RecyclerView.ViewHolder(view) {
        private val binding = ItemListCityOtherBinding.bind(view)

        fun render(weather: WeatherResponse, onClickListener: (WeatherResponse) -> Unit) {
            binding.tvTempView.text = itemView.context.getString(R.string.temp_format, weather.main.temp.toInt())
            binding.tvCiudadNameView.text = weather.name

            Glide.with(itemView)
                .load("https://openweathermap.org/img/wn/${weather.weather[0].icon}@2x.png")
                .into(binding.ivImagenPrepView)

            itemView.setOnClickListener {
                onClickListener(weather)
            }
        }
    }

    fun updateData(newWeatherCities: List<WeatherResponse>) {
        weatherCities = newWeatherCities
        notifyDataSetChanged()
    }
}