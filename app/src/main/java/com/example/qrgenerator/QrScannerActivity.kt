package com.example.qrgenerator


import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.qrgenerator.databinding.ActivityQrScannerBinding
import com.google.zxing.integration.android.IntentIntegrator

class QrScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrScannerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnScan.setOnClickListener {
            startScan()
        }
        binding.btnOpenUrl.setOnClickListener {
            openUrl()
        }
    }

    private fun startScan() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats("QR_CODE")
        integrator.setPrompt("Escanea el código QR")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()
    }

    private fun openUrl() {
        val url = binding.tvResult.text.toString()

        if (url.isEmpty()) {
            Toast.makeText(this, "Por favor, escanee primero un QR válido", Toast.LENGTH_SHORT).show()
        } else {
            var fullUrl = url
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                fullUrl = "http://$url"
            }
            if (Patterns.WEB_URL.matcher(fullUrl).matches()) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                startActivity(browserIntent)
            } else {
                Toast.makeText(this, "URL no válida", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null && result.contents != null) {
            binding.tvResult.text = result.contents
        }
    }
}
