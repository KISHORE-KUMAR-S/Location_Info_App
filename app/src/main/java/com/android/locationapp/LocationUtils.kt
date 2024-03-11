package com.android.locationapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class LocationUtils(context : Context) {
    private val _fusedLocationClient : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val _context : Context = context

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(viewModel: LocationViewModel) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                p0.lastLocation?.let {
                    val location = LocationData(latitude = it.latitude, longitude = it.longitude)
                    viewModel.updateLocation(location)
                }
            }
        }

        val locationRequest = LocationRequest
            .Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000
            ).build()

        _fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }



    fun hasLocationPermission(context: Context) : Boolean = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            &&
            ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun reverseGeocodeLocation(location: LocationData) : String {
        val geocoder = Geocoder(_context, Locale.getDefault())
        val coordinates = LatLng(location.latitude, location.longitude)
        val address : MutableList<Address>? =
            geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)!!

        return when {
            address?.isNotEmpty() == true -> {
                address[0].getAddressLine(0)
            }
            else -> {
                "Address not found"
            }
        }
    }
}