package com.example.youtubedownloader

import android.Manifest
import android.content.pm.PackageManager
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.*
import okio.IOException
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var urlInput: EditText
    private lateinit var downloadButton: Button
    private val client = OkHttpClient()

    // Adresse de ton serveur Flask (à adapter si tu passes en distant)
    private val SERVER_URL = "http://10.0.2.2:5000/download" // 10.0.2.2 pour l’émulateur Android

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        urlInput = findViewById(R.id.url_input)
        downloadButton = findViewById(R.id.download_button)

        // Demander permission si nécessaire
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        }

        downloadButton.setOnClickListener {
            val youtubeUrl = urlInput.text.toString().trim()
            if (youtubeUrl.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer une URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            downloadAudio(youtubeUrl)
        }
    }

    private fun downloadAudio(youtubeUrl: String) {
        Toast.makeText(this, "Téléchargement en cours...", Toast.LENGTH_SHORT).show()

        val json = """{"url":"$youtubeUrl"}"""
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json)

        val request = Request.Builder()
            .url(SERVER_URL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Erreur réseau : ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Erreur : ${response.code}", Toast.LENGTH_LONG).show()
                    }
                    return
                }

                val fileName = "youtube_audio_${System.currentTimeMillis()}.mp3"
                val file = File(getExternalFilesDir(null), fileName)
                val sink = file.outputStream()

                sink.use {
                    it.write(response.body?.bytes())
                }

                runOnUiThread {
                    Toast.makeText(applicationContext, "Audio téléchargé : ${file.name}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
