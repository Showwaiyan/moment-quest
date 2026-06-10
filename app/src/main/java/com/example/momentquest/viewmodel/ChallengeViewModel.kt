package com.example.momentquest.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.momentquest.model.Challenge
import com.example.momentquest.model.Memory
import com.example.momentquest.repository.ChallengeRepository
import com.example.momentquest.repository.StorageRepository
import kotlinx.coroutines.launch

class ChallengeViewModel(application: Application) : AndroidViewModel(application) {
    private val challengeRepository = ChallengeRepository()
    private val storageRepository = StorageRepository()

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> get() = _saveSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _challengeDetails = MutableLiveData<Challenge?>()
    val challengeDetails: LiveData<Challenge?> get() = _challengeDetails

    private val _memoriesList = MutableLiveData<List<Memory>>()
    val memoriesList: LiveData<List<Memory>> get() = _memoriesList

    fun addChallenge(title: String, category: String, deadline: Long?) {
        if (title.isBlank()) {
            _error.value = "Title cannot be empty"
            return
        }

        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val challenge = Challenge(
                    title = title.trim(),
                    category = category,
                    deadline = deadline,
                    status = "PENDING"
                )
                challengeRepository.addChallenge(context, challenge)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save challenge"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadChallengeDetails(challengeId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val challenges = challengeRepository.getChallenges(context)
                val found = challenges.find { it.id == challengeId }
                _challengeDetails.value = found
                if (found != null) {
                    val memories = challengeRepository.getMemories(context, challengeId)
                    _memoriesList.value = memories
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load details"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun completeChallenge(challengeId: String, notes: String, photoUri: Uri?, latitude: Double?, longitude: Double?) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                var photoUrl: String? = null
                if (photoUri != null) {
                    photoUrl = storageRepository.uploadPhoto(context, photoUri, "challenge_memories")
                }
                
                val memory = Memory(
                    notes = notes.trim(),
                    photoUrl = photoUrl,
                    latitude = latitude,
                    longitude = longitude,
                    completedAt = System.currentTimeMillis()
                )
                
                challengeRepository.completeChallenge(context, challengeId, memory)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to complete challenge"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteChallenge(challengeId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                challengeRepository.deleteChallenge(context, challengeId)
                _challengeDetails.value = null
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete challenge"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMemory(challengeId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                challengeRepository.deleteMemory(context, challengeId)
                loadChallengeDetails(challengeId)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete memory"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
