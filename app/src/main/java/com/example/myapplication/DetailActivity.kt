package com.example.myapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.bumptech.glide.Glide
import com.example.myapplication.data.SlipperDetail
import com.example.myapplication.databinding.ActivityDetailBinding
import com.example.myapplication.interfaces.SlipperService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class DetailActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var tvBrand: TextView
    private lateinit var tvType: TextView
    private lateinit var tvCode: TextView
    private lateinit var tvAmount: TextView
    private lateinit var tvSize: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        imageView = findViewById(R.id.imageView)
        tvBrand = findViewById(R.id.tvBrand)
        tvType = findViewById(R.id.tvType)
        tvCode = findViewById(R.id.tvCode)
        tvAmount = findViewById(R.id.tvAmount)
        tvSize = findViewById(R.id.tvSize)

        val url = intent.getStringExtra("scanned_url")
        if (url != null) {
            fetchDetails(url)
        }
    }
    private fun fetchDetails(url: String) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())  // Esto permite usar reflexión
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://44cf-2001-1388-5547-fb1b-e586-fd99-6171-13fa.ngrok-free.app/") // solo base
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = retrofit.create(SlipperService::class.java)
        val fullPath = url.substringAfter("https://44cf-2001-1388-5547-fb1b-e586-fd99-6171-13fa.ngrok-free.app/")

        service.getSlipperDetails(fullPath).enqueue(object : Callback<SlipperDetail> {
            override fun onResponse(call: Call<SlipperDetail>, response: Response<SlipperDetail>) {
                if (response.isSuccessful) {
                    val detail = response.body()
                    detail?.let {
                        tvBrand.text = "Marca: ${it.brand}"
                        tvType.text = "Tipo: ${it.type}"
                        tvCode.text = "Código: ${it.codToday}"
                        tvAmount.text = "Cantidad: ${it.amount}"
                        val tallasDisponibles = it.sizes?.filter {entry -> entry.value > 0 }
                        tvSize.text = if (tallasDisponibles.isNullOrEmpty()){
                            "No hay tallas disponibles"
                        }else{
                            "Tallas Disponibles:\n" + tallasDisponibles.entries.joinToString("\n"){
                                entry -> "${entry.key}: ${entry.value}"
                            }
                        }

                        val imageUrl = it.urlImg?.replace("http://localhost:8080", "https://44cf-2001-1388-5547-fb1b-e586-fd99-6171-13fa.ngrok-free.app")
                        Glide.with(this@DetailActivity)
                            .load(imageUrl)
                            .into(imageView)
                    }
                }
            }

            override fun onFailure(call: Call<SlipperDetail>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }


}