package com.example.momentquest.model

data class Moment(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val mood: String = "", // Happy, Grateful, Surprised, Reflective
    val photoUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)
