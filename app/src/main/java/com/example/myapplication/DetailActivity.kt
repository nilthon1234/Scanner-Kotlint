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
import androidx.core.text.HtmlCompat
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
    //para el boton amount
    private lateinit var btnAmount: Button

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

    private lateinit var gridVitrinaSizes: GridLayout
    private lateinit var gridVitrinaBSizes: GridLayout

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
        btnAmount = findViewById(R.id.btnAmount)
        gridVitrinaSizes = findViewById(R.id.gridVitrinaSizes)
        gridVitrinaBSizes = findViewById(R.id.gridVitrinaBSizes)



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
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(100) // Para versiones antiguas
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
                    // Mostrar datos de ALMACÉN
                    producto?.let { item ->
                        tvPrice.text = "Precio: S/ ${item.price ?: "N/A"}"
                        tvBrand.text = "Marca: ${item.brand}"
                        tvType.text = "Tipo: ${item.type}"
                        tvCode.text = "Código: ${item.codToday}"
                        tvAmount.text = "Cantidad: ${item.amount}"
                        tvGenero.text = "Género: ${item.genero}"
                        tvAmount.visibility = View.VISIBLE
                        btnAmount.visibility = View.GONE
                        val typePermitidos = listOf("GORRA", "CANGURO", "MEDIAS")
                        val amount = item.amount ?: 0

                        if (typePermitidos.contains(item.type?.uppercase()) && amount > 0) {
                            tvAmount.visibility = View.GONE
                            btnAmount.apply {
                                visibility = View.VISIBLE
                                text = "Cantidad: $amount"
                                setOnClickListener {
                                    vibrateOneSecond()
                                    registerScanner(item, null, "ALMACEN")
                                }
                            }
                        } else {
                            tvAmount.text = "Cantidad: ${item.amount ?: "N/A"}"
                        }

                        gridSizes.removeAllViews()
                        val tallasDisponibles = item.sizes?.filter { it.value > 0 }

                        if (tallasDisponibles.isNullOrEmpty()) {
                            gridSizes.visibility = View.GONE
                        } else {
                            gridSizes.visibility = View.VISIBLE

                            // Ordenar por prefijo (USA, EU) y luego por valor numérico
                            val tallasOrdenadas = tallasDisponibles
                                .toList()
                                .sortedWith(compareBy(
                                    { extraerPrefijo(it.first) },
                                    { extraerValorNumerico(it.first) }
                                ))

                            // Obtener la lista de tallas en SEPARATION
                            val separationSizes = data?.separation?.map { it.size } ?: emptyList()

                            tallasOrdenadas.forEach { entry ->
                                val size = entry.first
                                val cantidad = entry.second
                                val sizeVisual = size
                                    .replace(Regex("[A-Za-z]+"), "") // quita EU o USA
                                    .replace("_", ".")

                                // Contar cuántas veces aparece esta talla en SEPARATION
                                val separationCount = separationSizes.count { it == size }

                                val button = Button(this@DetailActivity).apply {
                                    // Si hay separaciones, mostrar la cantidad disponible y la cantidad separada
                                    text = if (separationCount > 0) {
                                        HtmlCompat.fromHtml(
                                            "$sizeVisual (<font color='#007BFF'>$cantidad</font> | <font color='#FFA500'>$separationCount</font>)",
                                            HtmlCompat.FROM_HTML_MODE_LEGACY
                                        )
                                    } else {
                                        HtmlCompat.fromHtml(
                                            "$sizeVisual (<font color='#007BFF'>$cantidad</font>)",
                                            HtmlCompat.FROM_HTML_MODE_LEGACY
                                        )
                                    }
                                    // Aplicar fondo rojo si hay separaciones
                                    setBackgroundResource(if (separationCount > 0) {
                                        R.drawable.size_button_red_bg // Asegúrate de crear este recurso
                                    } else {
                                        R.drawable.size_button_bg
                                    })
                                    setTextColor(Color.BLACK)
                                    textSize = 14f
                                    val params = GridLayout.LayoutParams().apply {
                                        width = 205
                                        height = GridLayout.LayoutParams.WRAP_CONTENT
                                        marginEnd = 86
                                        bottomMargin = 50
                                    }
                                    layoutParams = params
                                    setOnClickListener {
                                        vibrateOneSecond()
                                        fetchDetails(url) { response ->
                                            response?.almacen?.let { almacen ->
                                                if (almacen.sizes?.get(size)?.let { it > 0 } == true) {
                                                    registerScanner(almacen, size, "ALMACEN")
                                                } else {
                                                    Toast.makeText(
                                                        this@DetailActivity,
                                                        "Talla $size no disponible para ALMACÉN",
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

                        val imageUrl = item.urlImg?.replace("http://localhost:80", "https://bluejay-fitting-bluebird.ngrok-free.app")
                        Glide.with(this@DetailActivity).load(imageUrl).into(imageView)
                    }

                    // Mostrar datos de VITRINA
                    vitrina?.let { vitrinaObj ->
                        tvVitrinaAmount.text = "Cantidad: ${vitrinaObj.amount ?: "N/A"}"
                        tvVitrinaPrecio.text = "Precio: S/ ${vitrinaObj.precio ?: "N/A"}"
                        tvVitrinaTipo.text = "Tipo: ${vitrinaObj.type ?: "N/A"}"

                        gridVitrinaSizes.removeAllViews()

                        if (vitrinaObj.sizes.isNullOrBlank() || vitrinaObj.sizes == "0") {
                            btnVitrinaSize.text = "Talla no disponible"
                            btnVitrinaSize.isEnabled = false
                            btnVitrinaSize.setBackgroundColor(Color.LTGRAY)
                            gridVitrinaSizes.visibility = View.GONE
                        } else {
                            btnVitrinaSize.visibility = View.GONE // Ocultamos el botón antiguo
                            gridVitrinaSizes.visibility = View.VISIBLE

                            val tallas = vitrinaObj.sizes
                                .split(",")
                                .mapNotNull { it.trim().toDoubleOrNull() } // convertir a Double para orden
                                .sorted()
                                .map { it.toString() } // devolver a String si deseas mostrar como texto

                            tallas.forEach { tallaStr ->
                                val button = Button(this@DetailActivity).apply {
                                    text = "Talla: $tallaStr"
                                    setBackgroundResource(R.drawable.size_button_bg)
                                    setTextColor(Color.BLACK)
                                    textSize = 14f
                                    val params = GridLayout.LayoutParams().apply {
                                        width = 200
                                        height = GridLayout.LayoutParams.WRAP_CONTENT
                                        marginEnd = 40
                                        bottomMargin = 50
                                    }
                                    layoutParams = params
                                    setOnClickListener {
                                        vibrateOneSecond()
                                        registerScanner(vitrinaObj, tallaStr, "VITRINA")
                                    }
                                }
                                gridVitrinaSizes.addView(button)
                            }
                        }
                    }


                    // Mostrar datos de VITRINA B
                    vitrinaB?.let { vitrinaBObj ->
                        tvVitrinaBAmount.text = "Cantidad: ${vitrinaBObj.amount ?: "N/A"}"
                        tvVitrinaBPrecio.text = "Precio: S/ ${vitrinaBObj.precio ?: "N/A"}"
                        tvVitrinaBTipo.text = "Tipo: ${vitrinaBObj.type ?: "N/A"}"

                        gridVitrinaBSizes.removeAllViews()

                        if (vitrinaBObj.sizes.isNullOrBlank() || vitrinaBObj.sizes == "0") {
                            btnVitrinaBSize.text = "Talla no disponible"
                            btnVitrinaBSize.isEnabled = false
                            btnVitrinaBSize.setBackgroundColor(Color.LTGRAY)
                            gridVitrinaBSizes.visibility = View.GONE
                        } else {
                            btnVitrinaBSize.visibility = View.GONE // Ocultamos el botón antiguo
                            gridVitrinaBSizes.visibility = View.VISIBLE

                            val tallas = vitrinaBObj.sizes
                                .split(",")
                                .mapNotNull { it.trim().toDoubleOrNull() } // convertir a Double para orden
                                .sorted()
                                .map { it.toString() } // devolver a String si deseas mostrar como texto

                            tallas.forEach { tallaStr ->
                                val button = Button(this@DetailActivity).apply {
                                    text = "Talla: $tallaStr"
                                    setBackgroundResource(R.drawable.size_button_bg)
                                    setTextColor(Color.BLACK)
                                    textSize = 14f
                                    val params = GridLayout.LayoutParams().apply {
                                        width = 200
                                        height = GridLayout.LayoutParams.WRAP_CONTENT
                                        marginEnd = 40
                                        bottomMargin = 50
                                    }
                                    layoutParams = params
                                    setOnClickListener {
                                        vibrateOneSecond()
                                        registerScanner(vitrinaBObj, tallaStr, "VITRINAB")
                                    }
                                }
                                gridVitrinaBSizes.addView(button)
                            }
                        }
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
    // Extrae el valor numérico (con decimales) para ordenar tallas como USA_8, USA8_5, EU32_5
    private fun extraerValorNumerico(talla: String): Double {
        val regex = Regex("""\d+(?:_?\d+)?""") // Busca números como 8, 8_5, 32_5
        val match = regex.find(talla)?.value?.replace("_", ".")
        return match?.toDoubleOrNull() ?: Double.MAX_VALUE
    }

    // Extrae el prefijo (EU, USA, etc.)
    private fun extraerPrefijo(talla: String): String {
        val regex = Regex("""^[A-Za-z]+""")
        return regex.find(talla)?.value ?: ""
    }



    private fun registerScanner(detail: Any, size: String?, repositoryType: String) {
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