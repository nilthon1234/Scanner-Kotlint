package com.example.myapplication.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SeparationDetail(
    @Json(name = "size") val size: String?
)
