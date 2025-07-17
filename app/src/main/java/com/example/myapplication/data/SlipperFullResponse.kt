package com.example.myapplication.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SlipperFullResponse (
    @Json(name = "ALMACEN") val almacen: SlipperDetail?,
    @Json(name = "VITRINA") val vitrina: VitrinaDetail?,
    @Json(name = "VITRINAB") val vitrinaB: VitrinaDetail?,
    @Json(name = "SEPARATION") val separation: List<SeparationDetail>?
)