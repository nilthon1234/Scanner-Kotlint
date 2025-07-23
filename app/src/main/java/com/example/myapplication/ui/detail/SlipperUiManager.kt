package com.example.myapplication.ui.detail

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.api.UrlConstantsHttps
import com.example.myapplication.data.model.SlipperFullResponse
import com.example.myapplication.utils.SlipperUtils
import com.google.android.material.card.MaterialCardView


class SlipperUiManager(private val context: Context, private val vibrator: Vibrator) {
    fun vibrateOneSecond() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(100)
        }
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun updateUI(
        response: SlipperFullResponse,
        imageView: ImageView,
        tvPrice: TextView,
        tvBrand: TextView,
        tvType: TextView,
        tvCode: TextView,
        tvAmount: TextView,
        tvGenero: TextView,
        btnAmount: Button,
        gridSizes: GridLayout,
        tvVitrinaAmount: TextView,
        tvVitrinaPrecio: TextView,
        tvVitrinaTipo: TextView,
        btnVitrinaSize: Button,
        gridVitrinaSizes: GridLayout,
        tvVitrinaBAmount: TextView,
        tvVitrinaBPrecio: TextView,
        tvVitrinaBTipo: TextView,
        btnVitrinaBSize: Button,
        gridVitrinaBSizes: GridLayout,
        cardAlmacen: MaterialCardView,
        cardVitrina: MaterialCardView,
        cardVitrinaB: MaterialCardView,
        onSizeClick: (Any, String?, String) -> Unit
    ) {
        // Update ALMACEN
        if (response.almacen != null) {
            cardAlmacen.visibility = View.VISIBLE
            response.almacen?.let { item ->
                tvPrice.text = "Precio: S/ ${item.price ?: "N/A"}"
                tvBrand.text = "Marca: ${item.brand}"
                tvType.text = "Tipo: ${item.type}"
                tvCode.text = "Código: ${item.codToday}"
                tvAmount.text = "Cantidad: ${item.amount}"
                tvGenero.text = "Género: ${item.genero}"
                tvAmount.visibility = View.VISIBLE
                btnAmount.visibility = View.GONE

                val typePermitidos = listOf("GORRA", "CANGURO", "MEDIAS")
                val typeLetras = listOf("PANTALON", "POLO", "POLERA") // Added types for letter-based sizes
                val amount = item.amount ?: 0
                if (typePermitidos.contains(item.type?.uppercase()) && amount > 0) {
                    tvAmount.visibility = View.GONE
                    btnAmount.apply {
                        visibility = View.VISIBLE
                        text = "Cantidad: $amount"
                        setOnClickListener {
                            vibrateOneSecond()
                            onSizeClick(item, null, "ALMACEN")
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
                    val tallasOrdenadas = if (typeLetras.contains(item.type?.uppercase())) {
                        // Filter only letter-based sizes for PANTALON, POLO, POLERA
                        tallasDisponibles
                            .filter { it.key.matches(Regex("^[a-zA-Z]+$")) } // Only letter-based sizes (e.g., l, m, s, xl, xs)
                            .toList()
                            .sortedBy { it.first } // Sort alphabetically for consistency
                    } else {
                        // Existing logic for other types
                        tallasDisponibles
                            .toList()
                            .sortedWith(compareBy(
                                { SlipperUtils.extraerPrefijo(it.first) },
                                { SlipperUtils.extraerValorNumerico(it.first) }
                            ))
                    }
                    val separationSizes = response.separation?.map { it.size } ?: emptyList()
                    tallasOrdenadas.forEach { entry ->
                        val size = entry.first
                        val cantidad = entry.second
                        val sizeVisual = if (typeLetras.contains(item.type?.uppercase())) {
                            size // Show raw letter size (e.g., "l", "m", "s")
                        } else {
                            size.replace(Regex("[A-Za-z]+"), "").replace("_", ".")
                        }
                        val separationCount = separationSizes.count { it == size }
                        val button = Button(context).apply {
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
                            setBackgroundResource(if (separationCount > 0) {
                                R.drawable.size_button_red_bg
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
                                if (item.sizes?.get(size)?.let { it > 0 } == true) {
                                    onSizeClick(item, size, "ALMACEN")
                                } else {
                                    showToast("Talla $size no disponible para ALMACÉN")
                                }
                            }
                        }
                        gridSizes.addView(button)
                    }
                }
                val imageUrl = item.urlImg?.replace(UrlConstantsHttps.LOCALHOST_URL, UrlConstantsHttps.BASE_URL)
                Glide.with(context).load(imageUrl).into(imageView)
            }
        } else {
            cardAlmacen.visibility = View.GONE
        }

        // Update VITRINA
        if (response.vitrina != null) {
            cardVitrina.visibility = View.VISIBLE
            response.vitrina?.let { vitrinaObj ->
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
                    btnVitrinaSize.visibility = View.GONE
                    gridVitrinaSizes.visibility = View.VISIBLE
                    val tallas = vitrinaObj.sizes
                        .split(",")
                        .mapNotNull { it.trim().toDoubleOrNull() }
                        .sorted()
                        .map { it.toString() }
                    tallas.forEach { tallaStr ->
                        val button = Button(context).apply {
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
                                onSizeClick(vitrinaObj, tallaStr, "VITRINA")
                            }
                        }
                        gridVitrinaSizes.addView(button)
                    }
                }
            }
        } else {
            cardVitrina.visibility = View.GONE
        }

        // Update VITRINA B
        if (response.vitrinaB != null) {
            cardVitrinaB.visibility = View.VISIBLE
            response.vitrinaB?.let { vitrinaBObj ->
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
                    btnVitrinaBSize.visibility = View.GONE
                    gridVitrinaBSizes.visibility = View.VISIBLE
                    val tallas = vitrinaBObj.sizes
                        .split(",")
                        .mapNotNull { it.trim().toDoubleOrNull() }
                        .sorted()
                        .map { it.toString() }
                    tallas.forEach { tallaStr ->
                        val button = Button(context).apply {
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
                                onSizeClick(vitrinaBObj, tallaStr, "VITRINAB")
                            }
                        }
                        gridVitrinaBSizes.addView(button)
                    }
                }
            }
        } else {
            cardVitrinaB.visibility = View.GONE
        }
    }
}