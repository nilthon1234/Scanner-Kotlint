package com.example.myapplication.ui.main

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.ui.UrlInput.UrlInputActivity
import com.example.myapplication.ui.detail.DetailActivity
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import com.example.myapplication.data.database.DatabaseProvider

class MainActivity : AppCompatActivity() {
    private var scanned = false
    private lateinit var previewView: PreviewView
    private lateinit var cameraProvider: ProcessCameraProvider
    private var animator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Verificar si hay una URL guardada
        CoroutineScope(Dispatchers.IO).launch {
            val db = DatabaseProvider.getDatabase(applicationContext)
            val urlConfig = db.urlConfigDao().getBaseUrl()
            withContext(Dispatchers.Main) {
                if (urlConfig == null) {
                    // No hay URL, redirigir a UrlInputActivity
                    startActivity(Intent(this@MainActivity, UrlInputActivity::class.java))
                    finish()
                } else {
                    // Hay URL, continuar con la inicialización
                    setContentView(R.layout.activity_main)
                    initializeCamera()
                }
            }
        }
    }

    private fun initializeCamera() {
        previewView = findViewById(R.id.previewView)
        val laserLine = findViewById<View>(R.id.laserLine)
        val overlayFrame = findViewById<View>(R.id.overlayFrame)

        // Animación
        overlayFrame.post {
            val height = overlayFrame.height.toFloat()
            animator = ObjectAnimator.ofFloat(
                laserLine, "translationY", 0f, height - laserLine.height
            ).apply {
                duration = 2000
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                start()
            }
        }

        // Permisos y cámara
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 0)
        } else {
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(ContextCompat.getMainExecutor(this), QRCodeAnalyzer(
                    onCodeScanned = { url ->
                        scanned = true
                        val intent = Intent(this, DetailActivity::class.java)
                        intent.putExtra("scanned_url", url)
                        startActivity(intent)
                    },
                    shouldScan = { !scanned }
                ))
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, analyzer)
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        }
    }

    override fun onResume() {
        super.onResume()
        scanned = false // Restablecer el estado de escaneo
        if (::cameraProvider.isInitialized) {
            startCamera() // Reiniciar la cámara
        }
    }

    override fun onPause() {
        super.onPause()
        if (::cameraProvider.isInitialized) {
            cameraProvider.unbindAll() // Desvincular la cámara
        }
        animator?.cancel() // Cancelar la animación
    }

    class QRCodeAnalyzer(
        private val onCodeScanned: (String) -> Unit,
        private val shouldScan: () -> Boolean
    ) : ImageAnalysis.Analyzer {

        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null && shouldScan()) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                BarcodeScanning.getClient().process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            barcode.rawValue?.let {
                                onCodeScanned(it)
                            }
                        }
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }
}