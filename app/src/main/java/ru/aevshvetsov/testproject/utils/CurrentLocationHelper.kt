package ru.aevshvetsov.testproject.utils

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import org.osmdroid.util.GeoPoint

/**
 * Created by Alexander Shvetsov on 09.10.2020
 */
class CurrentLocationHelper(private val locationManager: LocationManager) {
    var hasGps = false
    var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    @SuppressLint("MissingPermission")
    fun getLocation(): GeoPoint {
        var point: GeoPoint? = null
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork) {

            if (hasGps) {
                Log.d("CodeAndroidLocation", "hasGps")
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    0F,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location?) {
                            if (location != null) {

                                Log.d("M_CurrentLocationHelper", " GPS Latitude : " + locationGps!!.latitude)
                                Log.d("M_CurrentLocationHelper", " GPS Longitude : " + locationGps!!.longitude)
                                point = GeoPoint(locationGps)
                            }
                        }

                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

                        override fun onProviderEnabled(provider: String?) {}

                        override fun onProviderDisabled(provider: String?) {}
                    })

                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null)
                    locationGps = localGpsLocation
            }
            if (hasNetwork) {
                Log.d("M_CurrentLocationHelper", "hasGps")
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000,
                    0F,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location?) {
                            if (location != null) {
                                locationNetwork = location
                                Log.d("M_CurrentLocationHelper", " Network Latitude : " + locationNetwork!!.latitude)
                                Log.d("M_CurrentLocationHelper", " Network Longitude : " + locationNetwork!!.longitude)
                                point = GeoPoint(locationNetwork)
                            }
                        }

                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                        }

                        override fun onProviderEnabled(provider: String?) {

                        }

                        override fun onProviderDisabled(provider: String?) {

                        }

                    })

                val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null)
                    locationNetwork = localNetworkLocation
            }

            when {
                locationGps != null && locationNetwork != null -> {
                    if (locationGps!!.accuracy > locationNetwork!!.accuracy) {
                        Log.d("M_CurrentLocationHelper", " Network Latitude : " + locationNetwork!!.latitude)
                        Log.d("M_CurrentLocationHelper", " Network Longitude : " + locationNetwork!!.longitude)
                        point = GeoPoint(locationNetwork)
                    } else {
                        Log.d("M_CurrentLocationHelper", " GPS Latitude : " + locationGps!!.latitude)
                        Log.d("M_CurrentLocationHelper", " GPS Longitude : " + locationGps!!.longitude)
                        point = GeoPoint(locationGps)
                    }
                }
                locationGps != null && locationNetwork == null -> {
                    point = GeoPoint(locationGps)
                }
                locationGps == null && locationNetwork != null -> {
                    point = GeoPoint(locationNetwork)
                }
            }

        }
        return point!!
    }
}