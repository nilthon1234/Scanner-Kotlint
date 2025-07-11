package com.example.myapplication.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VitrinaDetail (
    @Json(name = "id") val id: Int?,
    @Json(name = "store") val store: String?,
    @Json(name = "brand") val brand: String?,
    @Json(name = "codToday") val codToday: String?,
    @Json(name = "amount") val amount: Int?,
    @Json(name = "image") val image: String?,
    @Json(name = "genero") val genero: String?,
    @Json(name = "company") val company: String?,
    @Json(name = "size") val size: String?,
    @Json(name = "precio") val precio: Double?,
    @Json(name = "type") val type: String?,
    @Json(name = "registrationDate") val registrationDate: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "repositoryType") val repositoryType: String?,

    //servira para el registro descanner intercepra
    var maquina: String? = "MAQ02"
)