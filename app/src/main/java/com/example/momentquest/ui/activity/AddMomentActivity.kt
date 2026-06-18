package com.example.momentquest.ui.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.momentquest.R
import com.example.momentquest.databinding.ActivityAddMomentBinding
import com.example.momentquest.util.GeocoderHelper
import com.example.momentquest.util.UsabilityTracker
import com.example.momentquest.viewmodel.MomentViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.util.Locale

class AddMomentActivity : AppCompatActivity() {
    private val TAG = "AddMomentActivity"

    private lateinit var binding: ActivityAddMomentBinding
    private val viewModel: MomentViewModel by viewModels()

    private var photoUri: Uri? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var clearPhoto: Boolean = false

    // Permission Launchers
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchLocation()
        } else {
            binding.tvLocationCoords.text = "Permission denied. GPS skipped."
            binding.gpsStatusDot.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.outline)
                )
            )
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied. Photo skipped.", Toast.LENGTH_LONG).show()
        }
    }

    // Photo pickers
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            clearPhoto = false
            showPhotoPreview(photoUri!!)
        }
    }

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoUri = it
            clearPhoto = false
            showPhotoPreview(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMomentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
        observeViewModel()
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        Log.d(TAG, "checkLocationPermission: Checking ACCESS_FINE_LOCATION permission.")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLocation()
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun onLocationFound(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        binding.tvLocationCoords.text = String.format(
            Locale.US,
            "Coords: %.4f° N, %.4f° E",
            latitude,
            longitude
        )
    }

    private fun fetchLocation() {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onLocationFound(location)
                } else {
                    val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    val gpsLoc = try { locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: Exception) { null }
                    val netLoc = try { locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { null }
                    val bestLocation = gpsLoc ?: netLoc
                    if (bestLocation != null) {
                        onLocationFound(bestLocation)
                    } else {
                        binding.tvLocationCoords.text = "GPS active, fetching fresh fix..."
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .addOnSuccessListener { freshLocation ->
                                if (freshLocation != null) {
                                    onLocationFound(freshLocation)
                                } else {
                                    requestNativeLocationUpdate()
                                }
                            }
                            .addOnFailureListener {
                                requestNativeLocationUpdate()
                            }
                    }
                }
            }.addOnFailureListener {
                requestNativeLocationUpdate()
            }
        } catch (e: SecurityException) {
            binding.tvLocationCoords.text = "Location permission required."
        }
    }

    private fun requestNativeLocationUpdate() {
        try {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                binding.tvLocationCoords.text = "GPS active, but no location fix."
                return
            }
            val providers = locationManager.getProviders(true)
            if (providers.isEmpty()) {
                binding.tvLocationCoords.text = "Location services are disabled."
                return
            }
            val provider = if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                LocationManager.NETWORK_PROVIDER
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                LocationManager.GPS_PROVIDER
            } else {
                providers.first()
            }

            locationManager.requestSingleUpdate(provider, object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    onLocationFound(location)
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }, android.os.Looper.getMainLooper())
        } catch (e: SecurityException) {
            binding.tvLocationCoords.text = "Location permission required."
        } catch (e: Exception) {
            binding.tvLocationCoords.text = "GPS active, but no location fix."
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.tvLocationCoords.setOnClickListener {
            val lat = latitude
            val lng = longitude
            if (lat != null && lng != null) {
                GeocoderHelper.showAddressDialog(
                    this,
                    lat,
                    lng,
                    lifecycleScope
                )
            }
        }

        binding.photoContainer.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery")
            android.app.AlertDialog.Builder(this)
                .setTitle("Add Photo")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> checkCameraPermission()
                        1 -> selectImageLauncher.launch("image/*")
                    }
                }
                .show()
        }

        binding.btnRemovePhoto.setOnClickListener {
            photoUri = null
            clearPhoto = true
            binding.ivPhotoPreview.visibility = View.GONE
            binding.photoPlaceholder.visibility = View.VISIBLE
            binding.photoActions.visibility = View.GONE
        }

        binding.btnSave.setOnClickListener {
            saveMoment()
        }

        binding.btnSaveIcon.setOnClickListener {
            saveMoment()
        }
    }

    private fun saveMoment() {
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()

        val mood = when (binding.chipGroupMood.checkedChipId) {
            R.id.chipGrateful -> "Grateful"
            R.id.chipSurprised -> "Surprised"
            R.id.chipReflective -> "Reflective"
            else -> "Happy"
        }

        if (latitude == null || longitude == null) {
            Toast.makeText(this, "GPS unavailable. Null coordinates stored.", Toast.LENGTH_SHORT).show()
        }

        viewModel.addMoment(title, description, mood, photoUri, latitude, longitude)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val cacheDir = cacheDir
        val tempFile = File.createTempFile("captured_photo_", ".jpg", cacheDir)
        val authority = "${packageName}.fileprovider"
        photoUri = FileProvider.getUriForFile(this, authority, tempFile)

        takePictureLauncher.launch(photoUri!!)
    }

    private fun showPhotoPreview(uri: Uri) {
        binding.ivPhotoPreview.visibility = View.VISIBLE
        binding.photoPlaceholder.visibility = View.GONE
        binding.photoActions.visibility = View.VISIBLE
        binding.ivPhotoPreview.setImageURI(uri)
    }

    private fun observeViewModel() {
        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                if (UsabilityTracker.isTrackingEnabled(this)) {
                    val timeMs = UsabilityTracker.getElapsedTimeMs(this)
                    UsabilityTracker.showFeedbackDialog(this, "A", timeMs, lifecycleScope) {
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Moment captured successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnSave.isEnabled = !isLoading
            binding.btnSaveIcon.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }
}
