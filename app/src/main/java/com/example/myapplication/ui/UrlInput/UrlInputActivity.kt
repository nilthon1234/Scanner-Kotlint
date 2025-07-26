package com.example.myapplication.ui.UrlInput

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.example.myapplication.R
import com.example.myapplication.data.database.DatabaseProvider
import com.example.myapplication.data.database.UrlConfig
import com.example.myapplication.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UrlInputActivity : AppCompatActivity() {
    private lateinit var urlInput: EditText
    private lateinit var saveButton: Button
    private lateinit var scanButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_url_input)

        urlInput = findViewById(R.id.urlInput)
        saveButton = findViewById(R.id.saveButton)
        scanButton = findViewById(R.id.scanButton)

        saveButton.isEnabled = false

        // Habilitar el botón de guardar solo si la URL es válida
        urlInput.doAfterTextChanged {
            saveButton.isEnabled = it.toString().isNotBlank() && isValidUrl(it.toString())
        }

        // Guardar la URL
        saveButton.setOnClickListener {
            val url = urlInput.text.toString()
            if (isValidUrl(url)) {
                saveUrlToDatabase(url)
            } else {
                Toast.makeText(this, "URL no válida", Toast.LENGTH_SHORT).show()
            }
        }

        // Iniciar el escaneo de QR
        scanButton.setOnClickListener {
            // Aquí puedes iniciar una actividad de escaneo similar a MainActivity
            // o reutilizar el código de escaneo de QR
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("scan_for_url", true) // Indicar que es para escanear la URL base
            startActivityForResult(intent, REQUEST_CODE_SCAN)
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    private fun saveUrlToDatabase(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = DatabaseProvider.getDatabase(applicationContext)
            db.urlConfigDao().insertUrl(UrlConfig(baseUrl = url))
            withContext(Dispatchers.Main) {
                Toast.makeText(this@UrlInputActivity, "URL guardada", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@UrlInputActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            val scannedUrl = data?.getStringExtra("scanned_url")
            if (scannedUrl != null && isValidUrl(scannedUrl)) {
                urlInput.setText(scannedUrl)
                saveUrlToDatabase(scannedUrl)
            } else {
                Toast.makeText(this, "URL escaneada no válida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val REQUEST_CODE_SCAN = 100
    }
}