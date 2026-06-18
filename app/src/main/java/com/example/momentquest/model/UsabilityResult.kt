package com.example.momentquest.model

data class UsabilityResult(
    val id: String,
    val participantName: String,
    val variant: String, // "A" or "B"
    val timeMs: Long,
    val easeRating: Int,
    val errorCount: Int,
    val timestamp: Long
)
