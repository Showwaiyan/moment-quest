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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File

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

    private fun fetchLocation() {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    binding.tvLocationCoords.text = String.format(
                        "Coords: %.4f° N, %.4f° E",
                        latitude,
                        longitude
                    )
                } else {
                    binding.tvLocationCoords.text = "GPS active, fetching fresh fix..."
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { freshLocation ->
                            if (freshLocation != null) {
                                latitude = freshLocation.latitude
                                longitude = freshLocation.longitude
                                binding.tvLocationCoords.text = String.format(
                                    "Coords: %.4f° N, %.4f° E",
                                    latitude,
                                    longitude
                                )
                            } else {
                                binding.tvLocationCoords.text = "GPS active, but no location fix."
                            }
                        }
                        .addOnFailureListener {
                            binding.tvLocationCoords.text = "Failed to get fresh GPS fix."
                        }
                }
            }.addOnFailureListener {
                binding.tvLocationCoords.text = "Failed to fetch GPS coordinates."
            }
        } catch (e: SecurityException) {
            binding.tvLocationCoords.text = "Location permission required."
        }
    }

    private fun setupButtons() {
        binding.btnClose.setOnClickListener {
            dismiss()
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
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
