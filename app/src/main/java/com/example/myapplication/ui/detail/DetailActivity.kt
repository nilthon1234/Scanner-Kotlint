package com.example.myapplication.ui.detail

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
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.model.RegisterScannerRequest
import com.example.myapplication.data.model.SlipperDetail
import com.example.myapplication.data.model.SlipperFullResponse
import com.example.myapplication.data.model.VitrinaDetail
import com.example.myapplication.data.api.SlipperService
import com.example.myapplication.data.api.UrlConstantsHttps
import com.example.myapplication.data.repository.SlipperRepository
import com.google.android.material.card.MaterialCardView
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class DetailActivity : AppCompatActivity() {
    private lateinit var btnAmount: Button
    private lateinit var imageView: ImageView
    private lateinit var tvBrand: TextView
    private lateinit var tvType: TextView
    private lateinit var tvCode: TextView
    private lateinit var tvAmount: TextView
    private lateinit var tvGenero: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvVitrinaAmount: TextView
    private lateinit var tvVitrinaPrecio: TextView
    private lateinit var tvVitrinaTipo: TextView
    private lateinit var tvVitrinaBAmount: TextView
    private lateinit var tvVitrinaBPrecio: TextView
    private lateinit var tvVitrinaBTipo: TextView
    private lateinit var gridSizes: GridLayout
    private lateinit var btnVitrinaSize: Button
    private lateinit var btnVitrinaBSize: Button
    private lateinit var gridVitrinaSizes: GridLayout
    private lateinit var gridVitrinaBSizes: GridLayout
    private lateinit var vibrator: Vibrator
    private lateinit var repository: SlipperRepository
    private lateinit var uiManager: SlipperUiManager
    private lateinit var cardAlmacen: MaterialCardView
    private lateinit var cardVitrina: MaterialCardView
    private lateinit var cardVitrinaB: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Initialize views
        initViews()

        // Initialize Retrofit and repository
        val retrofit = Retrofit.Builder()
            .baseUrl(UrlConstantsHttps.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            ))
            .build()
        repository = SlipperRepository(retrofit.create(SlipperService::class.java))
        uiManager = SlipperUiManager(this, vibrator)

        // Set up button listeners
        val btnVolver = findViewById<Button>(R.id.btnVolver)
        btnVolver.setOnClickListener { finish() }

        // Fetch initial details
        val url = intent.getStringExtra("scanned_url")
        if (url != null) {
            fetchAndUpdateUI(url)
        }

        // Set click listeners for buttons
        setupButtonListeners(url)
    }

    private fun initViews() {
        imageView = findViewById(R.id.imageView)
        tvBrand = findViewById(R.id.tvBrand)
        tvType = findViewById(R.id.tvType)
        tvCode = findViewById(R.id.tvCode)
        tvAmount = findViewById(R.id.tvAmount)
        tvGenero = findViewById(R.id.tvGenero)
        tvPrice = findViewById(R.id.tvPrice)
        btnAmount = findViewById(R.id.btnAmount)
        gridSizes = findViewById(R.id.gridSizes)
        btnVitrinaSize = findViewById(R.id.btnVitrinaSize)
        tvVitrinaAmount = findViewById(R.id.tvVitrinaAmount)
        tvVitrinaPrecio = findViewById(R.id.tvVitrinaPrecio)
        tvVitrinaTipo = findViewById(R.id.tvVitrinaTipo)
        btnVitrinaBSize = findViewById(R.id.btnVitrinaBSize)
        tvVitrinaBAmount = findViewById(R.id.tvVitrinaBAmount)
        tvVitrinaBPrecio = findViewById(R.id.tvVitrinaBPrecio)
        tvVitrinaBTipo = findViewById(R.id.tvVitrinaBTipo)
        gridVitrinaSizes = findViewById(R.id.gridVitrinaSizes)
        gridVitrinaBSizes = findViewById(R.id.gridVitrinaBSizes)
        cardAlmacen = findViewById<MaterialCardView>(R.id.cardAlmacen)
        cardVitrina = findViewById<MaterialCardView>(R.id.cardVitrina)
        cardVitrinaB = findViewById<MaterialCardView>(R.id.cardVitrinaB)
    }

    private fun fetchAndUpdateUI(url: String) {
        repository.fetchDetails(url) { response ->
            if (response != null) {
                uiManager.updateUI(
                    response,
                    imageView,
                    tvPrice, tvBrand, tvType, tvCode, tvAmount, tvGenero, btnAmount,
                    gridSizes, tvVitrinaAmount, tvVitrinaPrecio, tvVitrinaTipo, btnVitrinaSize,
                    gridVitrinaSizes, tvVitrinaBAmount, tvVitrinaBPrecio, tvVitrinaBTipo, btnVitrinaBSize,
                    gridVitrinaBSizes, cardAlmacen, cardVitrina, cardVitrinaB,
                ) { detail, size, repositoryType ->
                    repository.registerScanner(detail, size, repositoryType) {
                        fetchAndUpdateUI(url) // Refresh UI after successful registration
                    }
                }
            }
        }
    }

    private fun setupButtonListeners(url: String?) {
        btnVitrinaSize.setOnClickListener {
            if (url != null) {
                uiManager.vibrateOneSecond()
                repository.fetchDetails(url) { response ->
                    response?.vitrina?.let { vitrina ->
                        if (vitrina.sizes.isNullOrBlank() || vitrina.sizes == "0") {
                            uiManager.showToast("Talla no disponible para VITRINA")
                        } else {
                            repository.registerScanner(vitrina, vitrina.sizes, "VITRINA") {
                                fetchAndUpdateUI(url)
                            }
                        }
                    }
                }
            }
        }

        btnVitrinaBSize.setOnClickListener {
            if (url != null) {
                uiManager.vibrateOneSecond()
                repository.fetchDetails(url) { response ->
                    response?.vitrinaB?.let { vitrinaB ->
                        if (vitrinaB.sizes.isNullOrBlank() || vitrinaB.sizes == "0") {
                            uiManager.showToast("Talla no disponible para VITRINAB")
                        } else {
                            repository.registerScanner(vitrinaB, vitrinaB.sizes, "VITRINAB") {
                                fetchAndUpdateUI(url)
                            }
                        }
                    }
                }
            }
        }
    }
}