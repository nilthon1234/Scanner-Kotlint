package com.example.myapplication.data.database

import android.content.Context
import com.example.myapplication.data.api.SlipperService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInitializer {

    fun createSlipperService(context: Context): SlipperService {
        val db = DatabaseProvider.getDatabase(context)
        val baseUrl = runBlocking {
            db.urlConfigDao().getBaseUrl()?.baseUrl
                ?: throw IllegalStateException("No base URL configured")
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(
                MoshiConverterFactory.create(
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            ))
            .build()

        return retrofit.create(SlipperService::class.java)
    }
}