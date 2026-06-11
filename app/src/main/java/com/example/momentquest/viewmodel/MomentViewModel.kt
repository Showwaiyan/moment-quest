package com.example.momentquest.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.momentquest.model.Moment
import com.example.momentquest.repository.FileStorageHelper
import com.example.momentquest.repository.MomentRepository
import com.example.momentquest.repository.StorageRepository
import kotlinx.coroutines.launch

class MomentViewModel(application: Application) : AndroidViewModel(application) {
    private val momentRepository = MomentRepository()
    private val storageRepository = StorageRepository()

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> get() = _saveSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _momentDetails = MutableLiveData<Moment?>()
    val momentDetails: LiveData<Moment?> get() = _momentDetails

    fun loadMomentDetails(momentId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val moments = momentRepository.getMoments(context)
                val found = moments.find { it.id == momentId }
                _momentDetails.value = found
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load moment"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addMoment(title: String, description: String, mood: String, photoUri: Uri?, latitude: Double?, longitude: Double?) {
        if (title.isBlank()) {
            _error.value = "Title cannot be empty"
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                var photoUrl: String? = null
                if (photoUri != null) {
                    photoUrl = storageRepository.uploadPhoto(context, photoUri, "moments")
                }

                val moment = Moment(
                    title = title.trim(),
                    description = description.trim(),
                    mood = mood,
                    photoUrl = photoUrl,
                    latitude = latitude,
                    longitude = longitude,
                    createdAt = System.currentTimeMillis()
                )

                momentRepository.addMoment(context, moment)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save moment"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMoment(momentId: String, title: String, description: String, mood: String, photoUri: Uri?, clearPhoto: Boolean, latitude: Double?, longitude: Double?) {
        if (title.isBlank()) {
            _error.value = "Title cannot be empty"
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val moments = momentRepository.getMoments(context)
                val found = moments.find { it.id == momentId }
                
                if (found != null) {
                    var finalPhotoPath = found.photoUrl
                    if (clearPhoto) {
                        if (!found.photoUrl.isNullOrEmpty()) {
                            FileStorageHelper.deleteFile(found.photoUrl)
                        }
                        finalPhotoPath = null
                    } else if (photoUri != null) {
                        // Save new photo
                        val newPhotoPath = storageRepository.uploadPhoto(context, photoUri, "moments")
                        // Delete old photo
                        if (!found.photoUrl.isNullOrEmpty()) {
                            FileStorageHelper.deleteFile(found.photoUrl)
                        }
                        finalPhotoPath = newPhotoPath
                    }

                    val updated = Moment(
                        id = momentId,
                        title = title.trim(),
                        description = description.trim(),
                        mood = mood,
                        photoUrl = finalPhotoPath,
                        latitude = latitude,
                        longitude = longitude,
                        createdAt = found.createdAt
                    )
                    momentRepository.updateMoment(context, updated)
                    _saveSuccess.value = true
                } else {
                    _error.value = "Moment not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update moment"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMoment(momentId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                momentRepository.deleteMoment(context, momentId)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete moment"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
