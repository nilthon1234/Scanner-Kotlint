package com.example.myapplication.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UrlConfig::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun urlConfigDao(): UrlConfigDao
}
