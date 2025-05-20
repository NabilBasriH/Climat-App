package com.example.appclima.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appclima.domain.model.CityResponse
import com.example.appclima.R
import com.example.appclima.databinding.ItemListCitySearchBinding
import com.example.appclima.data.local.model.FavouriteEntity

class SearchCityAdapter(
    private var cities: List<CityResponse>,
    private val onClickListener: (CityResponse) -> Unit,
    private val onBookmarkClick: (CityResponse) -> Unit,
    private var favouritesCities: List<FavouriteEntity> = emptyList(),
    private val favaouriteIds: () -> Set<Int>
): RecyclerView.Adapter<SearchCityAdapter.CityHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return CityHolder(layoutInflater.inflate(R.layout.item_list_city_search, parent, false))
    }

    override fun onBindViewHolder(holder: CityHolder, position: Int) {
        val city = cities[position]
        val isFavourite = favaouriteIds().contains(city.lat.hashCode() + city.lon.hashCode())
        holder.render(city, onClickListener, onBookmarkClick, isFavourite)
    }

    override fun getItemCount(): Int = cities.size

    class CityHolder(view: View): RecyclerView.ViewHolder(view) {
        private val binding = ItemListCitySearchBinding.bind(view)

        fun render(
            city: CityResponse,
            onClickListener: (CityResponse) -> Unit,
            onBookmarkClick: (CityResponse) -> Unit,
            isFavourite: Boolean
            ) {
            binding.tvNombre.text = city.name
            binding.tvPais.text = city.state ?: ""
            binding.tvPaisCorto.text = city.country

            val iconRes = if (isFavourite) R.drawable.icon_bookmark else R.drawable.icon_bookmark_empty
            binding.ivGuardar.setImageResource(iconRes)

            itemView.setOnClickListener {
                onClickListener(city)
            }

            binding.ivGuardar.setOnClickListener {
                onBookmarkClick(city)
            }
        }
    }

    fun updateData(newCities: List<CityResponse>) {
        cities = newCities
        notifyDataSetChanged()
    }
}