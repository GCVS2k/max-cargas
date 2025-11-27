package com.example.max_cargas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.max_cargas.data.model.ChargerSpot
import com.example.max_cargas.data.model.User

// Versão atualizada para 3 para limpar dados antigos e evitar conflitos com a nova lógica de cadastro
@Database(entities = [User::class, ChargerSpot::class], version = 3)
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
                )
                .fallbackToDestructiveMigration() // Limpa o banco se a versão mudar
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}