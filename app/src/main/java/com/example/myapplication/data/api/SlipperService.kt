package com.example.myapplication.data.api

import com.example.myapplication.data.model.RegisterScannerRequest
import com.example.myapplication.data.model.SlipperFullResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface SlipperService {
    @GET
    fun getSlipperDetails(@Url url: String): Call<SlipperFullResponse>
    @POST("/scanner/save")
    fun saveScanner(@Body request: RegisterScannerRequest): Call<Unit>
}