package com.example.myapplication.interfaces

import com.example.myapplication.data.SlipperDetail
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface SlipperService {

    @GET
    fun getSlipperDetails(@Url url: String): Call<SlipperDetail>
}