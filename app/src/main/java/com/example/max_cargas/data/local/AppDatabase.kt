package com.example.max_cargas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.max_cargas.data.model.ChargerSpot
import com.example.max_cargas.data.model.User

@Database(entities = [User::class, ChargerSpot::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun chargerSpotDao(): ChargerSpotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "charge_bsb_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}