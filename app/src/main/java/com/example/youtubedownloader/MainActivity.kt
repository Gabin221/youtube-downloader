package com.example.youtubedownloader

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var urlInput: EditText
    private lateinit var downloadButton: Button

    private val phpScriptUrl = "use/your/api/Api_youtube_downloader/downloader.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        urlInput = findViewById(R.id.url_input)
        downloadButton = findViewById(R.id.download_button)

        downloadButton.setOnClickListener {
            val youtubeUrl = urlInput.text.toString()
            if (youtubeUrl.isNotBlank()) {
                Log.d("Download", "URL entrée: $youtubeUrl")
                downloadAudio(youtubeUrl)
            } else {
                Toast.makeText(this, "Veuillez entrer une URL YouTube valide", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadAudio(youtubeUrl: String) {
        val videoId = extractYoutubeId(youtubeUrl)
        if (videoId == null) {
            Toast.makeText(this, "URL YouTube invalide", Toast.LENGTH_SHORT).show()
            return
        }

        val requestUrl = "$phpScriptUrl?video_id=$videoId"
        Log.d("Download", "ID vidéo : $videoId")
        Log.d("Download", "URL de requête : $requestUrl")

        val request = DownloadManager.Request(Uri.parse(requestUrl))
            .setTitle("Téléchargement audio YouTube")
            .setDescription("Téléchargement en cours...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "audio_youtube_${System.currentTimeMillis()}.mp3")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        Toast.makeText(this, "Téléchargement lancé...", Toast.LENGTH_SHORT).show()

        checkDownloadStatus(downloadManager, downloadId)
    }

    private fun extractYoutubeId(url: String): String? {
        val regex = Regex("(?:v=|youtu\\.be/|embed/)([a-zA-Z0-9_-]{11})")
        return regex.find(url)?.groupValues?.get(1)
    }

    private fun checkDownloadStatus(downloadManager: DownloadManager, downloadId: Long) {
        // Utilisation d'un thread pour éviter de bloquer l'UI
        Thread {
            var isDownloading = true
            while (isDownloading) {
                // Attente pour vérifier l'état à intervalles réguliers
                Thread.sleep(1000)

                // Création de la requête pour vérifier le statut
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))
                    Log.d("Download", "Status: $status, URI: $uriString")

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            Log.d("Download", "Téléchargement réussi.")
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Téléchargement réussi.", Toast.LENGTH_SHORT).show()
                            }
                            isDownloading = false
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                            Log.e("Download", "Échec du téléchargement, raison : $reason")
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Échec du téléchargement.", Toast.LENGTH_SHORT).show()
                            }
                            isDownloading = false
                        }
                        DownloadManager.STATUS_PENDING -> {
                            Log.d("Download", "Téléchargement en attente...")
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            Log.d("Download", "Téléchargement en cours...")
                        }
                    }
                }

                cursor.close()
            }
        }.start()
    }
}
