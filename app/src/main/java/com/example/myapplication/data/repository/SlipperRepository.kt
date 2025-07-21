package com.example.myapplication.data.repository

import com.example.myapplication.data.api.SlipperService
import com.example.myapplication.data.model.RegisterScannerRequest
import com.example.myapplication.data.model.SlipperDetail
import com.example.myapplication.data.model.SlipperFullResponse
import com.example.myapplication.data.model.VitrinaDetail
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SlipperRepository (private val slipperService: SlipperService) {
    fun fetchDetails(url: String, callback: (SlipperFullResponse?) -> Unit) {
        val fullPath = url.substringAfter("https://bluejay-fitting-bluebird.ngrok-free.app/")
        slipperService.getSlipperDetails(fullPath).enqueue(object : Callback<SlipperFullResponse> {
            override fun onResponse(call: Call<SlipperFullResponse>, response: Response<SlipperFullResponse>) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<SlipperFullResponse>, t: Throwable) {
                callback(null)
            }
        })
    }

    fun registerScanner(detail: Any, size: String?, repositoryType: String, onSuccess: () -> Unit) {
        val request = when (detail) {
            is VitrinaDetail -> RegisterScannerRequest(
                codToday = detail.codToday,
                repositoryType = repositoryType,
                company = detail.company,
                maquina = detail.maquina ?: "MAQ02",
                size = size,
                type = detail.type,
                genero = detail.genero,
                price = detail.precio
            )
            is SlipperDetail -> RegisterScannerRequest(
                codToday = detail.codToday,
                repositoryType = repositoryType,
                company = detail.company,
                maquina = detail.maquina ?: "MAQ02",
                size = size,
                type = detail.type,
                genero = detail.genero,
                price = detail.price
            )
            else -> return
        }

        slipperService.saveScanner(request).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    onSuccess()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                // Error handling is delegated to UI
            }
        })
    }
}