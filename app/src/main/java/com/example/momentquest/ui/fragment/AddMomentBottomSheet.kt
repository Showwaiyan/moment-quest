package com.example.momentquest.ui.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.momentquest.R
import com.example.momentquest.databinding.BottomSheetAddMomentBinding
import com.example.momentquest.util.GeocoderHelper
import com.example.momentquest.util.UsabilityTracker
import com.example.momentquest.viewmodel.MomentViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import java.util.Locale

class AddMomentBottomSheet : BottomSheetDialogFragment() {
    private val TAG = "AddMomentBottomSheet"

    private var _binding: BottomSheetAddMomentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MomentViewModel by viewModels()

    private var photoUri: Uri? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var clearPhoto: Boolean = false
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
                    ContextCompat.getColor(requireContext(), R.color.outline)
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
            Toast.makeText(requireContext(), "Camera permission denied. Photo skipped.", Toast.LENGTH_SHORT).show()
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

    companion object {
        fun newInstance(onSuccess: () -> Unit): AddMomentBottomSheet {
            val fragment = AddMomentBottomSheet()
            fragment.onSuccessCallback = onSuccess
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddMomentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        observeViewModel()
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        Log.d(TAG, "checkLocationPermission: Checking ACCESS_FINE_LOCATION permission.")
        if (ContextCompat.checkSelfPermission(
                requireContext(),
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
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onLocationFound(location)
                } else {
                    val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
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
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
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
            clearPhoto = true
            binding.ivPhotoPreview.visibility = View.GONE
            binding.photoPlaceholder.visibility = View.VISIBLE
            binding.photoActions.visibility = View.GONE
        }

        binding.btnSave.setOnClickListener {
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
            Toast.makeText(requireContext(), "GPS unavailable. Null coordinates stored.", Toast.LENGTH_SHORT).show()
        }

        viewModel.addMoment(title, description, mood, photoUri, latitude, longitude)
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
        val tempFile = File.createTempFile("captured_photo_", ".jpg", cacheDir)
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
                onSuccessCallback?.invoke()
                if (UsabilityTracker.isTrackingEnabled(requireContext())) {
                    val timeMs = UsabilityTracker.getElapsedTimeMs(requireContext())
                    UsabilityTracker.showFeedbackDialog(requireActivity(), "B", timeMs, viewLifecycleOwner.lifecycleScope) {
                        dismiss()
                    }
                } else {
                    Toast.makeText(requireContext(), "Moment captured successfully!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
