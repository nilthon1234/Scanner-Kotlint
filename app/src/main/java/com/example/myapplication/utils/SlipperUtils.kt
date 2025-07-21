package com.example.myapplication.utils

object SlipperUtils {
    fun extraerValorNumerico(talla: String): Double {
        val regex = Regex("""\d+(?:_?\d+)?""")
        val match = regex.find(talla)?.value?.replace("_", ".")
        return match?.toDoubleOrNull() ?: Double.MAX_VALUE
    }

    fun extraerPrefijo(talla: String): String {
        val regex = Regex("""^[A-Za-z]+""")
        return regex.find(talla)?.value ?: ""
    }
}