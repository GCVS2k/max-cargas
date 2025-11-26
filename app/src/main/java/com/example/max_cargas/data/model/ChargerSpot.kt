package com.example.max_cargas.data.model

import com.google.firebase.firestore.GeoPoint

data class ChargerSpot(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val location: GeoPoint? = null,
    val connectorType: String = "",
    val price: Double = 0.0,
    val isAvailable: Boolean = true,
    val description: String = "",
    val photoUrl: String = ""
)