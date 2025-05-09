package com.example.myapplication.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class SlipperDetail(
    @Json(name = "id") val id: Int?,
    @Json(name = "brand") val brand: String?,
    @Json(name = "codToday") val codToday: String?,
    @Json(name = "company") val company: String?,
    @Json(name = "amount") val amount: Int?,
    @Json(name = "registrationDate") val registrationDate: String?,
    @Json(name = "urlImg") val urlImg: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "genero") val genero: String?,
    @Json(name = "sizes") val sizes: Map<String, Int>?
)
