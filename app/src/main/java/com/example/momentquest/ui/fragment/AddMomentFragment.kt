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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.momentquest.databinding.FragmentAddMomentBinding
import com.example.momentquest.viewmodel.MomentViewModel
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.util.Locale
import android.util.Log

class AddMomentFragment : Fragment() {
    private val TAG = "AddMomentFragment"

    private var _binding: FragmentAddMomentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MomentViewModel by viewModels()
    private var photoUri: Uri? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var momentId: String? = null
    private var clearPhoto: Boolean = false
    private var isDeleting: Boolean = false

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
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                "Camera permission denied. Photo skipped.",
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            ).show()
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMomentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        momentId = arguments?.getString("moment_id")

        setupButtons()
        observeViewModel()

        if (!momentId.isNullOrEmpty()) {
            viewModel.loadMomentDetails(momentId!!)
        } else {
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        Log.d(TAG, "checkLocationPermission: Checking ACCESS_FINE_LOCATION permission.")
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "checkLocationPermission: Permission granted. Fetching location.")
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
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
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

        binding.btnSaveIcon.setOnClickListener {
            saveMoment()
        }
    }

    private fun saveMoment() {
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()
        
        val mood = when (binding.chipGroupMood.checkedChipId) {
            com.example.momentquest.R.id.chipGrateful -> "Grateful"
            com.example.momentquest.R.id.chipSurprised -> "Surprised"
            com.example.momentquest.R.id.chipReflective -> "Reflective"
            else -> "Happy"
        }

        if (latitude == null || longitude == null) {
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                "GPS unavailable. Null coordinates stored.",
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            ).show()
        }

        if (!momentId.isNullOrEmpty()) {
            viewModel.updateMoment(momentId!!, title, description, mood, photoUri, clearPhoto, latitude, longitude)
        } else {
            viewModel.addMoment(title, description, mood, photoUri, latitude, longitude)
        }
    }

    private fun bindMomentToViews(moment: com.example.momentquest.model.Moment) {
        binding.tvScreenTitle.text = "Edit Moment"
        binding.btnSave.text = "Update Moment"
        binding.etTitle.setText(moment.title)
        binding.etDescription.setText(moment.description)
        
        val moodId = when (moment.mood) {
            "Grateful" -> com.example.momentquest.R.id.chipGrateful
            "Surprised" -> com.example.momentquest.R.id.chipSurprised
            "Reflective" -> com.example.momentquest.R.id.chipReflective
            else -> com.example.momentquest.R.id.chipHappy
        }
        binding.chipGroupMood.check(moodId)
        
        if (!moment.photoUrl.isNullOrEmpty()) {
            val file = java.io.File(moment.photoUrl)
            if (file.exists()) {
                photoUri = android.net.Uri.fromFile(file)
                showPhotoPreview(photoUri!!)
            }
        }
        
        if (moment.latitude != null && moment.longitude != null) {
            latitude = moment.latitude
            longitude = moment.longitude
            binding.tvLocationCoords.text = String.format(
                Locale.US,
                "Coords: %.4f° N, %.4f° E",
                latitude,
                longitude
            )
        }

        binding.btnDelete.visibility = View.VISIBLE
        binding.btnDelete.setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Moment")
                .setMessage("Are you sure you want to delete this moment?")
                .setPositiveButton("Delete") { _, _ ->
                    isDeleting = true
                    viewModel.deleteMoment(moment.id)
                }
                .setNegativeButton("Cancel", null)
                .show()
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
                val msg = if (isDeleting) {
                    "Moment deleted successfully!"
                } else if (!momentId.isNullOrEmpty()) {
                    "Moment updated successfully!"
                } else {
                    "Moment captured successfully!"
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }

        viewModel.momentDetails.observe(viewLifecycleOwner) { moment ->
            moment?.let { bindMomentToViews(it) }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSave.isEnabled = !isLoading
            binding.btnSaveIcon.isEnabled = !isLoading
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

    companion object {
        fun newInstance(momentId: String): AddMomentFragment {
            return AddMomentFragment().apply {
                arguments = Bundle().apply {
                    putString("moment_id", momentId)
                }
            }
        }
    }
}
