package com.example.momentquest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.momentquest.repository.ChallengeRepository
import com.example.momentquest.repository.MomentRepository
import kotlinx.coroutines.launch

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val challengeRepository = ChallengeRepository()
    private val momentRepository = MomentRepository()

    private val _totalChallenges = MutableLiveData<Int>()
    val totalChallenges: LiveData<Int> get() = _totalChallenges

    private val _completedChallenges = MutableLiveData<Int>()
    val completedChallenges: LiveData<Int> get() = _completedChallenges

    private val _completionRate = MutableLiveData<Int>()
    val completionRate: LiveData<Int> get() = _completionRate

    private val _totalMoments = MutableLiveData<Int>()
    val totalMoments: LiveData<Int> get() = _totalMoments

    // Category progress maps a category string to a Pair of (completed, total)
    private val _categoryBreakdown = MutableLiveData<Map<String, Pair<Int, Int>>>()
    val categoryBreakdown: LiveData<Map<String, Pair<Int, Int>>> get() = _categoryBreakdown

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadStats() {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val challenges = challengeRepository.getChallenges(context)
                val moments = momentRepository.getMoments(context)

                val totalCount = challenges.size
                val completedCount = challenges.count { it.status == "COMPLETED" }
                val rate = if (totalCount > 0) (completedCount * 100) / totalCount else 0
                val momentsCount = moments.size

                val breakdown = mutableMapOf<String, Pair<Int, Int>>()
                // Define standard categories we want to report
                val categories = listOf("Travel", "Learning", "Fitness", "Social", "Career", "Others")
                for (cat in categories) {
                    val catChallenges = challenges.filter { it.category.equals(cat, ignoreCase = true) }
                    val catTotal = catChallenges.size
                    val catCompleted = catChallenges.count { it.status == "COMPLETED" }
                    breakdown[cat] = Pair(catCompleted, catTotal)
                }

                _totalChallenges.value = totalCount
                _completedChallenges.value = completedCount
                _completionRate.value = rate
                _totalMoments.value = momentsCount
                _categoryBreakdown.value = breakdown
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to calculate statistics"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
