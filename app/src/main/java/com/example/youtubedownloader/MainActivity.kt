package com.example.youtubedownloader

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var urlInput: EditText
    private lateinit var downloadButton: Button

    private val phpScriptUrl = "https://ton-serveur.com/download_audio.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        urlInput = findViewById(R.id.url_input)
        downloadButton = findViewById(R.id.download_button)

        downloadButton.setOnClickListener {
            val youtubeUrl = urlInput.text.toString()
            if (youtubeUrl.isNotBlank()) {
                downloadAudio(youtubeUrl)
            } else {
                Toast.makeText(this, "Veuillez entrer une URL YouTube valide", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadAudio(youtubeUrl: String) {
        val requestUrl = "$phpScriptUrl?url=${Uri.encode(youtubeUrl)}"

        val request = DownloadManager.Request(Uri.parse(requestUrl))
            .setTitle("Téléchargement audio YouTube")
            .setDescription("Téléchargement en cours...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "audio_youtube_${System.currentTimeMillis()}.mp3")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(this, "Téléchargement lancé...", Toast.LENGTH_SHORT).show()
    }
}
