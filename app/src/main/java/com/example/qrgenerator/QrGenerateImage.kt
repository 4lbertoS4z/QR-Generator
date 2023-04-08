package com.example.qrgenerator

import android.content.ContentValues
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.qrgenerator.databinding.ActivityMainBinding
import com.example.qrgenerator.databinding.ActivityQrGenerateImageBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.IOException

private lateinit var binding: ActivityQrGenerateImageBinding
class QrGenerateImage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding = ActivityQrGenerateImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button.setOnClickListener {
            val url = binding.editText.text.toString()
            if (url.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese una URL o texto", Toast.LENGTH_SHORT).show()

            } else {
                generateQRCode(url)
            }

        }

    }

    private fun generateQRCode(text: String) {

        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            binding.imageView.setImageBitmap(bitmap)

            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Guardar QR Code")
                .setMessage("¿Desea guardar el QR Code generado?")
                .setPositiveButton("Sí") { _, _ ->
                    saveQRCodeToGallery(bitmap)
                }
                .setNegativeButton("No", null)
                .create()

            alertDialog.show()

        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun saveQRCodeToGallery(bitmap: Bitmap) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "qr_code_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "QR Codes")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, values, null, null)
                }
                Toast.makeText(this, "QR Code guardado en la galería", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Error al guardar el QR Code en la galería", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
