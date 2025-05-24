package com.example.appclima.core.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.provider.Settings
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.appclima.R
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class AppLocation(private val activity: Activity) {
    companion object {
        const val REQUEST_CODE_LOCATION = 100
    }

    var onPermissionGranted: (() -> Unit)? = null
    private var onPermissionDenied: (() -> Unit)? = null
    private var gpsDialog: AlertDialog? = null

    fun enableLocation() {
        if (isLocationPermissionGranted()) {
            Log.d("AppLocation", "Permiso ya fue concedido")
            onPermissionGranted?.invoke()
        } else {
            requestLocationPermission()
        }
    }

    private fun isLocationPermissionGranted(): Boolean = ContextCompat.checkSelfPermission(
        activity, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED


    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.activate_location_permission)
            .setMessage(R.string.activate_location_permission_message)
            .setPositiveButton(R.string.go_to_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", activity.packageName, null)
                }
                activity.startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    fun handlePermissionResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("AppLocation", "Permiso concedido")
                onPermissionGranted?.invoke()
            } else {
                Log.e("AppLocation", "Permiso denegado")
                onPermissionDenied?.invoke()
            }
        }
    }

    fun isPermissionGrantedOnce(): Boolean {
        return isLocationPermissionGranted()
    }

    fun isGpsEnabled(): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun showGpsDisabledDialog() {
        if (gpsDialog?.isShowing == true) return

        gpsDialog = AlertDialog.Builder(activity)
            .setTitle(R.string.activate_gps)
            .setMessage(R.string.activate_gps_message)
            .setPositiveButton(R.string.go_to_settings) { _, _ ->
                activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()

        gpsDialog?.show()
    }

    fun dismissGpsDialog() {
        gpsDialog?.dismiss()
        gpsDialog = null
    }

    @SuppressLint("MissingPermission")
    fun getActualLocation(callback: (latitude: Double, longitude: Double) -> Unit) {
        if (!isLocationPermissionGranted()) {
            Log.e("AppLocation", "Permiso no concedido")
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity)
        val request = CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()

        fusedLocationClient.getCurrentLocation(request, null)
            .addOnSuccessListener { location ->
                location?.let {
                    callback(it.latitude, it.longitude)
                } ?: run {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                        lastLocation?.let {
                            callback(lastLocation.latitude, lastLocation.longitude)
                        } ?: run {
                            Toast.makeText(
                                activity,
                                R.string.location_not_found,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity,
                    "${R.string.location_error}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}