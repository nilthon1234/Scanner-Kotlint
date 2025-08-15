package com.example.myapplication.ui.detail

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
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
import com.example.myapplication.data.database.DatabaseProvider
import com.example.myapplication.data.model.SlipperFullResponse
import com.example.myapplication.utils.SlipperUtils
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    // Helper function to parse comma-separated sizes into a map with counts
    private fun parseSizes(sizeString: String?): Map<String, Int> {
        if (sizeString.isNullOrBlank()) return emptyMap()
        val sizes = sizeString.split(",").map { it.trim() }
        return sizes.groupingBy { it }.eachCount()
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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = DatabaseProvider.getDatabase(context)
                val baseUrl = db.urlConfigDao().getBaseUrl()?.baseUrl
                    ?: throw IllegalStateException("No base URL configured")

                withContext(Dispatchers.Main) {
                    val typePermitidos = listOf("UNICO")
                    val typeLetras = listOf("ROPA")

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

                            val amount = item.amount ?: 0
                            if (typePermitidos.contains(item.producto?.uppercase()) && amount > 0) {
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
                                val tallasOrdenadas = if (typeLetras.contains(item.producto?.uppercase())) {
                                    tallasDisponibles
                                        .filter { it.key.matches(Regex("^[a-zA-Z]+$")) }
                                        .toList()
                                        .sortedBy { it.first }
                                } else {
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
                                    val sizeVisual = if (typeLetras.contains(item.producto?.uppercase())) {
                                        size
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
                            val imageUrl = item.urlImg?.let { url ->
                                if (url.contains("localhost")) {
                                    url.replace(UrlConstantsHttps.LOCALHOST_URL, baseUrl)
                                } else {
                                    url
                                }
                            }
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

                            val sizesMap = parseSizes(vitrinaObj.sizes)
                            if (sizesMap.isEmpty()) {
                                btnVitrinaSize.text = "Talla no disponible"
                                btnVitrinaSize.isEnabled = false
                                btnVitrinaSize.setBackgroundColor(Color.LTGRAY)
                                gridVitrinaSizes.visibility = View.GONE
                            } else {
                                btnVitrinaSize.visibility = View.GONE
                                gridVitrinaSizes.visibility = View.VISIBLE
                                val tallasOrdenadas = if (typeLetras.contains(vitrinaObj.producto?.uppercase())) {
                                    sizesMap
                                        .filter { it.key.matches(Regex("^[a-zA-Z]+$")) }
                                        .toList()
                                        .sortedBy { it.first }
                                } else {
                                    sizesMap
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
                                    val sizeVisual = if (typeLetras.contains(vitrinaObj.producto?.uppercase())) {
                                        size
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
                                            marginEnd = 40
                                            bottomMargin = 50
                                        }
                                        layoutParams = params
                                        setOnClickListener {
                                            vibrateOneSecond()
                                            if (sizesMap[size]?.let { it > 0 } == true) {
                                                onSizeClick(vitrinaObj, size, "VITRINA")
                                            } else {
                                                showToast("Talla $size no disponible para VITRINA")
                                            }
                                        }
                                    }
                                    gridVitrinaSizes.addView(button)
                                }
                            }
                        }
                    } else {
                        cardVitrina.visibility = View.GONE
                    }

                    // Update VITRINAB
                    if (response.vitrinaB != null) {
                        cardVitrinaB.visibility = View.VISIBLE
                        response.vitrinaB?.let { vitrinaBObj ->
                            tvVitrinaBAmount.text = "Cantidad: ${vitrinaBObj.amount ?: "N/A"}"
                            tvVitrinaBPrecio.text = "Precio: S/ ${vitrinaBObj.precio ?: "N/A"}"
                            tvVitrinaBTipo.text = "Tipo: ${vitrinaBObj.type ?: "N/A"}"
                            gridVitrinaBSizes.removeAllViews()

                            val sizesMap = parseSizes(vitrinaBObj.sizes)
                            if (sizesMap.isEmpty()) {
                                btnVitrinaBSize.text = "Talla no disponible"
                                btnVitrinaBSize.isEnabled = false
                                btnVitrinaBSize.setBackgroundColor(Color.LTGRAY)
                                gridVitrinaBSizes.visibility = View.GONE
                            } else {
                                btnVitrinaBSize.visibility = View.GONE
                                gridVitrinaBSizes.visibility = View.VISIBLE
                                val tallasOrdenadas = if (typeLetras.contains(vitrinaBObj.type?.uppercase())) {
                                    sizesMap
                                        .filter { it.key.matches(Regex("^[a-zA-Z]+$")) }
                                        .toList()
                                        .sortedBy { it.first }
                                } else {
                                    sizesMap
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
                                    val sizeVisual = if (typeLetras.contains(vitrinaBObj.type?.uppercase())) {
                                        size
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
                                            marginEnd = 40
                                            bottomMargin = 50
                                        }
                                        layoutParams = params
                                        setOnClickListener {
                                            vibrateOneSecond()
                                            if (sizesMap[size]?.let { it > 0 } == true) {
                                                onSizeClick(vitrinaBObj, size, "VITRINAB")
                                            } else {
                                                showToast("Talla $size no disponible para VITRINAB")
                                            }
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
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error al cargar la URL base")
                }
            }
        }
    }
}