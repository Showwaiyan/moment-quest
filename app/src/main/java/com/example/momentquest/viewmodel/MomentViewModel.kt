package com.example.momentquest.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.momentquest.model.Moment
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
}
