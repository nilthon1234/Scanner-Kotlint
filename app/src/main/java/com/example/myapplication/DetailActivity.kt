package com.example.myapplication

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.myapplication.data.RegisterScannerRequest
import com.example.myapplication.data.SlipperDetail
import com.example.myapplication.data.SlipperFullResponse
import com.example.myapplication.data.VitrinaDetail
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
    // Vitrina VitrinaB
    private lateinit var tvVitrinaAmount: TextView
    private lateinit var tvVitrinaPrecio: TextView
    private lateinit var tvVitrinaTipo: TextView
    private lateinit var tvVitrinaBAmount: TextView
    private lateinit var tvVitrinaBPrecio: TextView
    private lateinit var tvVitrinaBTipo: TextView
    // Para el Boton size ALMACEN
    private lateinit var gridSizes: GridLayout
    private lateinit var btnVitrinaSize: Button
    private lateinit var btnVitrinaBSize: Button

    private lateinit var retrofit: Retrofit
    private lateinit var slipperService: SlipperService
    //para vibrador JAJAJA
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        //para el vibrador JAJAJA
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val btnVolver = findViewById<Button>(R.id.btnVolver)
        btnVolver.setOnClickListener {
            finish()
        }

        // Initialize views
        imageView = findViewById(R.id.imageView)
        tvBrand = findViewById(R.id.tvBrand)
        tvType = findViewById(R.id.tvType)
        tvCode = findViewById(R.id.tvCode)
        tvAmount = findViewById(R.id.tvAmount)
        tvGenero = findViewById(R.id.tvGenero)
        gridSizes = findViewById(R.id.gridSizes)
        btnVitrinaSize = findViewById(R.id.btnVitrinaSize)
        tvVitrinaAmount = findViewById(R.id.tvVitrinaAmount)
        tvVitrinaPrecio = findViewById(R.id.tvVitrinaPrecio)
        tvVitrinaTipo = findViewById(R.id.tvVitrinaTipo)
        btnVitrinaBSize = findViewById(R.id.btnVitrinaBSize)
        tvVitrinaBAmount = findViewById(R.id.tvVitrinaBAmount)
        tvVitrinaBPrecio = findViewById(R.id.tvVitrinaBPrecio)
        tvVitrinaBTipo = findViewById(R.id.tvVitrinaBTipo)
        tvPrice = findViewById(R.id.tvPrice)

        // Initialize Retrofit
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        retrofit = Retrofit.Builder()
            .baseUrl("https://bluejay-fitting-bluebird.ngrok-free.app/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        slipperService = retrofit.create(SlipperService::class.java)

        // Fetch initial details
        val url = intent.getStringExtra("scanned_url")
        if (url != null) {
            fetchDetails(url)
        }

        // Set click listeners for Vitrina and VitrinaB buttons
        btnVitrinaSize.setOnClickListener {
            vibrateOneSecond()
            fetchDetails(url!!) { response ->
                response?.vitrina?.let { vitrina ->
                    if (vitrina.sizes.isNullOrBlank() || vitrina.sizes == "0") {
                        Toast.makeText(this, "Talla no disponible para VITRINA", Toast.LENGTH_SHORT).show()
                    } else {
                        registerScanner(vitrina, vitrina.sizes, "VITRINA")
                    }
                }
            }
        }

        btnVitrinaBSize.setOnClickListener {
            vibrateOneSecond()
            fetchDetails(url!!) { response ->
                response?.vitrinaB?.let { vitrinaB ->
                    if (vitrinaB.sizes.isNullOrBlank() || vitrinaB.sizes == "0") {
                        Toast.makeText(this, "Talla no disponible para VITRINAB", Toast.LENGTH_SHORT).show()
                    } else {
                        registerScanner(vitrinaB, vitrinaB.sizes, "VITRINAB")
                    }
                }
            }
        }
    }
    //Para el vibrador JAJAJA
    private fun vibrateOneSecond() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200) // Para versiones antiguas
        }
    }

    private fun fetchDetails(url: String, callback: ((SlipperFullResponse?) -> Unit)? = null) {
        val fullPath = url.substringAfter("https://bluejay-fitting-bluebird.ngrok-free.app/")
        slipperService.getSlipperDetails(fullPath).enqueue(object : Callback<SlipperFullResponse> {
            override fun onResponse(call: Call<SlipperFullResponse>, response: Response<SlipperFullResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    callback?.invoke(data)
                    val producto = data?.almacen
                    val vitrina = data?.vitrina
                    val vitrinaB = data?.vitrinaB

                    // Mostrar datos de ALMACÉN
                    producto?.let {
                        tvPrice.text = "Precio: S/ ${it.price ?: "N/A"}"
                        tvBrand.text = "Marca: ${it.brand}"
                        tvType.text = "Tipo: ${it.type}"
                        tvCode.text = "Código: ${it.codToday}"
                        tvAmount.text = "Cantidad: ${it.amount}"
                        tvGenero.text = "Género: ${it.genero}"

                        gridSizes.removeAllViews() // Limpia el grid
                        val tallasDisponibles = it.sizes?.filter { entry -> entry.value > 0 }

                        if (tallasDisponibles.isNullOrEmpty()) {
                            gridSizes.visibility = View.GONE
                        } else {
                            gridSizes.visibility = View.VISIBLE
                            tallasDisponibles.forEach { entry ->
                                val size = entry.key
                                val cantidad = entry.value
                                val button = Button(this@DetailActivity).apply {
                                    text = "$size ($cantidad)"
                                    setBackgroundResource(R.drawable.size_button_bg)
                                    setTextColor(Color.BLACK)
                                    textSize = 14f
                                    // Establece ancho y margen personalizados
                                    val params = GridLayout.LayoutParams().apply {
                                        width = 200  // ancho en píxeles, puedes usar 160, 120, etc.
                                        height = GridLayout.LayoutParams.WRAP_CONTENT
                                        marginEnd = 40
                                        bottomMargin = 50
                                    }
                                    layoutParams = params
                                    // Set click listener for each size button
                                    setOnClickListener {
                                        vibrateOneSecond()
                                        // Re-fetch data to ensure latest state
                                        fetchDetails(url) { response ->
                                            response?.almacen?.let { almacen ->
                                                if (almacen.sizes?.get(size)?.let { it > 0 } == true) {
                                                    registerScanner(almacen, size, "ALMACEN")
                                                } else {
                                                    Toast.makeText(
                                                        this@DetailActivity,
                                                        "Talla $size no disponible para ALMACEN",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    }
                                }
                                gridSizes.addView(button)
                            }
                        }

                        val imageUrl = it.urlImg?.replace("http://localhost:80", "https://bluejay-fitting-bluebird.ngrok-free.app")
                        Glide.with(this@DetailActivity).load(imageUrl).into(imageView)
                    }

                    // Mostrar datos de VITRINA
                    vitrina?.let {
                        tvVitrinaAmount.text = "Cantidad: ${it.amount ?: "N/A"}"
                        tvVitrinaPrecio.text = "Precio: S/ ${it.precio ?: "N/A"}"
                        tvVitrinaTipo.text = "Tipo: ${it.type ?: "N/A"}"
                        if (it.sizes.isNullOrBlank() || it.sizes == "0") {
                            btnVitrinaSize.apply {
                                text = "Talla no disponible"
                                isEnabled = false
                                setBackgroundColor(Color.LTGRAY)
                            }
                        } else {
                            btnVitrinaSize.apply {
                                text = "Talla: ${it.sizes}"
                                isEnabled = true
                                setBackgroundColor(ContextCompat.getColor(context, R.color.purple_200))
                            }
                        }
                        tvVitrinaAmount.text = "Cantidad: ${it.amount ?: "N/A"}"
                        tvVitrinaPrecio.text = "Precio: S/ ${it.precio ?: "N/A"}"
                        tvVitrinaTipo.text = "Tipo: ${it.type ?: "N/A"}"
                    }

                    // Mostrar datos de VITRINA B
                    vitrinaB?.let {
                        tvVitrinaBAmount.text = "Cantidad: ${it.amount ?: "N/A"}"
                        tvVitrinaBPrecio.text = "Precio: S/ ${it.precio ?: "N/A"}"
                        tvVitrinaBTipo.text = "Tipo: ${it.type ?: "N/A"}"
                        if (it.sizes.isNullOrBlank() || it.sizes == "0") {
                            btnVitrinaBSize.apply {
                                text = "Talla no disponible"
                                isEnabled = false
                                setBackgroundColor(Color.LTGRAY)
                            }
                        } else {
                            btnVitrinaBSize.apply {
                                text = "Talla: ${it.sizes}"
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
                    callback?.invoke(null)
                }
            }

            override fun onFailure(call: Call<SlipperFullResponse>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                callback?.invoke(null)
            }
        })
    }

    private fun registerScanner(detail: Any, size: String, repositoryType: String) {
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
                    Toast.makeText(this@DetailActivity, "Registro exitoso en $repositoryType", Toast.LENGTH_SHORT).show()
                    // Refresh the details to reflect updated data
                    val url = intent.getStringExtra("scanned_url")
                    if (url != null) {
                        fetchDetails(url)
                    }
                } else {
                    Toast.makeText(this@DetailActivity, "Error al registrar: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(this@DetailActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}