package com.example.max_cargas.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.max_cargas.data.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert
    suspend fun insert(user: User)
}