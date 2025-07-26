package com.example.myapplication.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "url_config")
data class UrlConfig(
    @PrimaryKey val id: Int = 1, // Usamos un ID fijo para almacenar solo una URL
    val baseUrl: String
)