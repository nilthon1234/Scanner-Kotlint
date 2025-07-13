package com.example.myapplication.interfaces

import com.example.myapplication.data.RegisterScannerRequest
import com.example.myapplication.data.SlipperDetail
import com.example.myapplication.data.SlipperFullResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface SlipperService {
    @GET
    fun getSlipperDetails(@Url url: String): Call<SlipperFullResponse>
    @POST("scanner/save")
    fun saveScanner(@Body request: RegisterScannerRequest): Call<Unit>
}