package com.example.appclima.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appclima.R

class Network {
    companion object {
        fun hasNetwork(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false

            val network = connectivityManager.activeNetwork ?: return false

            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        }

        fun checkNetwork(context: Context): Boolean {
            if (!hasNetwork(context)) {
                Toast.makeText(context, R.string.no_network, Toast.LENGTH_SHORT).show()
                return false
            }
            return true
        }
    }
}