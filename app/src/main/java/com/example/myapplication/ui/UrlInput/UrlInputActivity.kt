package com.example.myapplication.ui.UrlInput

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
    private lateinit var logoImageView: ImageView
    private lateinit var quickRotateAnimation: Animation
    private var isAnimationScheduled = false
    private val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_url_input)

        urlInput = findViewById(R.id.urlInput)
        saveButton = findViewById(R.id.saveButton)
        logoImageView = findViewById(R.id.logoImageView)

        //quickRotateAnimation = AnimationUtils.loadAnimation(this, R.anim.spin_pulse)

        // Iniciar el ciclo de animación
        scheduleNextRotation()

        // ... resto de tu código ...
        saveButton.isEnabled = false
        urlInput.doAfterTextChanged {
            saveButton.isEnabled = it.toString().isNotBlank() && isValidUrl(it.toString())
        }

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
    private fun animateViews() {
        // Cargar la animación
        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        // Aplicar animaciones con retraso escalonado para un efecto secuencial
        urlInput.startAnimation(scaleAnimation)

        scaleAnimation.setStartOffset(100) // 100ms de retraso
        saveButton.startAnimation(scaleAnimation)

        scaleAnimation.setStartOffset(200) // 200ms de retraso
        scanButton.startAnimation(scaleAnimation)
    }
    private fun scheduleNextRotation() {
        if (!isAnimationScheduled) {
            isAnimationScheduled = true
            handler.postDelayed({
                logoImageView.startAnimation(quickRotateAnimation)
                // Cuando termina la animación, programar la siguiente
                quickRotateAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        // Programar la próxima rotación después de 2 segundos
                        handler.postDelayed({
                            scheduleNextRotation()
                        }, 2000) // 2 segundos de espera
                        isAnimationScheduled = false
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }, 2000) // Esperar 2 segundos antes de la primera rotación también
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Limpiar callbacks para evitar fugas de memoria
    }


}