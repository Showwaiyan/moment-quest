package com.example.momentquest.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object GeocoderHelper {
    private const val TAG = "GeocoderHelper"
    
    // In-memory cache for coordinates to Address objects
    private val addressCache = ConcurrentHashMap<Pair<Double, Double>, Address>()

    suspend fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): Address? {
        val cacheKey = Pair(latitude, longitude)
        addressCache[cacheKey]?.let {
            return it
        }

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = addresses?.firstOrNull()
                if (address != null) {
                    addressCache[cacheKey] = address
                }
                address
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reverse geocode Lat: $latitude, Lng: $longitude", e)
                null
            }
        }
    }

    fun showAddressDialog(context: Context, latitude: Double, longitude: Double, scope: CoroutineScope) {
        scope.launch {
            val address = getAddressFromLocation(context, latitude, longitude)
            val message = if (address != null) {
                val city = address.locality ?: "Unknown"
                val region = address.adminArea ?: "Unknown"
                val country = address.countryName ?: "Unknown"
                val fullAddress = address.getAddressLine(0) ?: "Unknown"
                
                "City: $city\nRegion: $region\nCountry: $country\n\nFull Address:\n$fullAddress"
            } else {
                "Unable to resolve address for coordinates:\nLat: $latitude, Lng: $longitude"
            }
            
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Location Details")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
