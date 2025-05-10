package com.example.appclima.utilities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class Location(private val activity: Activity) {
    companion object {
        const val REQUEST_CODE_LOCATION = 100
    }

    fun enableLocation() {
        if (isLocationPermissionGranted()) {
            Log.d("UBICACIÓN", "Permiso concedido")
        } else {
            requestLocationPermission()
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Toast.makeText(
                activity,
                "Vaya a ajustes y active los permisos de ubicación",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    fun handlePermissionResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("UBICACIÓN", "Permiso de ubicación concedido")
            } else {
                Toast.makeText(
                    activity,
                    "Para activar la ubicación, vaya a ajustes y active los permisos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun isPermissionGrantedOnce(): Boolean {
        return isLocationPermissionGranted()
    }

    @SuppressLint("MissingPermission")
    fun getActualLocation(callback: (latitude: Double, longitude: Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity)

        val request =
            CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()

        fusedLocationClient.getCurrentLocation(request, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    Log.d("UBICACIÓN", "$lat,$lon")
                    callback(lat, lon)
                } else {
                    Toast.makeText(
                        activity,
                        "No se pudo obtener la ubicación",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}