package com.example.myapplication.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterScannerRequest(
    @Json(name = "codToday") val codToday: String?,
    @Json(name = "repositoryType") val repositoryType: String?,
    @Json(name = "company") val company: String?,
    @Json(name = "maquina") val maquina: String?,
    @Json(name = "sizes") val size: String? = null,
    @Json(name = "type") val type: String?,
    @Json(name = "genero") val genero: String?,
    @Json(name = "price") val price: Double?
)
