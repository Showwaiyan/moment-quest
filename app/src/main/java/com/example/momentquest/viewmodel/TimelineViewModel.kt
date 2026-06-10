package com.example.momentquest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.momentquest.model.TimelineItem
import com.example.momentquest.repository.ChallengeRepository
import com.example.momentquest.repository.MomentRepository
import kotlinx.coroutines.launch

class TimelineViewModel(application: Application) : AndroidViewModel(application) {
    private val challengeRepository = ChallengeRepository()
    private val momentRepository = MomentRepository()

    private val _timelineItems = MutableLiveData<List<TimelineItem>>()
    val timelineItems: LiveData<List<TimelineItem>> get() = _timelineItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private var currentCategory: String = "All"
    private var currentStatus: String = "ALL"
    private var currentQuery: String = ""

    fun loadTimeline(
        category: String = currentCategory,
        status: String = currentStatus,
        query: String = currentQuery
    ) {
        currentCategory = category
        currentStatus = status
        currentQuery = query
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val challenges = challengeRepository.getChallenges(context)
                val moments = momentRepository.getMoments(context)

                // Map and filter challenges
                val challengeItems = challenges.filter { challenge ->
                    val matchCategory = category == "All" || challenge.category.equals(category, ignoreCase = true)
                    val matchStatus = status == "ALL" || challenge.status.equals(status, ignoreCase = true)
                    matchCategory && matchStatus
                }.map { TimelineItem.ChallengeItem(it) }

                // Map and filter moments
                val momentItems = if (status == "ALL") {
                    moments.map { TimelineItem.MomentItem(it) }
                } else {
                    emptyList()
                }

                // Merge and sort by timestamp desc
                val merged = (challengeItems + momentItems).sortedByDescending { it.timestamp }
                
                // Apply search filter if query is not empty
                val filtered = if (query.isBlank()) {
                    merged
                } else {
                    merged.filter { item ->
                        when (item) {
                            is TimelineItem.ChallengeItem -> {
                                item.challenge.title.contains(query, ignoreCase = true) ||
                                        item.challenge.category.contains(query, ignoreCase = true)
                            }
                            is TimelineItem.MomentItem -> {
                                item.moment.title.contains(query, ignoreCase = true) ||
                                        item.moment.description.contains(query, ignoreCase = true) ||
                                        item.moment.mood.contains(query, ignoreCase = true)
                            }
                        }
                    }
                }
                
                _timelineItems.value = filtered
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load timeline data"
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
                loadTimeline()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete moment"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
