package com.example.momentquest.model

data class Memory(
    val id: String = "",
    val notes: String = "",
    val photoUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val completedAt: Long = System.currentTimeMillis()
)
