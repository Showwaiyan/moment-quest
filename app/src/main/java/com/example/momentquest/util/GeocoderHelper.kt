package com.example.momentquest.util

import android.content.Context
import android.location.Geocoder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object GeocoderHelper {
    private const val TAG = "GeocoderHelper"
    
    // In-memory cache for coordinates to address strings to avoid redundant network lookups
    private val addressCache = ConcurrentHashMap<Pair<Double, Double>, String>()

    suspend fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): String {
        val cacheKey = Pair(latitude, longitude)
        addressCache[cacheKey]?.let {
            return it
        }

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                // Use the synchronous API wrapped in Dispatchers.IO for maximum compatibility across SDK versions
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val addressText = if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    address.getAddressLine(0) ?: "${String.format(Locale.US, "%.4f", latitude)}, ${String.format(Locale.US, "%.4f", longitude)}"
                } else {
                    "${String.format(Locale.US, "%.4f", latitude)}, ${String.format(Locale.US, "%.4f", longitude)}"
                }
                
                addressCache[cacheKey] = addressText
                addressText
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reverse geocode Lat: $latitude, Lng: $longitude", e)
                "${String.format(Locale.US, "%.4f", latitude)}, ${String.format(Locale.US, "%.4f", longitude)}"
            }
        }
    }
}
