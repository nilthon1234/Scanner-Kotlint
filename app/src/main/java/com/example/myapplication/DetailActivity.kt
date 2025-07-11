package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.bumptech.glide.Glide
import com.example.myapplication.data.SlipperDetail
import com.example.myapplication.data.SlipperFullResponse
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
    private lateinit var tvGenero: TextView
    private lateinit var tvSize: TextView
    private lateinit var tvCommpany: TextView
    private lateinit var tvRepositoryType: TextView
    private lateinit var tvPrice: TextView
    //Vitrina VitrinaB

    private lateinit var tvVitrinaAmount: TextView
    private lateinit var tvVitrinaPrecio: TextView
    private lateinit var tvVitrinaTipo: TextView
    private lateinit var tvVitrinaBAmount: TextView
    private lateinit var tvVitrinaBPrecio: TextView
    private lateinit var tvVitrinaBTipo: TextView

    //para el Boton size ALMACEN
    private lateinit var gridSizes: GridLayout
    private lateinit var btnVitrinaSize: Button
    private lateinit var btnVitrinaBSize: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        imageView = findViewById(R.id.imageView)
        tvBrand = findViewById(R.id.tvBrand)
        tvType = findViewById(R.id.tvType)
        tvCode = findViewById(R.id.tvCode)
        tvAmount = findViewById(R.id.tvAmount)
        tvGenero = findViewById(R.id.tvGenero)
        gridSizes = findViewById(R.id.gridSizes)

        //Vitrina VitrinaB
        btnVitrinaSize  = findViewById(R.id.btnVitrinaSize)
        tvVitrinaAmount = findViewById(R.id.tvVitrinaAmount)
        tvVitrinaPrecio = findViewById(R.id.tvVitrinaPrecio)
        tvVitrinaTipo = findViewById(R.id.tvVitrinaTipo)

        btnVitrinaBSize   = findViewById(R.id.btnVitrinaBSize)
        tvVitrinaBAmount = findViewById(R.id.tvVitrinaBAmount)
        tvVitrinaBPrecio = findViewById(R.id.tvVitrinaBPrecio)
        tvVitrinaBTipo = findViewById(R.id.tvVitrinaBTipo)


        val url = intent.getStringExtra("scanned_url")
        if (url != null) {
            fetchDetails(url)
        }
    }
    private fun fetchDetails(url: String) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://bluejay-fitting-bluebird.ngrok-free.app/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = retrofit.create(SlipperService::class.java)
        val fullPath = url.substringAfter("https://bluejay-fitting-bluebird.ngrok-free.app/")

        service.getSlipperDetails(fullPath).enqueue(object : Callback<SlipperFullResponse> {
            override fun onResponse(call: Call<SlipperFullResponse>, response: Response<SlipperFullResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    val producto = data?.almacen
                    val vitrina = data?.vitrina
                    val vitrinaB = data?.vitrinaB

                    // 👉 Mostrar datos de ALMACÉN
                    producto?.let {
                        tvBrand.text = "Marca: ${it.brand}"
                        tvType.text = "Tipo: ${it.type}"
                        tvCode.text = "Código: ${it.codToday}"
                        tvAmount.text = "Cantidad: ${it.amount}"
                        tvGenero.text = "Género: ${it.genero}"

                        gridSizes.removeAllViews() // Limpia el grid

                        val tallasDisponibles = it.sizes?.filter { entry -> entry.value > 0 }

// Primero limpiamos el grid de tallas anteriores
                        gridSizes.removeAllViews()

                        if (tallasDisponibles.isNullOrEmpty()) {
                            // Si no hay tallas disponibles, ocultamos el grid
                            gridSizes.visibility = View.GONE
                        } else {
                            // Si hay tallas, mostramos el grid
                            gridSizes.visibility = View.VISIBLE

                            tallasDisponibles.forEach { entry ->
                                val size = entry.key
                                val cantidad = entry.value

                                val button = Button(this@DetailActivity).apply {
                                    text = "$size ($cantidad)"
                                    setBackgroundResource(R.drawable.size_button_bg) // asegúrate de tener este fondo
                                    setTextColor(Color.BLACK)
                                    textSize = 14f
                                }

                                gridSizes.addView(button)
                            }
                        }



                        val imageUrl = it.urlImg?.replace("http://localhost:80", "https://bluejay-fitting-bluebird.ngrok-free.app")
                        Glide.with(this@DetailActivity).load(imageUrl).into(imageView)
                    }

                    // 👉 Mostrar datos de VITRINA
                    vitrina?.let {
                        if (it.size.isNullOrBlank() || it.size == "0") {
                            btnVitrinaSize.apply {
                                text = "Talla no disponible"
                                isEnabled = false
                                setBackgroundColor(Color.LTGRAY)
                            }
                        } else {
                            btnVitrinaSize.apply {
                                text = "Talla: ${it.size}"
                                isEnabled = true
                                setBackgroundColor(ContextCompat.getColor(context, R.color.purple_200)) // o el color original
                            }
                        }

                        tvVitrinaAmount.text = "Cantidad: ${it.amount ?: "N/A"}"
                        tvVitrinaPrecio.text = "Precio: S/ ${it.precio ?: "N/A"}"
                        tvVitrinaTipo.text = "Tipo: ${it.type ?: "N/A"}"
                    }

                    // 👉 Mostrar datos de VITRINA B
                    vitrinaB?.let {
                        if (it.size.isNullOrBlank() || it.size == "0") {
                            btnVitrinaBSize.apply {
                                text = "Talla no disponible"
                                isEnabled = false
                                setBackgroundColor(Color.LTGRAY)
                            }
                        } else {
                            btnVitrinaBSize.apply {
                                text = "Talla: ${it.size}"
                                isEnabled = true
                                setBackgroundColor(ContextCompat.getColor(context, R.color.purple_200))
                            }
                        }

                        tvVitrinaBAmount.text = "Cantidad: ${it.amount ?: "N/A"}"
                        tvVitrinaBPrecio.text = "Precio: S/ ${it.precio ?: "N/A"}"
                        tvVitrinaBTipo.text = "Tipo: ${it.type ?: "N/A"}"
                    }

                } else {
                    Toast.makeText(this@DetailActivity, "Respuesta vacía", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SlipperFullResponse>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }




}