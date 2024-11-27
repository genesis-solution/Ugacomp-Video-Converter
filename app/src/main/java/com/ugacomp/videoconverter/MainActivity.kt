package com.ugacomp.videoconverter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.SeekBar
import com.arthenica.mobileffmpeg.FFmpeg

private val REQUEST_CODE = 1001

class MainActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playerView: PlayerView = findViewById(R.id.player_view)
        player = SimpleExoPlayer.Builder(this).build()
        playerView.player = player

        // Prepare media item (replace with actual media URI)
        val mediaItem = MediaItem.fromUri("your-media-uri")
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

        // Add controls for play, pause, stop
        findViewById<Button>(R.id.play_button).setOnClickListener {
            player.playWhenReady = true
        }

        findViewById<Button>(R.id.pause_button).setOnClickListener {
            player.playWhenReady = false
        }

        findViewById<Button>(R.id.stop_button).setOnClickListener {
            player.stop()
        }

        // Request runtime permissions if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
        } else {
            // Permission already granted, proceed with scanning media
            scanMediaFiles()
        }
        // Example function for video to audio conversion (update paths accordingly)
//        convertVideoToAudio("inputVideoPath", "outputAudioPath")
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    // Function to convert video to audio using FFmpeg
    private fun convertVideoToAudio(inputPath: String, outputPath: String) {
        val cmd = "-i $inputPath -q:a 0 -map a $outputPath"
        FFmpeg.executeAsync(cmd) { _, returnCode ->
            if (returnCode == 0) {
                // Conversion successful
            } else {
                // Handle error
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with scanning media
                scanMediaFiles()
            } else {
                // Permission denied, handle appropriately
            }
        }
    }


    private fun scanMediaFiles() {
        val mediaFiles = mutableListOf<String>()

        val projection = arrayOf(
            MediaStore.MediaColumns.DATA, // File path
            MediaStore.MediaColumns.MIME_TYPE, // MIME type
            MediaStore.MediaColumns.DISPLAY_NAME // Display name
        )

        // Query for audio files
        val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val audioCursor = contentResolver.query(audioUri, projection, null, null, null)
        audioCursor?.use {
            val dataIndex = it.getColumnIndex(MediaStore.MediaColumns.DATA)
            while (it.moveToNext()) {
                val path = it.getString(dataIndex)
                mediaFiles.add(path)
            }
        }

        // Query for video files
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val videoCursor = contentResolver.query(videoUri, projection, null, null, null)
        videoCursor?.use {
            val dataIndex = it.getColumnIndex(MediaStore.MediaColumns.DATA)
            while (it.moveToNext()) {
                val path = it.getString(dataIndex)
                mediaFiles.add(path)
            }
        }

        // Query for image files (if needed)
        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val imageCursor = contentResolver.query(imageUri, projection, null, null, null)
        imageCursor?.use {
            val dataIndex = it.getColumnIndex(MediaStore.MediaColumns.DATA)
            while (it.moveToNext()) {
                val path = it.getString(dataIndex)
                mediaFiles.add(path)
            }
        }

        // Process mediaFiles as needed
        // For example, update the UI or start playback
    }

}

