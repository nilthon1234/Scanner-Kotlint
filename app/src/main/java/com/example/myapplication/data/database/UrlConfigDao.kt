package com.example.myapplication.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UrlConfigDao {

    @Query("DELETE FROM url_config")
    fun deleteAll()

    @Query("SELECT * FROM url_config WHERE id = 1")
    suspend fun getBaseUrl(): UrlConfig?

    @Insert
    suspend fun insertUrl(urlConfig: UrlConfig)
}