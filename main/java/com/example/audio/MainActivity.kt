package com.example.audio

import android.Manifest.permission.RECORD_AUDIO
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private var player: MediaPlayer? = null
    private var recorder: MediaRecorder? = null

    private var openFileLabel: TextView? = null

    private var recorderFileDescriptor: ParcelFileDescriptor? = null
    private val setDestinationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
            val data: Intent? = result.data
            data?.let {
                val uri: Uri? = data.data
                uri?.let {
                    try {
                        //Log.v("data", result.data)
                        recorderFileDescriptor = contentResolver.openFileDescriptor(uri, "w")
                    } catch (e: FileNotFoundException) {
                        Log.e(javaClass.simpleName, e.stackTraceToString())
                    }
                }
            }
    }

    private var playerFileUri: Uri = Uri.EMPTY
    private val openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
            val data: Intent? = result.data
            data?.let {
                val uri: Uri? = data.data
                uri?.let {
                    playerFileUri = uri
                    // last file opened
                    val fname= File(playerFileUri.path).name
                    openFileLabel!!.text = fname
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        openFileLabel = findViewById(R.id.openedFileLabel)
        // Open file
        val openFileButton: Button = findViewById(R.id.openFileButton)
        openFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("*/*")
            try {
                openFileLauncher.launch(intent)
                Toast.makeText(this@MainActivity, "opened file", Toast.LENGTH_SHORT).show()
            } catch (e: ActivityNotFoundException) {
                Log.e(javaClass.simpleName, e.stackTraceToString())
            }
        }

        // Play audio
        val playButton: ImageButton = findViewById(R.id.playButton)
        playButton.setOnClickListener{
            if (player == null) {
                player = MediaPlayer().apply {
                    try {
                        setDataSource(applicationContext, playerFileUri)
                        prepare()
                    } catch (e: IOException) {
                        Log.e(javaClass.simpleName, "prepare() failed")
                    }
                }
            }
            player?.start()
            Toast.makeText(this@MainActivity, "playing file", Toast.LENGTH_SHORT).show()
        }

        // Pause audio
        val pauseButton: ImageButton = findViewById(R.id.pauseButton)
        pauseButton.setOnClickListener{
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    Toast.makeText(this@MainActivity, "playing paused", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Stop audio
        val stopButton: ImageButton = findViewById(R.id.stopButton)
        stopButton.setOnClickListener{
            player?.let {
                if (it.isPlaying) {
                    it.stop()
                    it.reset() // Reset the MediaPlayer for future use
                    Toast.makeText(this@MainActivity, "stopped playing", Toast.LENGTH_SHORT).show()
                }
                it.release()
                player = null
            }
        }

        // Create file
        val setDestinationButton: Button = findViewById(R.id.setDestinationButton)
        setDestinationButton.setOnClickListener{
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("audio/*")
            intent.putExtra(Intent.EXTRA_TITLE, "output.3gpp")
            try {
                setDestinationLauncher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                Log.e(javaClass.simpleName, e.stackTraceToString())
            }
        }

        // Record audio
        val recordButton: ImageButton = findViewById(R.id.startRecordingButton)
        recordButton.setOnClickListener{
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(recorderFileDescriptor!!.fileDescriptor)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                try {
                    prepare()
                    Log.v("prepatarion", "prepared well to record\n")
                } catch (e: IOException) {
                    Log.e(javaClass.simpleName, "prepare() failed")
                }
                start()
                Toast.makeText(this@MainActivity, "Recording started", Toast.LENGTH_SHORT).show()
            }
        }

        val stopRecordingButton: ImageButton = findViewById(R.id.stopRecordingButton)
        stopRecordingButton.setOnClickListener {
            recorder?.stop()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
        recorder?.release()
        recorder = null
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(RECORD_AUDIO), 0)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Microphone permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Microphone permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}