package com.yewon.airquality

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute
import android.location.Location
import android.location.LocationManager
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat

class LocationProvider(val context : Context) {
    private var location : Location? = null
    private var locationManager : LocationManager? = null

    init{
        getLocation()

    }

    private fun getLocation() : Location? {
        try{
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            var gpsLocation : Location? = null
            var networkLocation : Location? = null


            val isGPSEnabled = locationManager !!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager !!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if(!isGPSEnabled && !isNetworkEnabled){
                return null
            }
            else{
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return null
                }

                if(isNetworkEnabled){
                    networkLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                if(isGPSEnabled){
                    gpsLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }

                if(gpsLocation!= null && networkLocation != null){
                    if(gpsLocation.accuracy > networkLocation.accuracy){
                        location = gpsLocation
                    }else{
                        //location = networkLocation
                        location = gpsLocation
                    }
                }else{
                    if(gpsLocation != null){
                        location = gpsLocation
                    }
                    if(networkLocation!= null){
                        location = networkLocation
                    }

                }
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
        return location
    }

    fun getLocationLatitude() : Double? {
        return location?.latitude
    }

    fun getLocationLongitude() : Double? {
        return location?.longitude
    }

}