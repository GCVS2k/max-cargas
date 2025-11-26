package com.example.max_cargas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charger_spots")
data class ChargerSpot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val connectorType: String,
    val price: Double,
    val isAvailable: Boolean,
    val description: String,
    val photoUrl: String = ""
)