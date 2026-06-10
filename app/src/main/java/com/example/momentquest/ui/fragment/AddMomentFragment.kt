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
import com.example.momentquest.databinding.FragmentAddMomentBinding
import com.example.momentquest.viewmodel.MomentViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File

class AddMomentFragment : Fragment() {

    private var _binding: FragmentAddMomentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MomentViewModel by viewModels()
    private var photoUri: Uri? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

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
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.photoContainer.setOnClickListener {
            // Ask user for Camera or Gallery
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
                Toast.makeText(requireContext(), "Moment captured successfully!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSave.isEnabled = !isLoading
            binding.btnSaveIcon.isEnabled = !isLoading
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
