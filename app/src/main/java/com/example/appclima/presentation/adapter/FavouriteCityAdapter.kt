package com.example.appclima.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appclima.R
import com.example.appclima.databinding.ItemListCityFavouriteBinding
import com.example.appclima.data.local.model.FavouriteEntity

class FavouriteCityAdapter(private var favouriteCities: List<FavouriteEntity>, private val onClickListener: (FavouriteEntity) -> Unit): RecyclerView.Adapter<FavouriteCityAdapter.FavouriteCityHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavouriteCityHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return FavouriteCityHolder(layoutInflater.inflate(R.layout.item_list_city_favourite, parent, false))
    }

    override fun onBindViewHolder(holder: FavouriteCityHolder, position: Int) {
        holder.render(favouriteCities[position], onClickListener)
    }

    override fun getItemCount(): Int = favouriteCities.size

    class FavouriteCityHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemListCityFavouriteBinding.bind(view)

        fun render(city: FavouriteEntity, onClickListener: (FavouriteEntity) -> Unit) {
            binding.tvFTempView.text = itemView.context.getString(R.string.temp_format, city.temp.toInt())
            binding.tvFCiudadNameView.text = city.name

            Glide.with(itemView)
                .load("https://openweathermap.org/img/wn/${city.icon}@2x.png")
                .into(binding.ivFImagenPrepView)

            itemView.setOnClickListener {
                onClickListener(city)
            }
        }
    }

    fun updateData(newFavouritesCities: List<FavouriteEntity>) {
        favouriteCities = newFavouritesCities
        notifyDataSetChanged()
    }
}