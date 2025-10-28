package com.example.mycal.model

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val timestamp: Long,
    val speed: Float,
    val accuracy: Float
)
