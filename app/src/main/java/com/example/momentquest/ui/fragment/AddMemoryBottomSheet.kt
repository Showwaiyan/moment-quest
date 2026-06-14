package com.example.momentquest.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.example.momentquest.databinding.BottomSheetAddMemoryBinding
import com.example.momentquest.viewmodel.ChallengeViewModel
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.util.Locale
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.momentquest.util.GeocoderHelper

class AddMemoryBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddMemoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChallengeViewModel by viewModels()
    private var challengeId: String = ""
    private var photoUri: Uri? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var onSuccessCallback: (() -> Unit)? = null

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
                    ContextCompat.getColor(requireContext(), com.example.momentquest.R.color.outline)
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
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo pickers
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            showPhotoPreview(photoUri!!)
        }
    }

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoUri = it
            showPhotoPreview(uri)
        }
    }

    companion object {
        private const val TAG = "AddMemoryBottomSheet"

        fun newInstance(challengeId: String, onSuccess: () -> Unit): AddMemoryBottomSheet {
            val fragment = AddMemoryBottomSheet()
            fragment.challengeId = challengeId
            fragment.onSuccessCallback = onSuccess
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddMemoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        observeViewModel()
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        Log.d(TAG, "checkLocationPermission: Checking if ACCESS_FINE_LOCATION permission is granted.")
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "checkLocationPermission: Permission is granted. Fetching location.")
            fetchLocation()
        } else {
            Log.d(TAG, "checkLocationPermission: Permission not granted. Launching request launcher.")
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun onLocationFound(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        Log.d(TAG, "onLocationFound: Coordinates resolved. Lat: $latitude, Lng: $longitude")
        binding.tvLocationCoords.text = String.format(
            Locale.US,
            "Coords: %.4f° N, %.4f° E",
            latitude,
            longitude
        )
    }

    private fun requestNativeLocationUpdate() {
        Log.d(TAG, "requestNativeLocationUpdate: Initiating fallback native LocationManager update.")
        try {
            val context = requireContext()
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                Log.w(TAG, "requestNativeLocationUpdate: LocationManager was null.")
                binding.tvLocationCoords.text = "GPS active, but no location fix."
                return
            }

            val providers = locationManager.getProviders(true)
            if (providers.isEmpty()) {
                Log.w(TAG, "requestNativeLocationUpdate: No enabled providers found.")
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
            Log.d(TAG, "requestNativeLocationUpdate: Using provider '$provider' for native request.")

            locationManager.requestSingleUpdate(provider, object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    Log.d(TAG, "requestNativeLocationUpdate callback: Location retrieved.")
                    onLocationFound(location)
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }, android.os.Looper.getMainLooper())

        } catch (e: SecurityException) {
            Log.e(TAG, "requestNativeLocationUpdate: SecurityException caught due to location permissions.", e)
            binding.tvLocationCoords.text = "Location permission required."
        } catch (e: Exception) {
            Log.e(TAG, "requestNativeLocationUpdate: Exception caught during native location update.", e)
            binding.tvLocationCoords.text = "GPS active, but no location fix."
        }
    }

    private fun fetchLocation() {
        Log.d(TAG, "fetchLocation: Starting Fused Location Provider client retrieval.")
        try {
            val context = requireContext()
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            
            // 1. Try Google Fused Location lastLocation
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    Log.d(TAG, "fetchLocation: Last known location found via Fused Provider.")
                    onLocationFound(location)
                } else {
                    Log.d(TAG, "fetchLocation: Last known location was null. Querying fallback providers.")
                    // 2. Try Native LocationManager lastKnownLocation
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    val gpsLoc = try { locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: Exception) { null }
                    val netLoc = try { locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { null }
                    
                    val bestLocation = gpsLoc ?: netLoc
                    if (bestLocation != null) {
                        Log.d(TAG, "fetchLocation: Fallback last known location found from local manager.")
                        onLocationFound(bestLocation)
                    } else {
                        // 3. Force request updates
                        Log.d(TAG, "fetchLocation: No cached locations. Forcing high-accuracy fresh location request.")
                        binding.tvLocationCoords.text = "GPS active, fetching fresh fix..."
                        
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .addOnSuccessListener { freshLocation ->
                                if (freshLocation != null) {
                                    Log.d(TAG, "fetchLocation: Fresh location resolved.")
                                    onLocationFound(freshLocation)
                                } else {
                                    Log.w(TAG, "fetchLocation: Fresh location was null. Invoking requestNativeLocationUpdate.")
                                    requestNativeLocationUpdate()
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "fetchLocation: Failed to fetch fresh location.", exception)
                                requestNativeLocationUpdate()
                            }
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "fetchLocation: Fused client lastLocation failed. Querying native update.", exception)
                requestNativeLocationUpdate()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "fetchLocation: SecurityException caught due to location permissions.", e)
            binding.tvLocationCoords.text = "Location permission required."
        }
    }

    private fun setupButtons() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.tvLocationCoords.setOnClickListener {
            val lat = latitude
            val lng = longitude
            if (lat != null && lng != null) {
                GeocoderHelper.showAddressDialog(
                    requireContext(),
                    lat,
                    lng,
                    viewLifecycleOwner.lifecycleScope
                )
            }
        }

        binding.photoContainer.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery")
            android.app.AlertDialog.Builder(requireContext())
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
            binding.ivPhotoPreview.visibility = View.GONE
            binding.photoPlaceholder.visibility = View.VISIBLE
            binding.photoActions.visibility = View.GONE
        }

        binding.btnComplete.setOnClickListener {
            val notes = binding.etNotes.text.toString()
            if (latitude == null || longitude == null) {
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "GPS unavailable. Null coordinates stored.",
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).show()
            }
            viewModel.completeChallenge(challengeId, notes, photoUri, latitude, longitude)
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val cacheDir = requireContext().cacheDir
        val tempFile = File.createTempFile("captured_memory_", ".jpg", cacheDir)
        val authority = "${requireContext().packageName}.fileprovider"
        photoUri = FileProvider.getUriForFile(requireContext(), authority, tempFile)
        
        takePictureLauncher.launch(photoUri!!)
    }

    private fun showPhotoPreview(uri: Uri) {
        binding.ivPhotoPreview.visibility = View.VISIBLE
        binding.photoPlaceholder.visibility = View.GONE
        binding.photoActions.visibility = View.VISIBLE
        binding.ivPhotoPreview.setImageURI(uri)
    }

    private fun observeViewModel() {
        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Challenge marked complete!", Toast.LENGTH_SHORT).show()
                onSuccessCallback?.invoke()
                dismiss()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnComplete.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    it,
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
