package com.example.max_cargas.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.max_cargas.data.model.ChargerSpot

@Dao
interface ChargerSpotDao {
    @Query("SELECT * FROM charger_spots")
    suspend fun getAllSpots(): List<ChargerSpot>

    @Insert
    suspend fun insert(spot: ChargerSpot)
}