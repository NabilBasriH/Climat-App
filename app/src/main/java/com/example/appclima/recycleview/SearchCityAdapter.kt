package com.example.appclima.recycleview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appclima.model.CityResponse
import com.example.appclima.R
import com.example.appclima.databinding.ItemListCitySearchBinding

class SearchCityAdapter(
    private var cities: List<CityResponse>,
    private val onClickListener: (CityResponse) -> Unit
): RecyclerView.Adapter<SearchCityAdapter.CityHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return CityHolder(layoutInflater.inflate(R.layout.item_list_city_search, parent, false))
    }

    override fun onBindViewHolder(holder: CityHolder, position: Int) {
        holder.render(cities[position], onClickListener)
    }

    override fun getItemCount(): Int = cities.size

    class CityHolder(view: View): RecyclerView.ViewHolder(view) {
        private val binding = ItemListCitySearchBinding.bind(view)

        fun render(city: CityResponse, onClickListener: (CityResponse) -> Unit) {
            binding.tvNombre.text = city.name
            binding.tvPais.text = city.state ?: ""
            binding.tvPaisCorto.text = city.country

            itemView.setOnClickListener {
                onClickListener(city)
            }
        }
    }

    fun updateData(newCities: List<CityResponse>) {
        cities = newCities
        notifyDataSetChanged()
    }
}