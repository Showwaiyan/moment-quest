package com.example.momentquest.model

data class Challenge(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val deadline: Long? = null,
    val status: String = "PENDING", // PENDING, COMPLETED
    val createdAt: Long = System.currentTimeMillis()
)
