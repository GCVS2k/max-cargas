package com.example.max_cargas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val password: String, // Em um app real, nunca salve senhas em texto puro! Use hash.
    val name: String
)